package lru

class LRUCacheImpl<K, V>(private val capacity: Int): LRUCache<K, V> {
    private val cache: MutableMap<K, Node<K, V>>
    private val order: DoublyLinkedList<V>

    init {
        assert(capacity > 0)
        cache = HashMap(capacity)
        order = DoublyLinkedList()
    }

    override fun get(key: K): V? {
        return if (key in cache) {
            val node = cache[key]!!
            order.remove(node)
            order.pushHead(node)
            node.value
        } else {
            null
        }
    }

    override fun put(key: K, value: V) {
        if (key in cache) {
            cache[key]!!.value = value
        } else {
            var newNode = Node(key, value)
            if (capacity == cache.size) {
                val tail = order.popTail()
                cache.remove(tail.key)
            }
            newNode = order.pushHead(newNode)
            cache[key] = newNode
        }
    }



    private inner class Node<K, V>(var key: K, var value: V) {
        var prev: Node<K, V>? = null
        var next: Node<K, V>? = null
    }

    private inner class DoublyLinkedList<V> {
        private var head: Node<K, V>? = null
        private var tail: Node<K, V>? = null
        private var size: Int = 0

        fun pushHead(newNode: Node<K, V>): Node<K, V> {
            newNode.prev = null
            when(size) {
                0 -> {
                    tail = newNode
                }
                1 -> {
                    tail!!.prev = newNode
                    newNode.next = tail
                }
                else -> {
                    newNode.next = head
                    head!!.prev = newNode
                }
            }
            head = newNode
            size++
            return newNode
        }

        fun pushTail(newNode: Node<K, V>): Node<K, V> {
            newNode.next = null
            when(size) {
                0 -> {
                    head = newNode
                }
                1 -> {
                    head!!.next = newNode
                    newNode.prev = head
                }
                else -> {
                    newNode.prev = tail
                    tail!!.next = newNode
                }
            }
            tail = newNode
            size++
            return newNode
        }

        fun popHead(): Node<K, V> {
            assert(size > 0)
            val result = head!!
            when(size) {
                1 -> {
                    head = null
                    tail = null
                }
                else -> {
                    head = head!!.next
                    head!!.prev = null
                }
            }
            result.next = null
            size--
            return result
        }


        fun popTail(): Node<K, V> {
            assert(size > 0)
            val result = tail!!
            when(size) {
                1 -> {
                    head = null
                    tail = null
                }
                else -> {
                    tail = tail!!.prev
                    tail!!.next = null
                }
            }
            result.prev = null
            size--
            return result
        }

        fun remove(node: Node<K, V>) {
            when (size) {
                1 -> {
                    head = null
                    tail = null
                }
                else -> {
                    if (size == 2) {
                        if (node.next == null) {
                            tail = head
                        } else {
                            head = tail
                        }
                    }
                    node.next?.prev = node.prev
                    node.prev?.next = node.next
                }
            }
            size--
        }
    }
}