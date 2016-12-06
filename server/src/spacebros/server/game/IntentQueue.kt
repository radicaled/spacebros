package spacebros.server.game

class IntentQueue {
    private val intents = arrayListOf<Intent>()

    fun append(item: Intent) {
        intents.add(item)
    }

    fun take(maxNum: Int): List<Intent> {
        synchronized(intents) {
            val itemsToTake = Math.min(maxNum, intents.size - 1)
            val items = intents.slice(0..itemsToTake)
            (0..itemsToTake).reversed().forEach { intents.removeAt(it) }
            return items
        }
    }
}
