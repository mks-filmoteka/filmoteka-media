package io.github.mksfilmoteka.media

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FilmotekaMediaApplication

fun main(args: Array<String>) {
    runApplication<FilmotekaMediaApplication>(*args)
}
