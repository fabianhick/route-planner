/*
 * Copyright (c) 2020.
 * Fabian Hick
 */

package de.unistuttgart.WebApp


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
public open class Web() {
    init {
        main(arrayOf());
    }
    public fun main(args: Array<String>) {
        runApplication<Web>(*args)
    }

}
