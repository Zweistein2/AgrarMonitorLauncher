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

    val propertiesFile = File("config.properties")
    val properties = Properties()
    if(propertiesFile.isFile && propertiesFile.exists()) {
        logger.debug { "Using config file: ${propertiesFile.absolutePath}" }
        properties.load(FileInputStream(propertiesFile))
    }

    val host = properties.getProperty("host", hostArg?.split("=")?.get(1) ?: "localhost")
    val port = properties.getProperty("port", portArg?.split("=")?.get(1) ?: "8080").toIntOrNull() ?: 8080
    var savegameDir = properties.getProperty("savegameDir")

    if(savegameDir.isNullOrBlank()) {
        savegameDir = savegameDirArg?.split("=")?.get(1) ?: "${System.getenv("USERPROFILE")}\\Documents\\My Games\\FarmingSimulator2022"
    }

    if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI("http://$host:$port?savegame=1"))
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
