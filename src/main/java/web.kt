/*
 * Copyright (c) 2020.
 * Fabian Hick
 */

import Global.graph
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import java.io.File
import com.google.gson.Gson
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong
import kotlin.system.measureTimeMillis

/*
 * Copyright (c) 2020.
 * Fabian Hick
 */
class Web() : Runnable {
    override fun run() {
        val server = embeddedServer(Netty, port = 8000) {

            routing {

                // Get route
                get("/route/{orig_lat}/{orig_long}/{target_lat}/{target_long}") {
                    var time = measureTimeMillis {
                        println("Received request, calculating nearest nodes...")
                        val origin = Graph.Position(
                            call.parameters["orig_lat"]!!.toDouble(),
                            call.parameters["orig_long"]!!.toDouble()
                        )
                        val target = Graph.Position(
                            call.parameters["target_lat"]!!.toDouble(),
                            call.parameters["target_long"]!!.toDouble()
                        )
                        var start: Int = 0
                        var poi: Int = 0
                        var time = measureTimeMillis {
                            start = Global.graph.findNearestNode(origin).id
                            poi = Global.graph.findNearestNode(target).id
                        }
                        println("Finished calculating nearest nodes in ${time/1000}s")
                        println("Starting to calculate route from #$start to #$poi...")
                        try {
                            val paths: Graph.DijkstraPath = Global.graph.distanceTo(start, poi);
                            println("Dijkstra finished. Calculating visual path.")
                            val response = paths.getPathAsArrayBackwards(poi);
                            call.respondText(Gson().toJson(response).toString())
                        } catch (e: Throwable) {
                            call.respondText { "An error occured: " + e.message }
                            println("An error occured: " + e.message)
                            e.printStackTrace()

                        }
                    }
                    println("Calculating request took ${time/1000} s.")
                }
                // Get route
                get("/route/nodes/{origin}/{target}") {
                    var time = measureTimeMillis {
                        val origin: Int = call.parameters["origin"]!!.toInt()
                        val target: Int = call.parameters["target"]!!.toInt()
                        println("Starting to calculate route from #$origin to #$target...")
                        try {
                            val paths: Graph.DijkstraPath = Global.graph.distanceTo(origin, target);
                            val response = paths.getPathAsArrayBackwards(target);
                            call.respondText(Gson().toJson(response).toString())
                        } catch (e: Throwable) {
                            call.respondText { "An error occured: " + e.message }
                            println("An error occured: " + e.message)
                            e.printStackTrace()

                        }
                    }
                    println("Calculating request took ${time/1000} s.")
                }

                get("/node/{lat}/{long}") {
                    var time = measureTimeMillis {
                        println("Received request, calculating nearest nodes...")
                        val point =
                            Graph.Position(call.parameters["lat"]!!.toDouble(), call.parameters["long"]!!.toDouble())
                        val node = Global.graph.findNearestNode(point)
                        call.respondText(Gson().toJson(node).toString())
                    }
                    println("Calculating request took ${time/1000} s.")
                }

                get("/position/{node}") {
                    var time = measureTimeMillis {
                        val node: Int = call.parameters["node"]!!.toInt()
                        println("Received request, getting position for node #$node ...")
                        val point = Global.graph.getNode(node)
                        call.respondText(Gson().toJson(point.position).toString())
                    }
                    println("Calculating request took ${time/1000} s.")
                }
                get("/nodes/size") {
                    var time = measureTimeMillis {
                        println("Received request, return size of nodes ...")
                        val size = Global.graph.nodes.size
                        call.respondText(Gson().toJson(size).toString())
                    }
                    println("Calculating request took ${time/1000} s.")
                }

                static {
                    resource("/", "site/map.html")
                }
            }
        }
        try {
            server.start(wait = true)
        } catch (e: InterruptedException) {
            server.stop(1, 5, TimeUnit.SECONDS)
            return
        }
    }

}



