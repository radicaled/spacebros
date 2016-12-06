package spacebros.server.game

data class Intent(
        val invokingEntityId: Int,
        val targetEntityId: Int,
        val actionName: String
)
