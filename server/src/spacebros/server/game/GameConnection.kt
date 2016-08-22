package spacebros.server.game

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.net.NetSocket

abstract class GameConnection {
    protected var dataReceivedHandler: ((String) -> Unit)? = null
    protected var closedHandler: (() -> Unit)? = null

    abstract fun sendData(data: String)

    fun dataHandler(callback: (String) -> Unit) {
        dataReceivedHandler = callback
    }

    fun closeHandler(callback: () -> Unit) {
        closedHandler = callback
    }
}

class WebsocketGameConnection(val websocket: ServerWebSocket) : GameConnection() {
    init {
        websocket.frameHandler { dataReceivedHandler?.invoke(it.textData()) }
        websocket.closeHandler { closedHandler?.invoke() }
    }

    override fun sendData(data: String) {
        websocket.writeFinalTextFrame(data)
    }
}

class NetGameConnection(val socket: NetSocket) : GameConnection() {
    var buffer: Buffer = Buffer.buffer()

    init {
        socket.handler {
            // Message format:
            // "MSG": header, ASCII
            // Unsigned Integer, Big Endian: how many bytes the message is.
            // A series of bytes, utf-8 encoded
            acceptData(it.bytes)
        }
        socket.closeHandler { closedHandler?.invoke() }
    }

    override fun sendData(data: String) {
        val buffer = Buffer.buffer()
        val messageBuffer = Buffer.buffer(data, "utf-8")
        buffer.appendString("MSG", "ascii")
        buffer.appendUnsignedInt(messageBuffer.length().toLong())
        buffer.appendBuffer(messageBuffer)
        socket.write(data)
    }

    fun acceptData(bytes: ByteArray) {
        buffer.appendBytes(bytes)
        while (true) {
            val msg = readBufferedMessage()
            println("Iterated over msg: $msg vs its length: ${buffer.length()}")
            if (msg != null)
                dataReceivedHandler?.invoke(msg)
            else
                break
        }

    }

    private fun readBufferedMessage(): String? {
        var data: String? = null

        if (buffer.length() > 3 && buffer.getString(0, 3, "ascii") == "MSG") {
            val headerLength = 3
            val sizeLength = 4
            val expectedLength = buffer.getUnsignedInt(3)
            val actualLength = (buffer.length() - (headerLength + sizeLength))
            val dataStartPosition = (headerLength + sizeLength)
            if (actualLength >= expectedLength) {
                val dataEndPosition = dataStartPosition + expectedLength.toInt()
                data = buffer.getString(dataStartPosition, dataEndPosition, "utf-8")
                // NOTE: Buffer#slice = automatic - 1 at end of buffer.
                buffer = Buffer.buffer(buffer.slice(dataEndPosition,
                        buffer.length()).bytes)
            } else {
                // Temp debug
                println("[DEBUG] Received a new message, but not complete...")
                println("expectedLength: $expectedLength")
                println("actualLength: $actualLength")
            }
        }
        return data
    }
}
