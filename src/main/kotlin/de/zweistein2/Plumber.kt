package de.zweistein2

import java.io.File

class Plumber {
    private val messageList: MutableList<String> = mutableListOf()

    init {
        val is64bit = ((System.getProperty("os.arch") ?: "amd64").indexOf("64") != -1)

        val plumber = if(is64bit) {
            File("Plumber64.dll")
        } else {
            File("Plumber.dll")
        }

        System.load(plumber.absolutePath)
    }

    external fun createNamedPipe(): Int

    fun printMessage(message: String) {
        println(message)

        messageList.add(message)
    }

    fun getMessages(): List<String> {
        val messages = messageList.toList()
        messageList.removeAll { true }

        return messages
    }
}