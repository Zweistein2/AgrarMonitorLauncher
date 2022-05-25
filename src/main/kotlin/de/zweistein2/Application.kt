package de.zweistein2

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import de.zweistein2.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.websocket.*
import mu.KotlinLogging
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.time.Duration
import java.util.*

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val hostArg = args.firstOrNull { arg -> arg.contains("host") }
    val portArg = args.firstOrNull { arg -> arg.contains("port") }
    val savegameDirArg = args.firstOrNull { arg -> arg.contains("savegameDir") }
    val languageArg = args.firstOrNull { arg -> arg.contains("language") }
    val savegameArg = args.firstOrNull { arg -> arg.contains("savegame") }

    val propertiesFile = File("config.properties")
    val properties = Properties()
    if(propertiesFile.isFile && propertiesFile.exists()) {
        logger.debug { "Using config file: ${propertiesFile.absolutePath}" }
        properties.load(FileInputStream(propertiesFile))
    }

    var host = properties.getProperty("host")
    if(hostArg == null) {
        if(host.isNullOrBlank()) {
            host = "localhost"
        }
    } else {
        host = hostArg.split("=")[1]
    }

    var port = properties.getProperty("port")?.toIntOrNull()
    if(portArg == null) {
        if(port == null) {
            port = 8080
        }
    } else {
        port = portArg.split("=")[1].toIntOrNull() ?: 8080
    }

    var savegameDir = properties.getProperty("savegameDir")
    if(savegameDirArg == null) {
        if(savegameDir.isNullOrBlank()) {
            savegameDir = "${System.getenv("USERPROFILE")}\\Documents\\My Games\\FarmingSimulator2022"
        }
    } else {
        savegameDir = savegameDirArg.split("=")[1]
    }

    var language = properties.getProperty("language")
    if(languageArg == null) {
        if(language.isNullOrBlank()) {
            language = "de"
        }
    } else {
        language = languageArg.split("=")[1]
    }

    var savegame = properties.getProperty("savegame")
    if(savegameArg == null) {
        if(savegame.isNullOrBlank()) {
            savegame = "1"
        }
    } else {
        savegame = savegameArg.split("=")[1]
    }

    if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI("http://$host:$port/$language?savegame=$savegame"))
    }

    embeddedServer(Netty, port = port, host = host) {
        install(CORS) {
            allowMethod(HttpMethod.Get)
            anyHost()
        }
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(5)
            timeout = Duration.ofSeconds(30)
            maxFrameSize = Long.MAX_VALUE
        }
        configureRouting(savegameDir)
    }.start(wait = true)
}
