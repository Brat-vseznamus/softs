package lab1.lru

interface LRUCache<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V)
}