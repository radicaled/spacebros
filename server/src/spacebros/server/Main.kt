package spacebros.server

import io.vertx.core.Vertx

fun main(vararg args: String) {
    Vertx.vertx().deployVerticle(MainServerVerticle())
}
