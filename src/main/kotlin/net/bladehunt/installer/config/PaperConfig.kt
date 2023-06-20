package net.bladehunt.installer.config

import kotlinx.serialization.Serializable

@Serializable
data class PaperConfig(
    val version: String = "1.19.4",
    val ip: String = "127.0.0.1",
    val port: Int = 25566,
    val onlineMode: Boolean = false,
    val velocitySettings: VelocitySettings = VelocitySettings(true,true,"CHANGE_THIS"),
    val plugins: List<String> = listOf("LuckPerms")
) {
    @Serializable
    data class VelocitySettings(
        val enabled: Boolean,
        val onlineMode: Boolean,
        val secret: String
    )
}