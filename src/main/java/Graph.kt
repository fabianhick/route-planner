/*
 * Copyright (c) 2020.
 * Fabian Hick
 */

import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.function.Function
import kotlin.collections.ArrayList
import kotlin.math.*
import kotlin.system.measureTimeMillis

class Graph (val fileName: String) {


    data class Position(val latitude: Double, val longitude: Double) {
        /**
         * Calculate Great-circle distance of two positions
         * https://en.wikipedia.org/wiki/Great-circle_distance
         *
         * @input point : Position of the other point
         */
        fun distance(point: Position) : Double {
            val origin = this.radian()
            val target = point.radian()
            val angle = acos(sin(origin.latitude)*sin(target.latitude)
                    +cos(origin.latitude)*cos(target.latitude)*cos(target.longitude-origin.longitude))
            return abs(6335.439*angle)
        }

        /**
         * Returns the current position converted to radian (standard storage would be degree)
         */
        fun radian() : Position {
            if(longitude < 2*PI)
                return this
            return Position(latitude * PI / 180.0, longitude * PI / 180.0)
        }
    }
    data class Node(var offset: Int, val position: Position, val id: Int)
    data class Edge(val src: Int, val target: Int, val weight: Int) {}
    var nodes: Array<Node> = Array(1) {Node(0, Position(0.0,0.0),0)} ;
    var edges: Array<Edge> = Array(1) {Edge(0,0,0)};

    var numNodes = 0
        get() = field
    var numEdges = 0
        get() = field

    init {
        loadFile()
    }

    fun loadFile() {
        println("Loading file: $fileName")
        var count = 0
        File(fileName).forEachLine {
            if (it.startsWith("#")) {
                //println(it.removePrefix("# ").replace(" ", "").split(":"))
            } else if (numNodes == 0) {
                numNodes = it.toIntOrNull() ?: 0
                val temp = Node(0, Position(.0,.0),0)
                nodes = Array(numNodes) { temp }
            } else if (numEdges == 0) {
                numEdges = it.toIntOrNull() ?: 0
                val temp = Edge(0,0,0)
                edges = Array(numEdges) { temp }
            } else {
                //if(count < 5)
                //    println(it)
                when (count) {
                    0 -> println("Starting to read nodes.")
                    numEdges -> println("Finished reading $numNodes nodes.\nStarting to read edges.")
                    else -> null
                }
                if (count < numNodes) {
                    with(it.split(" ")) {
                        nodes!![count] = Node(Int.MIN_VALUE, Position(latitude = get(2).toDouble(), longitude = get(3).toDouble()), id = count)
                    }
                } else {
                    with(it.split(" ")) {
                        val source: Int = get(0).toInt()
                        //var weight: Int = get(2).toInt();
                        //if(weight < 0)
                        //    weight = Int.MAX_VALUE

                        edges[count-numNodes] = Edge(source, target = get(1).toInt(), weight = get(2).toInt())
                        if (nodes.get(source).offset == Int.MIN_VALUE)
                            nodes.get(source).offset = count - numNodes;
                    }
                }
                //if(count > numNodes-5 && count < numNodes + 1)
                //    println(it)
                count++
            }

        }
        println("Nodes: $numNodes; Loaded: ${nodes.size}")
        println("Edges: $numEdges; Loaded: ${edges.size}")
        println("Entries: $count")
       // System.gc()
    }

    fun distanceTo(start: Int = 0, target: Int = 0) : DijkstraPath {

        class NodeComparator(ref : distance) : Comparator<Node> {
            var ref = ref
            override fun compare(o1: Node, o2: Node): Int {
                if(ref.data[o1.id] == ref.data[o2.id]) {
                    return o1.id.compareTo(o2.id)
                } else {
                    return ref.data[o1.id].compareTo(ref.data[o2.id])
                }
            }
        }
        var distance: distance = distance(Array(numNodes) { i -> Double.POSITIVE_INFINITY })
        val queue: TreeSet<Node> = TreeSet(NodeComparator(distance))
        val previous: IntArray = IntArray(numNodes) { i -> -1}
        distance.data[start] = 0.0

        queue.add(nodes[start])
        while(queue.isNotEmpty()) {
            val u = queue.pollFirst()
            if(u == nodes[target]) {
                break;
            }
            var index: Int = u.offset

            // Continue if node doesn't have any edges
            if(index == Int.MIN_VALUE)
                continue
            //println(edges[index].src == u.id)
            //println("src: ${edges[index].src}; id: ${u.id}")
            while(edges[index].src == u.id) {
                val edge = edges[index]
                val v = nodes[edge.target]
                val alt: Double = distance.data[u.id] + edge.weight
                if (alt < distance.data[v.id]) {
                    distance.data[v.id] = alt

                    previous[v.id] = u.id
                    queue.remove(v)
                    //   v.distance = alt
                    queue.add(v)
                }
                index++
                if(index > edges.size -1) {
                    // TODO: println("[!!!!!] Skipped, due to index overlap!")
                    break
                }
            }
        }
        //return distance.data
        return DijkstraPath(distance.data, previous, nodes)
    }

    // shared data class for comparator and dijkstra
    data class distance(var data: Array<Double>)
    fun oneToAll(start: Int = 0) : DijkstraPath {

        class NodeComparator(ref : distance) : Comparator<Node> {
            var ref = ref
            override fun compare(o1: Node, o2: Node): Int {
                if(ref.data[o1.id] == ref.data[o2.id]) {
                    return o1.id.compareTo(o2.id)
                } else {
                    return ref.data[o1.id].compareTo(ref.data[o2.id])
                }
            }
        }

        var distance: distance = distance(Array(numNodes) { i -> Double.POSITIVE_INFINITY })
        val queue: TreeSet<Node> = TreeSet(NodeComparator(distance))
        val previous: IntArray = IntArray(numNodes) { i -> -1}
        distance.data[start] = 0.0

        queue.add(nodes[start])

        while(queue.isNotEmpty()) {
            val u = queue.pollFirst()
            var index: Int = u.offset

            // Continue if node doesn't have any edges
            if(index == Int.MIN_VALUE)
                continue
            //println(edges[index].src == u.id)
            //println("src: ${edges[index].src}; id: ${u.id}")
            while(edges[index].src == u.id) {
                val edge = edges[index]
                val v = nodes[edge.target]
                val alt: Double = distance.data[u.id] + edge.weight
                if (alt < distance.data[v.id]) {
                    distance.data[v.id] = alt

                    previous[v.id] = u.id
                    queue.remove(v)
                 //   v.distance = alt
                    queue.add(v)
                }
                index++
                if(index > edges.size -1) {
                    // TODO: println("[!!!!!] Skipped, due to index overlap!")
                    break
                }

            }
        }
        //return distance.data
        return DijkstraPath(distance.data, previous, nodes)
    }
    data class DijkstraPath(val distance: Array<Double>, val way: IntArray, val nodes: Array<Node>) {
        fun getPathAsArrayBackwards (node: Int) : LinkedList<Array<Double>> {
            val result: LinkedList<Array<Double>> = LinkedList()
            var id = node
            while(id != -1) {
                val s: Array<Double> = Array(2 ) { nodes[id].position.latitude}
                s[1] = nodes[id].position.longitude
                result.add(s)
                id = way[id]
            }
            return result
        }

        fun getPathBackwards(node: Int) : LinkedList<Node> {
            val result: LinkedList<Node> = LinkedList()
            var id = node
            while(true) {
                result.add(nodes[id])
                id = way[id]
                if(id == -1)
                    break;
            }
            return result
        }

        fun getPath(node: Int) : ArrayList<Node> {
            val queue : Deque<Node> = LinkedList()
            var id = node;
            while(true) {
                queue.addFirst(nodes[id])
                id = way[id]
                if(id == -1)
                    break;
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
        return nodes.parallelStream().min(Comparator.comparing(Function<Node, Double> { a: Node -> a.position.distance(target) })).unwrap()!!
        //return nodes.minBy { it.position.distance(target) } ?: throw IllegalStateException("Graph shouldn't be empty") //TODO parallel
    }


    fun fileChallenge(input: String, output: String) {
        var result: DijkstraPath? = null
        var currentNode: Int = -1;

        File(input).forEachLine {
            val challengeNodes = it.trim().split(" ")
            if(challengeNodes[0].toInt() != currentNode) {
                currentNode = challengeNodes[0].toInt()
                println("Calculating dijkstra for node $currentNode...")
                result = null
                //System.gc()
                var time = measureTimeMillis {
                    result = oneToAll(challengeNodes[0].toInt())
                }
                println("Finished calculating dijkstra for node #$currentNode after ${time/1000} seconds.")
            }
            val value = result!!.distance.get(challengeNodes[1].toInt())
            var text = value.toString().removeSuffix(".0")
            if(value == Double.POSITIVE_INFINITY)
                text = "-1"
            File(output).appendText(text + "\n")
        }
    }

    fun fileChallengeOptimized(input: String, output: String) {
        var result: DijkstraPath? = null
        var challengeNodes: List<String> = ArrayList<String>()
        var counter: Int = 0
        var g_time: Long = 0
        File(input).forEachLine {
            challengeNodes = it.trim().split(" ")
            val currentNode = challengeNodes[0].toInt()
            println("Calculating dijkstra for way from $currentNode to ${challengeNodes[1].toInt()}...")
            var time = measureTimeMillis {
                result = distanceTo(challengeNodes[0].toInt(), challengeNodes[1].toInt())
            }
            val value = result!!.distance.get(challengeNodes[1].toInt())
            var text = value.toString().removeSuffix(".0")
            if(value == Double.POSITIVE_INFINITY)
                text = "-1"
            File(output).appendText(text + "\n")
            println("Finished calculating dijkstra for node #$currentNode after ${time/1000} seconds.")
            counter++;
            g_time += time
        }
        println("Finished calculating challenge. Average time for each line: ${(g_time/1000)/counter}s")
    }

}
/*
class DijkstraQueue<T>()  {
    val map: MutableMap<Double, Queue<T>> = mutableMapOf()
    val reverseMap: MutableMap<T, Double> = mutableMapOf()
    var size: Int = 0
    init {
        var size: Int = 0
    }
    fun addWithPriority(item: T, priority: Double) {
        val valueQueue: Queue<T> = map.get(priority) ?: LinkedList()
        valueQueue.add(item)
        map.putIfAbsent(priority, valueQueue)
        reverseMap.putIfAbsent(item, priority)
        size++
    }
    fun isEmpty() : Boolean {
        return size == 0
    }
    fun getNextItem() : T {
        //println(size)
        val priority: Double = map.minBy { it.key }!!.key ?: throw NoSuchElementException("Map is empty")
        val element: T = map.get(priority)!!.poll();
        removeItem(element)
        return element
    }

    fun decreasePriority(item: T, priority: Double) {
        removeItem(item)
        addWithPriority(item, priority)
    }

    private fun removeItem(item: T) {
        println(item.toString())
        println(reverseMap.toString())
        val priority: Double = reverseMap[item]!!
        map.get(priority)?.remove(item)
        if(map.get(priority)!!.isEmpty())
            map.remove(priority)
        reverseMap.remove(item)
        size--
    }
*/