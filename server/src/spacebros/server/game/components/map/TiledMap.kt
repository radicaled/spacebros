package spacebros.server.game.components.map

import io.vertx.core.json.JsonObject
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

class TiledMap(val width: Int, height: Int) {
    companion object {
        fun parse(jsonObject: JsonObject): TiledMap {
            val tiledMap = TiledMap(jsonObject.getInteger("width"), jsonObject.getInteger("height"))
            val layers = jsonObject.getJsonArray("layers")
            (0..layers.size() - 1).map { layers.getJsonObject(it) }
                    .filter { it.getString("type") == "tilelayer" }.forEach { layer ->
                if (layer.getString("compression") != "zlib")
                    throw IllegalStateException("Unsupported compression for layer; ${layer.getString("compression")}")
                val width = layer.getInteger("width")
                val height = layer.getInteger("height")
                val name   = layer.getString("name")
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

                tiledMap.layers.add(TileLayer(name, width, height, tiles))
            }
            return tiledMap
        }
    }

    val layers = arrayListOf<Layer>()
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
