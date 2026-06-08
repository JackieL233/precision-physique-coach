package com.iwanttobeanifbbpro.app.core

import android.content.Context
import java.io.FileNotFoundException

class SkillAssetRepository(private val context: Context) {
    fun read(relativePath: String): String {
        val normalized = relativePath.trimStart('/')
        val assetPath = "skill/$normalized"
        return try {
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (error: FileNotFoundException) {
            ""
        }
    }

    fun readMany(relativePaths: List<String>): String {
        return relativePaths.joinToString(separator = "\n\n") { path ->
            val body = read(path)
            if (body.isBlank()) {
                "## Missing asset: $path"
            } else {
                "## Asset: $path\n\n$body"
            }
        }
    }
}
