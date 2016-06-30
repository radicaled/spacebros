package spacebros.server.game.components.map

import io.vertx.core.json.JsonObject
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.InflaterInputStream

class TiledMap(val width: Int, height: Int) {
    companion object {
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
                while (true) {
                    val read = reader.read(tmp)
                    if (read == -1) break
                    if (read != tmp.size) throw IllegalStateException("Error reading compressed data.")

                    tiles[idx] = tmp[0].toInt() or tmp[1].toInt().shl(8) or
                            tmp[2].toInt().shl(16) or tmp[3].toInt().shl(24)
                    idx += 1
                }
            }
            val tileLayer = TileLayer(name, width, height, tiles)
            return tileLayer
        }

        private fun parseTileset(tileset: JsonObject): TileSet {
            val name = tileset.getString("name")
            val firstgid = tileset.getInteger("firstgid")
            return TileSet(name, firstgid)
        }
    }

    val layers = arrayListOf<Layer>()
    val tilesets = arrayListOf<TileSet>()
}

interface Layer {
    val name: String
}

class TileLayer(override val name: String,
                val width: Int, val height: Int,
                val tiles: IntArray) : Layer {

    fun getTile(x: Int, y: Int): Int? {
        if (x < 0) throw IllegalArgumentException("x < 0")
        if (y < 0) throw IllegalArgumentException("y < 0")
        if (x > width) throw IllegalArgumentException("x ($x) > width ($width)")
        if (y > height) throw IllegalArgumentException("y ($y) > height ($height)")

        val index = x + (y * width)
        return tiles[index]
    }
}

class TileSet(val name: String, val firstgid: Int) {

}
