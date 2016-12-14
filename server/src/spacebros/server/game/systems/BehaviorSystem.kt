package spacebros.server.game.systems

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.annotations.Wire
import spacebros.server.game.ConnectionHub
import spacebros.server.game.IntentQueue
import spacebros.server.game.components.BehaviorComponent

/***
 * Executes behaviors attached to an entity every tick if necessaery
 */
class BehaviorSystem(val intentQueue: IntentQueue) : BaseEntitySystem(aspects) {
    companion object {
        val aspects: Aspect.Builder = Aspect.all(BehaviorComponent::class.java)
    }
    // Drain this many intents every tick
    // TODO: what's the action/performance penalty for players on an active server?
    val drainEveryTick = 100

    lateinit var behaviorMapper: ComponentMapper<BehaviorComponent>

    override fun processSystem() {
        val actives = subscription.entities
        val intents = intentQueue.take(drainEveryTick)
        intents.filter { actives.contains(it.targetEntityId) }.forEach { intent ->
            val behaviorComponent = behaviorMapper.get(intent.targetEntityId)
            behaviorComponent.behaviors.forEach { behavior ->
                behavior.execute(intent)
            }

        }
    }

    fun process(entityId: Int) {
        // behaviors should be executed inline with the game system so that all reactions execute
        // in game time
        // ergo, any potential actions a user wants to perform that would in theory trigger
        // a behavior should then also be present within the game system
        // ...
        // the last html5 prototype used a concept called actions and intentions to insert data into the ecs
        // entities exposed actions that they understood, like, "use" and "open" and "close" and "hijack"
        // the client-side UI picked this up and then would slot the selected action into a message
        // and send it to the server.
        // the server would then see the message and apply it to all behaviors attached to an entity.
        // this allowed for overrides or side-effects:
        //  - hacked doors would have a "boobytrap" behavior that would listen for (or maybe even intercept)
        //    an action like "open", and apply an effect to the player.
        // in order ot keep that kind of behavior i need a similar system here.
        // ...
        // for now should just respond to generic actions like "use" ?
        // How are intents applied?
        // Old System:
        //  - sent "Interact With" request, with entity id and action.
        //  - seemed to use an "intent system" instead of a behavior system
        //    made for duplicate logic iirc
        //    everything had to be hard-coded
        //  - intents had:
        //    invoking entity, target entity, and with entity
        //      with entity: a proxy entity.
        //      eg if Player 1 tried to "open" a door with a crowbar, with === crowbar
        //  Intent system was basically an event-driven system that worked out of band of the game tick
        //  probably why i dropped it
        // Basically this should be the "intents" system, cycles through intents every game tick,
        // applies them to behaviors which then do custom stuff.
        // THINK REAL HARD ABOUT IT MAN YOU DON'T WANT TO MAKE "THE MISTAKE"
        // "THE MISTAKE"
        // Should intents be queued up _outside_ the world system, but then flushed _inside?_
        // IntentQueue(listOf(Intent)) => ArrayList?
        // BehaviorSystem(intentQueue) => drains queue via iterator, continues
        // ... Seems Legit ...
        // Deterministic event system in other words. Synchronized with the
        // Do I queue intents by affected entity or do I just let'em be giant pillocks?
        // I have to drain the queue in case some idiot sends a bunch of requests for a nonexistent entity
        // so fuck it
    }
}
