package net.bladehunt.installer

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.file.CommentedFileConfig
import com.electronwill.nightconfig.toml.TomlFormat
import com.electronwill.nightconfig.toml.TomlWriter
import kotlinx.serialization.json.*
import net.bladehunt.installer.config.VelocityConfig
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import kotlin.io.path.Path


class VelocitySetup(val config: VelocityConfig) {
    companion object {
        fun getLatestVersionBuild(): Pair<String, Int> {
            val version = Json.parseToJsonElement(
                InputStreamReader(
                    URL("https://api.papermc.io/v2/projects/velocity")
                    .openStream()).readText()
            ).jsonObject["versions"]!!.jsonArray.last().jsonPrimitive.content
            val build = Json.parseToJsonElement(
                InputStreamReader(
                    URL("https://api.papermc.io/v2/projects/velocity/versions/$version")
                    .openStream()).readText()
            ).jsonObject["builds"]?.jsonArray?.last()?.jsonPrimitive?.int
                ?: throw RuntimeException("")
            return version to build
        }
    }
    private val versionBuild = getLatestVersionBuild()
    fun downloadServer() {
        println("Downloading server...")
        URL("https://api.papermc.io/v2/projects/velocity/versions/${versionBuild.first}/builds/${versionBuild.second}/downloads/velocity-${versionBuild.first}-${versionBuild.second}.jar")
            .openStream()
            .use { Files.copy(it, Path("velocity-${versionBuild.first}-${versionBuild.second}.jar")) }
        println("Successfully downloaded server!")
    }
    fun initializeServer() {
        println("Initializing server...")
        val process = ProcessBuilder("java", "-jar", "velocity-${versionBuild.first}-${versionBuild.second}.jar")
            .start()
        val input = BufferedReader(InputStreamReader(process.inputStream))
        try {
            input.forEachLine {
                if (it.contains("done",ignoreCase = true)) {
                    process.destroy()
                    input.close()
                }
            }
            process.waitFor()
        } catch (_: IOException) {
        }
        println("Completed initializing server!")
    }
    fun configure() {
        val fileConfig = CommentedFileConfig.builder(Path("velocity.toml"))
            .preserveInsertionOrder()
            .build()
        fileConfig.load()
        fileConfig.set<String>("bind",config.bind)
        fileConfig.set<String>("motd",config.motd)
        fileConfig.set<Int>("show-max-players",config.maxPlayers)
        fileConfig.set<String>("force-key-authentication",config.forceKeyAuth)
        fileConfig.set<String>("player-info-forwarding-mode",config.forwardingMode.name)

        val servers = fileConfig.get<Config>("servers")
        servers.clear()
        for (server in config.servers) {
            servers.set<String>(server.key,server.value)
        }
        servers.set<List<String>>("try",config.tryServers)

        val forcedHosts = fileConfig.get<Config>("forced-hosts")
        forcedHosts.clear()
        for (host in config.forcedHosts) {
            forcedHosts.set<String>(host.key,host.value)
        }

        fileConfig.save()
        fileConfig.close()
    }
}