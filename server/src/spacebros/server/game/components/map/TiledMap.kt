package spacebros.server.game.components.map

import io.vertx.core.json.JsonObject
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.InflaterInputStream

class TiledMap(val width: Int, height: Int) {
    companion object {
        val FLIPPED_HORIZONTALLY_FLAG = 0x80000000.toInt()
        val FLIPPED_VERTICALLY_FLAG   = 0x40000000.toInt()
        val FLIPPED_DIAGONALLY_FLAG   = 0x20000000.toInt()
        val MASK_CLEAR = 0xE0000000.toInt()

        fun parse(jsonObject: JsonObject): TiledMap {
            val tiledMap = TiledMap(jsonObject.getInteger("width"), jsonObject.getInteger("height"))
            val layers = jsonObject.getJsonArray("layers")
            val tilesets = jsonObject.getJsonArray("tilesets")
            (0..tilesets.size() -1).map { tilesets.getJsonObject(it) }.forEach { tileset ->
                tiledMap.tilesets.add(parseTileset(tileset))
            }
            (0..layers.size() - 1).map { layers.getJsonObject(it) }
                    .filter { it.getString("type") == "tilelayer" }.forEach { layer ->
                tiledMap.layers.add(parseTileLayer(layer))
            }
            return tiledMap
        }

        private fun parseTileLayer(layer: JsonObject): TileLayer {
            if (layer.getString("compression") != "zlib")
                throw IllegalStateException("Unsupported compression for layer; ${layer.getString("compression")}")
            val width = layer.getInteger("width")
            val height = layer.getInteger("height")
            val name = layer.getString("name")
            val base64Data = layer.getString("data")
            val compressedData = Base64.getDecoder().decode(base64Data)

            val tiles = IntArray(width * height)

            val strm = InflaterInputStream(ByteArrayInputStream(compressedData))
            val reader = strm.buffered()
            reader.use {
                val tmp = ByteArray(4)
                var idx = 0
                fun fuk(b: Byte) = b.toInt() and 0xFF
                while (true) {
                    val read = reader.read(tmp)
                    if (read == -1) break
                    if (read != tmp.size) throw IllegalStateException("Error reading compressed data.")

                    var globalTileId = fuk(tmp[0]) or fuk(tmp[1]).shl(8) or
                            fuk(tmp[2]).shl(16) or fuk(tmp[3]).shl(24)
                    // clear flags
                    // goddamn it intellij why would you even do this
                    // what this actually does: globalTileId &= ~(FLIPPED_HORIZONTALLY_FLAG | FLIPPED_VERTICALLY_FLAG | FLIPPED_DIAGONALLY_FLAG)
                    globalTileId = globalTileId and (FLIPPED_HORIZONTALLY_FLAG or FLIPPED_VERTICALLY_FLAG or FLIPPED_DIAGONALLY_FLAG).inv()
                    tiles[idx] = globalTileId
                    idx += 1
                }
            }
            val tileLayer = TileLayer(name, width, height, tiles)
            return tileLayer
        }

        private fun parseTileset(tileset: JsonObject): TileSet {
            val name = tileset.getString("name")
            val firstgid = tileset.getInteger("firstgid")
            val img = tileset.getString("image")
            return TileSet(name, firstgid, img)
        }
    }

    val layers = arrayListOf<Layer>()
    val tilesets = arrayListOf<TileSet>()

    fun getTile(tileGid: Int): Tile {
        val tileSet = tilesets.lastOrNull {
            tileGid >= it.firstgid
        } ?: return EmptyTile()
        return Tile(tileGid, tileSet)
    }
}

interface Layer {
    val name: String
}

class TileLayer(override val name: String,
                val width: Int, val height: Int,
                val tiles: IntArray) : Layer {

    fun getCell(x: Int, y: Int): Int? {
        if (x < 0) throw IllegalArgumentException("x < 0")
        if (y < 0) throw IllegalArgumentException("y < 0")
        if (x > width) throw IllegalArgumentException("x ($x) > width ($width)")
        if (y > height) throw IllegalArgumentException("y ($y) > height ($height)")

        val index = x + (y * width)
        return tiles[index]
    }
}

class TileSet(val name: String, val firstgid: Int, val image: String) {
    companion object {
        fun empty(): TileSet {
            return TileSet("[UNDEFINED]", -1, "[UNDEFINED]")
        }
    }
}

open class Tile(val gid: Int, val tileset: TileSet) {
    val localTileId: Int by lazy { gid - tileset.firstgid }
}

class EmptyTile(): Tile(-1, TileSet.empty())
