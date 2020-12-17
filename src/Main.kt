import Global.graph
import java.lang.IllegalArgumentException
import kotlin.Exception
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
            println("1. Using challenge files (slow Dijkstra)")
            println("2. One-to-all (slow Dijkstra)")
            println("3. Nearest node of position")
            println("4. Inspect a node")
            print("Please choose: ")
            var selection = readLine()?.toIntOrNull() ?: -1
            try {
                when(selection) {
                    1 -> testFiles()
                    2 -> oneToAll()
                    3 -> nodePosition()
                    4 -> node()
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
    print("\nLongitude: ")
    var longitude = readLine()?.toDoubleOrNull() ?: 0.0

    println(Global.graph.findNearestNode(Graph.Position(latitude = latitude, longitude = longitude)))

}

fun oneToAll() {
    print("From which node would you like to know the distance? ")
    var source = readLine()?.toIntOrNull() ?: 0
    var distance: Array<Double>? = null
    println("Calculating one-to-all dijkstra... please wait.")
    var time = measureTimeMillis {
        distance = Global.graph.oneToAll(source)
    }
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
        println("To return to the main menu, enter -1.")
    }
}


fun testFiles() {
    println("You selected file input:")
    print("Source (full!) path: ")
    var source: String = readLine()?.toString() ?: throw IllegalArgumentException()
    print("Target (full!) path: ")
    var target = readLine()?.toString() ?: throw IllegalArgumentException()
    println("Starting processing challenge...")
    var time = measureTimeMillis {
        Global.graph.fileChallenge(source, target)
    }
    println("Processing the challenge took ${time/1000} seconds in total.")
}