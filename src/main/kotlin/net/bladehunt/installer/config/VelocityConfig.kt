package net.bladehunt.installer.config

import kotlinx.serialization.Serializable

@Serializable
data class VelocityConfig(
    val bind: String = "0.0.0.0:25565",
    val motd: String = "-    <gradient:#ff3388:#8833ff>oglass server installer</gradient>    -",
    val maxPlayers: Int = -1,
    val forceKeyAuth: Boolean = false,
    val forwardingMode: ForwardingMode = ForwardingMode.MODERN,
    val servers: Map<String,String> = mapOf(
        "lobby" to "127.0.0.1:25566",
        "server" to "127.0.0.1:25567"
    ),
    val tryServers: List<String> = listOf("lobby","server"),
    val forcedHosts: Map<String,Array<String>> = mapOf(),
    val plugins: List<String> = listOf("LuckPerms")
) {
    enum class ForwardingMode {
        NONE,LEGACY,BUNGEEGUARD,MODERN
    }
}