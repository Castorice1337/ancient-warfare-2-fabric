package com.columbina.content.structure.template

import com.columbina.runtime.ColumbinaIds
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile

object LegacyTemplateLoader {
    private const val TEMPLATE_ROOT = "assets/${ColumbinaIds.LEGACY_NAMESPACE}/template"
    private const val SOURCE_ROOT = "src/main/resources"

    fun loadTemplates(): Map<String, ImportedStructureTemplate> {
        return listAwsResources(TEMPLATE_ROOT).mapNotNull { resourcePath ->
            val lines = readTemplateLines(resourcePath) ?: return@mapNotNull null
            val parsed = AwsTemplateParser.parseTemplate(resourcePath, lines) ?: return@mapNotNull null
            parsed.name to parsed
        }.toMap(linkedMapOf())
    }

    fun loadSurvivalTemplates(): Map<String, ImportedStructureTemplate> {
        return loadTemplates().filterValues(ImportedStructureTemplate::isSurvival)
    }

    private fun readTemplateLines(path: String): List<String>? {
        val stream = javaClass.classLoader.getResourceAsStream(path) ?: return null
        stream.use { input ->
            return input.bufferedReader(StandardCharsets.ISO_8859_1).readLines()
        }
    }

    private fun listAwsResources(root: String): List<String> {
        val sourcePath = Paths.get(SOURCE_ROOT).resolve(root)
        if (Files.exists(sourcePath)) {
            return Files.walk(sourcePath)
                .filter { it.isRegularFile() && it.extension == "aws" }
                .map { sourcePath.relativize(it).invariantSeparatorsPathString }
                .map { "$root/$it" }
                .sorted()
                .toList()
        }

        val rootUrl = javaClass.classLoader.getResource(root) ?: return emptyList()
        val uri = rootUrl.toURI()
        if (uri.scheme == "jar") {
            val raw = uri.toString()
            val bang = raw.indexOf('!')
            val jarUri = URI.create(raw.substring(0, bang))
            val internalPath = raw.substring(bang + 1)
            val fs = try {
                FileSystems.getFileSystem(jarUri)
            } catch (_: FileSystemNotFoundException) {
                FileSystems.newFileSystem(jarUri, emptyMap<String, Any>())
            }
            val jarRoot = fs.getPath(internalPath)
            return Files.walk(jarRoot)
                .filter { it.isRegularFile() && it.extension == "aws" }
                .map { jarRoot.relativize(it).invariantSeparatorsPathString }
                .map { "$root/$it" }
                .sorted()
                .toList()
        }

        val path = Paths.get(uri)
        return Files.walk(path)
            .filter { it.isRegularFile() && it.extension == "aws" }
            .map { path.relativize(it).invariantSeparatorsPathString }
            .map { "$root/$it" }
            .sorted()
            .toList()
    }
}
