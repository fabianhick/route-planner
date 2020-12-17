import com.sun.source.tree.Tree
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
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
            return Position( latitude*PI/180.0, longitude* PI/180.0)
        }
    }
    data class Node(var offset: Int, val position: Position, val id: Int, var distance: Double = Double.POSITIVE_INFINITY) : Comparable<Node> {
        override public fun compareTo(other: Node) : Int {
            if(this.distance == other.distance) {
                return this.id.compareTo(other.id)
            } else {
                return this.distance.compareTo(other.distance)
            }
        }
        /*fun getEdges(edges: ArrayList<Edge>) : ArrayList<Edge> {
            var set: ArrayList<Edge> = ArrayList()
            var index = offset
            if(index > edges.size -1)
                return set
            while(edges[index].src == id) {
                set.add(edges[index])
                index++
                if(index > edges.size-1)
                    break
            }
            return set
        }*/
    }

    data class Edge(val src: Int, val target: Int, val weight: Int) {}
    var nodes: ArrayList<Node> = ArrayList()
    var edges: ArrayList<Edge> = ArrayList()
   // var shortestPath: ArrayList<Int> = ArrayList()

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
            } else if (numEdges == 0) {
                numEdges = it.toIntOrNull() ?: 0
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
                        nodes.add(Node(Int.MIN_VALUE, Position(latitude = get(2).toDouble(), longitude = get(3).toDouble()), id = count))
                    }
                } else {
                    with(it.split(" ")) {
                        val source: Int = get(0).toInt()
                        //var weight: Int = get(2).toInt();
                        //if(weight < 0)
                        //    weight = Int.MAX_VALUE

                        edges.add(Edge(source, target = get(1).toInt(), weight = get(2).toInt()))
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
        System.gc()
    }

    fun distanceTo(source: Int, destination: Int) {
        TODO()
    }

    /*fun oneToAll(start: Int = 0) : ArrayList<Int> {
        val finished: MutableSet<Node> = mutableSetOf() // a subset of vertices, for which we know the true distance

        val delta = nodes.map { it to Int.MAX_VALUE }.toMap().toMutableMap()
        val previous: MutableMap<Node, Node?> = nodes.map{it to null}.toMap().toMutableMap()
        delta[nodes[start]] = 0


        while (finished != nodes.toSet()) {
            val node: Node = delta
                    .filter { !finished.contains(it.key) }
                    .minBy { it.value }!!
                    .key

            node.getEdges(edges).forEach { edge ->
               // println(edge.toString())
                val newPath = delta.getValue(node) + edge.weight
                val neighbor = nodes[edge.target];
                if (newPath < delta.getValue(neighbor)) {
                    delta[neighbor] = newPath
                    previous[neighbor] = node
                }
            }

            finished.add(node)
        }
        var distance: ArrayList<Int> = ArrayList()
        nodes.forEach() {
            distance.add(delta.get(it) ?: Int.MAX_VALUE)
        }
        //return previous.toMap()
        return distance;
    }

fun oneToAll(start: Int = 0) : Array<Double> {
        val queue: DijkstraQueue<Int> = DijkstraQueue()
        val distance: Array<Double> = Array(numNodes) { i -> Double.POSITIVE_INFINITY }
        val previous: IntArray = IntArray(numNodes) { i -> -1}

        distance[start] = 0.0
        distance.forEachIndexed {
            id, distance ->
            queue.addWithPriority(id, distance)
        }

        while(!queue.isEmpty()) {
            val n = queue.getNextItem()
            var index: Int = nodes[n].offset
            while(edges[index].src == n) {
                val it = edges[index]
                val alt: Double = distance[it.src] + it.weight
                if (alt < distance[it.target]) {
                    distance[it.target] = alt
                    previous[it.target] = it.src
                    queue.decreasePriority(item = it.target, priority = alt)
                }
                index++
                if(index > edges.size -1)
                    break
            }
        }

        return distance

    }
*/
   /* class NodeComparator : Comparator<Node> {
        override fun compare(o1: Node, o2: Node): Int {
            if(o1.distance == o2.distance) {
                return o1.id.compareTo(o2.id)
            } else {
                return o1.distance.compareTo(o2.distance)
            }
        }
    }*/

    fun oneToAll(start: Int = 0) : Array<Double> {
        val queue: TreeSet<Node> = TreeSet()
        val distance: Array<Double> = Array(numNodes) { i -> Double.POSITIVE_INFINITY }
        val previous: IntArray = IntArray(numNodes) { i -> -1}
        nodes[start].distance = 0.0
        distance[start] = 0.0
        //nodes.forEach({
        //    queue.add(it)
        //})
        queue.add(nodes[start])
        while(queue.isNotEmpty()) {
            val u = queue.pollFirst()
            var index: Int = u.offset
            if(index == Int.MIN_VALUE)
                continue
            //println(edges[index].src == u.id)
            //println("src: ${edges[index].src}; id: ${u.id}")
            while(edges[index].src == u.id) {
                val edge = edges[index]
                val v = nodes[edge.target]
                val alt: Double = u.distance + edge.weight
                if (alt < distance[v.id]) {
                    distance[v.id] = alt

                    previous[v.id] = u.id
                    queue.remove(v)
                    v.distance = alt
                    queue.add(v)
                }
                index++
                if(index > edges.size -1) {
                    // TODO: println("[!!!!!] Skipped, due to index overlap!")
                    break
                }

            }
        }
        var time = measureTimeMillis {
            nodes.forEach { it.distance = Double.POSITIVE_INFINITY }

        }
        println("Reset took ${time/1000} seconds.")
        return distance

    }

    fun getNode(id: Int = 0): Node {
        return nodes.get(id).copy()
    }

    /**
     * Returns the nearest node according to the passed position
     */
    fun findNearestNode(target: Position): Node {
        return nodes.minBy { it.position.distance(target) } ?: throw IllegalStateException("Graph shouldn't be empty") //TODO parallel
    }


    fun fileChallenge(input: String, output: String) {
        var result: Array<Double>? = null
        var currentNode: Int = -1;

        File(input).forEachLine {
            val challengeNodes = it.split(" ")
            if(challengeNodes[0].toInt() != currentNode) {
                currentNode = challengeNodes[0].toInt()
                println("Calculating dijkstra for node $currentNode...")
                result = null
                System.gc()
                var time = measureTimeMillis {
                    result = oneToAll(challengeNodes[0].toInt())
                }
                println("Finished calculating dijkstra for node #$currentNode after ${time/1000} seconds.")
            } else {
                val value = result!!.get(challengeNodes[1].toInt())
                var text = value.toString().removeSuffix(".0")
                if(value == Double.POSITIVE_INFINITY)
                    text = "-1"
                File(output).appendText(text + "\n")
            }
        }
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