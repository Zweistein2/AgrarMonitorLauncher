package de.zweistein2.plugins

import de.zweistein2.Plumber
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

@OptIn(DelicateCoroutinesApi::class)
fun Application.configureRouting(savegameDir: String) {
    val plumber = Plumber()
    GlobalScope.launch {
        plumber.createNamedPipe()
    }

    routing {
        get("/feed/{savegame}/{file}") {
            assert(!call.parameters["savegame"].isNullOrEmpty())
            assert(!call.parameters["file"].isNullOrEmpty())

            if(((call.parameters["savegame"]!!.toIntOrNull() ?: -1) >= 1 && (call.parameters["savegame"]!!.toIntOrNull() ?: -1) <= 20)
                && !call.parameters["file"]!!.endsWith(".xml")) {
                logger.info { "accessed \"${call.parameters["file"]}.xml\" in \"savegame${call.parameters["savegame"]}\"" }
                call.respondText { File("${savegameDir}/savegame${call.parameters["savegame"]}/${call.parameters["file"]}.xml").readText() }
            } else {
                logger.warn { "failed to access \"${call.parameters["file"]}.xml\" in \"savegame${call.parameters["savegame"]}\"" }
                call.respondText { "Savegame \"${call.parameters["savegame"]}\" or File \"${call.parameters["file"]}\" not found" }
            }
        }
        webSocket("/plumber") {
            send("connection established!")

            for(frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedMessage = frame.readText()

                if(receivedMessage == "ERROR" || receivedMessage == "STOP") {
                    return@webSocket
                } else {
                    for(message in plumber.getMessages()) {
                        send(message)
                    }
                }
            }
        }
        static("/") {
            preCompressed {
                resources("agrarmonitor/dist")
                defaultResource("agrarmonitor/dist/index.html")
            }
        }
        static("/de") {
            preCompressed {
                resources("agrarmonitor/dist")
                defaultResource("agrarmonitor/dist/index.html")
            }
        }
        static("/de/*") {
            preCompressed {
                resources("agrarmonitor/dist")
                defaultResource("agrarmonitor/dist/index.html")
            }
        }
        static("/en") {
            preCompressed {
                resources("agrarmonitor/dist")
                defaultResource("agrarmonitor/dist/index.html")
            }
        }
        static("/en/*") {
            preCompressed {
                resources("agrarmonitor/dist")
                defaultResource("agrarmonitor/dist/index.html")
            }
        }
    }
}
