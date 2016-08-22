package spacebros.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.net.NetServerOptions
import io.vertx.core.net.NetSocket
import io.vertx.ext.web.Router
import spacebros.server.game.GameVerticle
import spacebros.server.game.NetGameConnection
import spacebros.server.game.WebsocketGameConnection

@Suppress("unused")
class MainServerVerticle : AbstractVerticle() {
    val router = createRouter()
    val game = GameVerticle()
    override fun start() {
        vertx.deployVerticle(game)
        vertx.createHttpServer()
                .websocketHandler { handleWebsocket(it) }
                .requestHandler { handleRequest(it) }
                .listen(8080)
        vertx.createNetServer()
                .connectHandler { handleNetSocket(it) }
                .listen(3030)
    }

    fun createRouter(): Router = Router.router(vertx).apply {
    }

    fun handleRequest(request: HttpServerRequest) {
        router.accept(request)
    }

    fun handleNetSocket(socket: NetSocket) {
        game.acceptRemoteConnection(NetGameConnection(socket))
    }

    fun handleWebsocket(websocket: ServerWebSocket) {
        if (websocket.path() != "/gameStream") {
            websocket.reject()
        } else {
            // INTO THE RABBIT WHOLE
            game.acceptRemoteConnection(WebsocketGameConnection(websocket))
        }
    }
}
