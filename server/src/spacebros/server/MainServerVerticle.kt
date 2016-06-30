package spacebros.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.ServerWebSocket
import io.vertx.ext.web.Router
import spacebros.server.game.GameVerticle

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
    }

    fun createRouter() = Router.router(vertx).apply {
    }

    fun handleRequest(request: HttpServerRequest) {
        router.accept(request)
    }

    fun handleWebsocket(websocket: ServerWebSocket) {
        if (websocket.path() != "/gameStream") {
            websocket.reject()
        } else {
            // INTO THE RABBIT WHOLE
            game.acceptRemoteConnection(websocket)
        }
    }
}
