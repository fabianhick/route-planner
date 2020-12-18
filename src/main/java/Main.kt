/*
 * Copyright (c) 2020.
 * Fabian Hick
 */

import java.lang.IllegalArgumentException
import kotlin.system.measureTimeMillis
/*
 * Copyright (c) 2020.
 * Fabian Hick
 */
var time: Long = 0
object Global {
        lateinit var graph: Graph
    }
    fun main(args: Array<String>) {
        time = measureTimeMillis {
            Global.graph = Graph(args[0]);
        }
        menu()
    }

    fun menu() {
        println("Everything finished!")
        println("Took ${time/1000} seconds.")
        while(true) {
            println("Menu: ")
            println("1. Using challenge files (one-to-all)")
            println("2. Using challenge files (optimized)")
            println("3. One-to-all (slow Dijkstra)")
            println("4. Nearest node of position")
            println("5. Inspect a node")
            println("6. Start webserver")
            print("Please choose: ")
            var selection = readLine()?.toIntOrNull() ?: -1
            try {
                when(selection) {
                    1 -> testFiles()
                    2 -> testFilesOptimized()
                    3 -> oneToAll()
                    4 -> nodePosition()
                    5 -> node()
                    6 -> startServer()
                    else -> println("This input is unrecognized. Please try again.")
                }
            } catch (t: Throwable) {
                println("An error occured: ${t.message}")
                t.printStackTrace()
                println("Recovering...")
            }
        }
    }

fun node() {
    print("Which node would you like to inspect? ")
    var source = readLine()?.toIntOrNull() ?: 0
    println("You requested node $source.")
    println(Global.graph.getNode(source))
}

fun nodePosition() {
    println("You selected nearest node:")
    print("Latitude: ")
    var latitude: Double = readLine()?.toDoubleOrNull() ?: 0.0
    print("Longitude: ")
    var longitude = readLine()?.toDoubleOrNull() ?: 0.0

    println(Global.graph.findNearestNode(Graph.Position(latitude = latitude, longitude = longitude)))

}

fun oneToAll() {
    print("From which node would you like to know the distance? ")
    var source = readLine()?.toIntOrNull() ?: 0
    var path: Graph.DijkstraPath? = null
    println("Calculating one-to-all dijkstra... please wait.")
    var time = measureTimeMillis {
        path = Global.graph.oneToAll(source)
    }
    val distance = path!!.distance
    println("Calculated one-to-all dijkstra in ${time/1000} seconds.")
    println("Calculated ${distance!!.size} entries of ${Global.graph.numNodes}")
    while(true) {
        print("To which node would you like to know the distance? ")
        var target = readLine()?.toIntOrNull() ?: 0
        if(target <= -2 || target > distance!!.size -1) {
            println("Invalid input. Try again!")
            continue
        }
        if(target == -1)
            break

        println("Your distance is: ${distance!![target]}")
        println("Your path: ${path!!.getPath(target)}")

        println("To return to the main menu, enter -1.")
    }
}


fun testFiles() {
    println("You selected file input:")
    print("Source (full!) path: ")
    var source: String = readLine()?.toString() ?: throw IllegalArgumentException()
    print("Target (full!) path: ")
    var target = readLine()?.toString() ?: throw IllegalArgumentException()
    println("Starting to process challenge...")
    var time = measureTimeMillis {
        Global.graph.fileChallenge(source, target)
    }
    println("Processing the challenge took ${time/1000} seconds in total.")
}

fun testFilesOptimized() {
    println("You selected file input:")
    print("Source (full!) path: ")
    var source: String = readLine()?.toString() ?: throw IllegalArgumentException()
    print("Target (full!) path: ")
    var target = readLine()?.toString() ?: throw IllegalArgumentException()
    println("Starting to process challenge...")
    var time = measureTimeMillis {
        Global.graph.fileChallengeOptimized(source, target)
    }
    println("Processing the challenge took ${time/1000} seconds in total.")
}

fun startServer() {
    /*class Parallel : Runnable {
        override fun run() {
            val server = Web()
        }
    }*/
    val thread = Thread(Web(Global.graph))
    thread.start()

    println("Starting server in thread.")
    while(true) {
        print("To Terminate, please enter -1: ")
        var target = readLine()?.toIntOrNull() ?: 0
        if(target == -1)
            break
    }

    thread.interrupt()

    thread.stop()

}
