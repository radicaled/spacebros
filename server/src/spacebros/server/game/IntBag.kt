package spacebros.server.game

import com.artemis.utils.IntBag

/**
 * Return all ACTIVE Ints in an IntBag (data returns everything, including old/expired/etc)
 */
fun IntBag.active() = (0 until size()).map { data[it] }
