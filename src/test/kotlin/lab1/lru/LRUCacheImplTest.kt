package lab1.lru

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.AssertionError
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

internal class LRUCacheImplTest {

    @Test
    fun `put value and check it is stored`() {
        val lruCache: LRUCache<Int, Int> = LRUCacheImpl(1)
        val kvs = randomKeyValueSet(1)

        lruCache.put(kvs[0].first, kvs[0].second)

        assertEquals(lruCache.get(kvs[0].first), kvs[0].second)
    }


    @Test
    fun `put more than capacity number values and check forgetting first stored`() {
        val lruCache: LRUCache<Int, Int> = LRUCacheImpl(1)
        val kvs = randomKeyValueSet(2)

        lruCache.put(kvs[0].first, kvs[0].second)
        lruCache.put(kvs[1].first, kvs[1].second)

        assertNull(lruCache.get(kvs[0].first))
    }

    @Test
    fun `update value for stored key`() {
        val lruCache: LRUCache<Int, Int> = LRUCacheImpl(1)
        val kvs = randomKeyValueSet(2)

        lruCache.put(kvs[0].first, kvs[0].second)
        lruCache.put(kvs[0].first, kvs[1].second)

        assertEquals(lruCache.get(kvs[0].first), kvs[1].second)
    }

    @Test
    fun `test get returns null for non-stored keys`() {
        val lruCache: LRUCache<Int, Int> = LRUCacheImpl(1)
        val kvs = randomKeyValueSet(2)

        lruCache.put(kvs[0].first, kvs[0].second)

        assertNull(lruCache.get(kvs[1].first))
    }

    @Test
    fun `test pushing forward more relevant values`() {
        val lruCache: LRUCache<Int, Int> = LRUCacheImpl(2)
        val kvs = randomKeyValueSet(3)

        lruCache.put(kvs[0].first, kvs[0].second)
        lruCache.put(kvs[1].first, kvs[1].second)

        // increase priority of 1-st key
        lruCache.get(kvs[1].first)

        lruCache.put(kvs[2].first, kvs[2].second)

        assertNull(lruCache.get(kvs[0].first))
        assertEquals(lruCache.get(kvs[1].first), kvs[1].second)
        assertEquals(lruCache.get(kvs[2].first), kvs[2].second)
    }

    @Test
    fun `test that initializing only positive sized caches`() {
        assertThrows<AssertionError> {
            LRUCacheImpl<Int, Int>(-1)
        }

        assertThrows<AssertionError> {
            LRUCacheImpl<Int, Int>(0)
        }

        assertDoesNotThrow {
            LRUCacheImpl<Int, Int>(1)
        }
    }


    private fun randomKeyValueSet(size: Int): List<Pair<Int, Int>> {
        val keySet: MutableSet<Int> = HashSet()
        val valueSet: MutableSet<Int> = HashSet()
        val result: MutableList<Pair<Int, Int>> = ArrayList(size)

        repeat(size) {
            var k = randomInt()
            while (k in keySet) {
                k = randomInt()
            }
            var v = randomInt()
            while (v in valueSet) {
                v = randomInt()
            }
            keySet.add(k)
            valueSet.add(v)
            result.add(k to v)
        }

        return result
    }

    private fun randomInt(): Int {
        return Random().nextInt()
    }


}