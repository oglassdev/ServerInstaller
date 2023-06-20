package net.bladehunt.installer.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PluginConfig(
    val name: String,
    val downloads: Map<String,String>,
    val config: List<ConfigEdit>
) {
    @Serializable
    data class ConfigEdit(
        val filePath: String,
        val type: String,
        val platforms: List<String>,
        val edits: JsonObject
    )
}