package net.bladehunt.installer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import net.bladehunt.installer.config.PaperConfig
import net.bladehunt.installer.config.PluginConfig
import net.bladehunt.installer.config.VelocityConfig
import java.io.File
import java.io.FileOutputStream

object Main {
    val format = Json {
        encodeDefaults = true
        prettyPrint = true
    }
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.getOrNull(0) == "--generateConfig") {
            generateServerConfig(args.getOrNull(1))
            return
        }
        val file = File("installer.json")
        if (!file.exists()) {
            println("installer.json does not exist!")
            return
        }

        val rawJson = format.parseToJsonElement(file.readText()).jsonObject
        when (rawJson["type"]?.jsonPrimitive?.content) {
            "paper" -> {
                val config = format.decodeFromJsonElement<PaperConfig>(rawJson["config"]!!)
                PaperSetup(config).let {
                    it.downloadServer()
                    it.initializeServer()
                    it.configure()
                }
            }
            "velocity" -> {
                val config = format.decodeFromJsonElement<VelocityConfig>(rawJson["config"]!!)
                VelocitySetup(config).let {
                    it.downloadServer()
                    it.initializeServer()
                    it.configure()
                }
            }
        }
    }
    @OptIn(ExperimentalSerializationApi::class)
    fun generateServerConfig(type: String?) {
        val serverType = type?.lowercase() ?: run {
            println("Please provide a server type! (velocity/paper)")
            return
        }
        if (serverType != "paper" && serverType != "velocity") {
            println("Invalid server type!")
            return
        }
        val stream = FileOutputStream(File("installer.json").also { it.createNewFile() }, false)
        println("Generating $type config...")
        if (type == "paper") {
            format.encodeToStream(
                JsonObject(
                    mapOf(
                        "type" to JsonPrimitive(type),
                        "config" to format.encodeToJsonElement(PaperConfig()),
                    )
                ),
                stream
            )
        } else {
            format.encodeToStream(
                JsonObject(
                    mapOf(
                        "type" to JsonPrimitive(type),
                        "config" to format.encodeToJsonElement(VelocityConfig()),
                    )
                ),
                stream
            )
        }

        stream.close()
    }
    fun loadPlugins(): List<PluginConfig> {
        return listOf()
    }
}