import kotlin.system.measureTimeMillis

var time: Long = 0

object Global {
    lateinit var graph: Graph
}

fun main(args: Array<String>) {
    time = measureTimeMillis {
        Global.graph = Graph(args[0])
    }
    menu()
}

fun menu() {
    println("Everything finished!")
    println("Took ${time / 1000f} seconds.")
    while (true) {
        println("Menu: ")
        println("1. Using challenge files (one-to-all)")
        println("2. Using challenge files (optimized)")
        println("3. One-to-all")
        println("4. Nearest node of position")
        println("5. Inspect a node")
        println("6. Start webserver")
        print("Please choose: ")
        val selection = readLine()?.toIntOrNull() ?: -1
        try {
            when (selection) {
                1 -> testFiles()
                2 -> testFiles(optimized = true)
                3 -> oneToAll()
                4 -> nodePosition()
                5 -> node()
                6 -> startServer()
                else -> println("This input is unrecognized. Please try again.")
            }
        } catch (t: Throwable) {
            println("An error occurred: ${t.message}")
            t.printStackTrace()
            println("Recovering...")
        }
    }
}

fun node() {
    print("Which node would you like to inspect? ")
    val source = readLine()?.toIntOrNull() ?: 0
    println("You requested node $source.")
    println(Global.graph.getNode(source))
}

fun nodePosition() {
    println("You selected nearest node:")
    print("Latitude: ")
    val latitude: Double = readLine()?.toDoubleOrNull() ?: 0.0
    print("Longitude: ")
    val longitude = readLine()?.toDoubleOrNull() ?: 0.0

    println(Global.graph.findNearestNode(Graph.Position(latitude = latitude, longitude = longitude)))

}

fun oneToAll() {
    print("From which node would you like to know the distance? ")
    val source = readLine()?.toIntOrNull() ?: 0
    var path: Graph.DijkstraPath? = null
    println("Calculating one-to-all dijkstra... please wait.")
    val time = measureTimeMillis {
        path = Global.graph.calculateDijkstra(source)
    }
    val distance = path!!.distance
    println("Calculated one-to-all dijkstra in ${time / 1000f} seconds.")
    println("Calculated ${distance.size} entries of ${Global.graph.numNodes}")
    while (true) {
        print("To which node would you like to know the distance? ")
        val target = readLine()?.toIntOrNull() ?: 0
        if (target <= -2 || target > distance.size - 1) {
            println("Invalid input. Try again!")
            continue
        }
        if (target == -1)
            break

        println("Your distance is: ${distance[target]}")
        println("Your path: ${path!!.getPath(target)}")

        println("To return to the main menu, enter -1.")
    }
}


fun testFiles(optimized: Boolean = false) {
    println("You selected file input:")
    print("Source (full!) path: ")
    val source: String = readLine() ?: throw IllegalArgumentException()
    print("Target (full!) path: ")
    val target = readLine() ?: throw IllegalArgumentException()
    println("Starting to process challenge...")
    val time = measureTimeMillis {
        Global.graph.fileChallenge(source, target, optimized)
    }
    println("Processing the challenge took ${time / 1000f} seconds in total.")
}

fun startServer() {
    val thread = Thread(Web())
    thread.start()

    println("Starting server in thread.")
    println("To Terminate, please enter -1.")
    while (true) {
        val target = readLine()?.toIntOrNull() ?: 0
        if (target == -1)
            break
        print("To Terminate, please enter -1: ")
    }

    thread.interrupt()
}
