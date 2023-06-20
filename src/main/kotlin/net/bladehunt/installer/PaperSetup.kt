package net.bladehunt.installer

import kotlinx.serialization.json.*
import net.bladehunt.installer.config.PaperConfig
import org.apache.commons.configuration2.FileBasedConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.YAMLConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.net.URL
import java.nio.file.Files
import javax.naming.ConfigurationException
import kotlin.io.path.Path

class PaperSetup(val config: PaperConfig) {
    companion object {
        fun getLatestBuild(version: String): Int {
            println("Finding latest build for Paper $version...")
            return Json.parseToJsonElement(
                InputStreamReader(URL("https://api.papermc.io/v2/projects/paper/versions/$version")
                    .openStream()).readText()
            ).jsonObject["builds"]?.jsonArray?.last()?.jsonPrimitive?.int
                ?: throw RuntimeException("")
        }
    }
    val build = getLatestBuild(config.version)
    fun downloadServer() {
        println("Downloading server...")
        URL("https://api.papermc.io/v2/projects/paper/versions/${config.version}/builds/$build/downloads/paper-${config.version}-$build.jar")
            .openStream()
            .use { Files.copy(it, Path("paper-${config.version}-$build.jar")) }
        println("Successfully downloaded server!")
        println("Accepting EULA...")
        File("eula.txt").let {
            it.createNewFile()
            it.writeText("eula=true")
        }
    }
    fun initializeServer() {
        println("Initializing server...")
        val process = ProcessBuilder("java", "-jar", "paper-${config.version}-$build.jar")
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
        configureServerProperties()
        configureVelocitySettings()
    }
    private fun configureServerProperties() {
        val params = Parameters()
        val builder = FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration::class.java)
            .configure(
                params.properties()
                    .setFileName("server.properties")
            )
        try {
            val config = builder.configuration
            config.setProperty("server-ip",this.config.ip)
            config.setProperty("server-port",this.config.port)
            config.setProperty("online-mode",this.config.onlineMode)
            builder.save()
        } catch (e: ConfigurationException) {
            println("There was a problem opening the server.properties file!")
        }
        println("Configured server properties...")
    }
    private fun configureVelocitySettings() {
        val settings = config.velocitySettings
        if (!settings.enabled) return
        val params = Parameters()
        val builder = FileBasedConfigurationBuilder<FileBasedConfiguration>(YAMLConfiguration::class.java)
            .configure(
                params.properties()
                    .setBasePath("config")
                    .setFileName("paper-global.yml")
            )
        try {
            val config = builder.configuration
            config.setProperty("proxies.velocity.enabled",true)
            config.setProperty("proxies.velocity.online-mode",settings.onlineMode)
            config.setProperty("proxies.velocity.secret",settings.secret)
            builder.save()
        } catch (e: ConfigurationException) {
            println("There was a problem opening the server.properties file!")
        }
        println("Configured proxy settings...")
    }
}