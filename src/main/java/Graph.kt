/*
 * Copyright (c) 2020.
 * Fabian Hick
 */

import java.io.File
import java.util.*
import java.util.function.Function
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.*
import kotlin.system.measureTimeMillis

class Graph(val fileName: String) {


    data class Position(val latitude: Double, val longitude: Double) {
        /**
         * Calculate Great-circle distance of two positions
         * https://en.wikipedia.org/wiki/Great-circle_distance
         *
         * @input point : Position of the other point
         */
        fun distance(point: Position): Double {
            val origin = this.radian()
            val target = point.radian()
            val angle = acos(
                sin(origin.latitude) * sin(target.latitude)
                        + cos(origin.latitude) * cos(target.latitude) * cos(target.longitude - origin.longitude)
            )
            return abs(6335.439 * angle)
        }

        /**
         * Returns the current position converted to radian (standard storage would be degree)
         */
        fun radian(): Position {
            if (longitude < 2 * PI)
                return this
            return Position(latitude * PI / 180.0, longitude * PI / 180.0)
        }
    }

    data class Node(var offset: Int, val position: Position, val id: Int)
    data class Edge(val src: Int, val target: Int, val weight: Int)

    var nodes: Array<Node> = Array(1) { Node(0, Position(0.0, 0.0), 0) }
    var edges: Array<Edge> = Array(1) { Edge(0, 0, 0) }

    var numNodes = 0
    var numEdges = 0

    init {
        loadFile()
    }

    private fun loadFile() {
        println("Loading file: $fileName")
        var count = 0
        File(fileName).forEachLine {
            if (it.startsWith("#")) {
                //println(it.removePrefix("# ").replace(" ", "").split(":"))
            } else if (numNodes == 0) {
                numNodes = it.toIntOrNull() ?: 0
                val temp = Node(0, Position(.0, .0), 0)
                nodes = Array(numNodes) { temp }
            } else if (numEdges == 0) {
                numEdges = it.toIntOrNull() ?: 0
                val temp = Edge(0, 0, 0)
                edges = Array(numEdges) { temp }
            } else {
                when (count) {
                    0 -> println("Starting to read nodes.")
                    numEdges -> println("Finished reading $numNodes nodes.\nStarting to read edges.")
                }
                if (count < numNodes) {
                    with(it.split(" ")) {
                        nodes[count] = Node(
                            Int.MIN_VALUE,
                            Position(latitude = get(2).toDouble(), longitude = get(3).toDouble()),
                            id = count
                        )
                    }
                } else {
                    with(it.split(" ")) {
                        val source: Int = get(0).toInt()

                        edges[count - numNodes] = Edge(source, target = get(1).toInt(), weight = get(2).toInt())
                        if (nodes.get(source).offset == Int.MIN_VALUE)
                            nodes.get(source).offset = count - numNodes
                    }
                }
                count++
            }

        }
        println("Nodes: $numNodes; Loaded: ${nodes.size}")
        println("Edges: $numEdges; Loaded: ${edges.size}")
        println("Entries: $count")
    }

    /**
     * Internal helper.
     * Compares the distance of two nodes for the Heap.
     */
    class NodeComparator(ref: distance) : Comparator<Int> {
        private var ref = ref
        override fun compare(o1: Int, o2: Int): Int {
            if (ref.data[o1] == ref.data[o2]) {
                return o1.compareTo(o2)
            } else {
                return ref.data[o1].compareTo(ref.data[o2])
            }
        }
    }

    // shared data class for comparator and dijkstra
    data class distance(var data: IntArray)

    /**
     * Calculates the DijkstraPath from a given start node to a target node.
     * If no target node is provided, this function will calculate a one-to-all DijkstraPath.
     *
     * @param start: The starting node
     * @param target: Another node (default: -1 for one-to-all Dijkstra)
     */
    fun calculateDijkstra(start: Int = 0, target: Int = -1): DijkstraPath {
        val distance: distance = distance(IntArray(numNodes) { Int.MAX_VALUE })
        val queue: Heap = Heap(numNodes, NodeComparator(distance))
        val previous: IntArray = IntArray(numNodes) { i -> -1 }
        distance.data[start] = 0
        queue.insert(start)

        if (target < 0) {
            while (queue.isNotEmpty()) {
                process(queue, distance, previous)
            }
        } else {
            while (queue.peek() != target && queue.isNotEmpty()) {
                process(queue, distance, previous)
            }
        }

        return DijkstraPath(distance.data, previous, nodes)
    }

    /**
     * Performs a Dijkstra algorithm iteration
     */
    private inline fun process(queue: Heap, distance: distance, previous: IntArray) {
        val u = queue.poll()
        var index: Int = nodes[u].offset

        // Continue if node doesn't have any edges
        if (index == Int.MIN_VALUE)
            return
        while (edges[index].src == u) {
            val edge = edges[index]
            val v = edge.target
            val alt: Int = distance.data[u] + edge.weight
            if (alt < distance.data[v]) {
                distance.data[v] = alt

                previous[v] = u

                if (!queue.contains(v)) {
                    queue.insert(v)
                } else {
                    queue.decreaseKey(v)
                }
            }
            index++
            if (index > edges.size - 1) {
                // TODO: println("[!!!!!] Skipped, due to index overlap!")
                break
            }

        }
    }


    data class DijkstraPath(val distance: IntArray, val way: IntArray, val nodes: Array<Node>) {
        fun getPathAsArrayBackwards(node: Int): LinkedList<Array<Double>> {
            val result: LinkedList<Array<Double>> = LinkedList()
            var id = node
            while (id != -1) {
                val s: Array<Double> = Array(2) { nodes[id].position.latitude }
                s[1] = nodes[id].position.longitude
                result.add(s)
                id = way[id]
            }
            return result
        }

        fun getPathBackwards(node: Int): LinkedList<Node> {
            val result: LinkedList<Node> = LinkedList()
            var id = node
            while (id != -1) {
                result.add(nodes[id])
                id = way[id]
            }
            return result
        }

        fun getPath(node: Int): ArrayList<Node> {
            val queue: Deque<Node> = LinkedList()
            var id = node
            while (id != -1) {
                queue.addFirst(nodes[id])
                id = way[id]
            }

            val path: ArrayList<Node> = ArrayList()
            queue.iterator().forEach {
                path.add(it)
            }
            return path
        }
    }

    fun getNode(id: Int = 0): Node {
        return nodes.get(id).copy()
    }

    fun <T> Optional<T>.unwrap(): T? = orElse(null)

    /**
     * Returns the nearest node according to the passed position
     */
    fun findNearestNode(target: Position): Node {
        var nodes: List<Node> = nodes.toList()
        return nodes.parallelStream()
            .min(Comparator.comparing(Function<Node, Double> { a: Node -> a.position.distance(target) })).unwrap()!!
        //return nodes.minBy { it.position.distance(target) } ?: throw IllegalStateException("Graph shouldn't be empty") //TODO parallel
    }


    fun fileChallenge(input: String, output: String, optimized: Boolean = false) {
        var result: DijkstraPath? = null
        var currentNode: Int = -1

        if (!optimized) {
            File(input).forEachLine {
                val challengeNodes = it.trim().split(" ")
                if (challengeNodes[0].toInt() != currentNode) {
                    currentNode = challengeNodes[0].toInt()
                    println("Calculating dijkstra for node $currentNode...")
                    result = null
                    var time = measureTimeMillis {
                        result = calculateDijkstra(challengeNodes[0].toInt())
                    }
                    println("Finished calculating dijkstra for node #$currentNode after ${time / 1000} seconds.")
                }
                writeResult(result, challengeNodes, output)
            }
        } else {
            var challengeNodes: List<String> = ArrayList<String>()
            var counter: Int = 0
            var g_time: Long = 0

            File(input).forEachLine {
                challengeNodes = it.trim().split(" ")
                val currentNode = challengeNodes[0].toInt()
                println("Calculating dijkstra for way from $currentNode to ${challengeNodes[1].toInt()}...")
                var time = measureTimeMillis {
                    result = calculateDijkstra(challengeNodes[0].toInt(), challengeNodes[1].toInt())
                }
                writeResult(result, challengeNodes, output)
                println("Finished calculating dijkstra for node #$currentNode after ${time / 1000} seconds.")
                counter++
                g_time += time
            }
            println("Finished calculating challenge. Average time for each line: ${(g_time / 1000) / counter}s")

        }
    }

    private fun writeResult(result: DijkstraPath?, challengeNodes: List<String>, output: String) {
        val value = result!!.distance.get(challengeNodes[1].toInt())
        var text = value.toString().removeSuffix(".0")
        if (value == Int.MAX_VALUE)
            text = "-1"
        File(output).appendText(text + "\n")
    }


    data class Heap(val size: Int, val comparator: NodeComparator) {
        var currentSize: Int = 0
        val heap: IntArray = IntArray(size + 1)
        val map: IntArray = IntArray(size) { -1 }

        fun contains(node: Int): Boolean {
            return map[node] != -1
        }

        fun isNotEmpty() = currentSize > 0

        inline fun swap(first: Int, second: Int) {
            val i = map[first]
            val j = map[second]

            heap[j] = first
            heap[i] = second

            map[first] = j
            map[second] = i
        }

        fun insert(key: Int) {
            currentSize++
            heap[currentSize] = key
            map[key] = currentSize
            var i = currentSize
            while (i > 1) {
                if (comparator.compare(heap[i], heap[i / 2]) == -1) {
                    swap(heap[i], heap[i / 2])
                    i /= 2
                } else {
                    break
                }
            }
        }

        fun peek(): Int {
            return heap[1]
        }

        fun poll(): Int {
            val answer = heap[1]
            map[answer] = -1
            heap[1] = heap[currentSize]

            map[heap[1]] = 1

            currentSize--
            var i = 1

            while (2 * i <= currentSize) {
                if ((2 * i + 1 > currentSize) || (comparator.compare(heap[2 * i], heap[2 * i + 1]) == -1)) {
                    if (comparator.compare(heap[i], heap[2 * i]) == 1) {
                        swap(heap[i], heap[2 * i])
                        i *= 2
                    } else {
                        break
                    }
                } else {
                    if (comparator.compare(heap[i], heap[2 * i + 1]) == 1) {
                        swap(heap[i], heap[2 * i + 1])
                        i = 2 * i + 1
                    } else {
                        break
                    }
                }
            }
            return answer
        }

        fun decreaseKey(key: Int) {
            var i = map[key]
            while (i > 1) {
                if (comparator.compare(heap[i], heap[i / 2]) == -1) {
                    swap(heap[i], heap[i / 2])
                    i /= 2
                } else {
                    break
                }
            }
        }
    }

}

