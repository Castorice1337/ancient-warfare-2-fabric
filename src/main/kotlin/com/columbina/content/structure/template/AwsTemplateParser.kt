package com.columbina.content.structure.template

import net.minecraft.core.BlockPos

object AwsTemplateParser {
    private val blockNameRegex = Regex("""blockName:"([^"]+)"""")

    fun parseTemplate(fileName: String, lines: List<String>): ImportedStructureTemplate? {
        val filtered = lines.filterNot { it.startsWith("#") || it.isBlank() }
        val iterator = filtered.iterator()

        var version = "0.0"
        var name = fileName.substringAfterLast('/').substringBeforeLast('.')
        var size = BlockPos.ZERO
        var offset = BlockPos.ZERO
        var modDependencies: Set<String> = emptySet()
        var isSurvival = false
        var isWorldGenEnabled = false

        var templateData = IntArray(0)
        val rules = linkedMapOf<Int, ImportedStructureRule>()

        while (iterator.hasNext()) {
            val line = iterator.next()
            when {
                line == "header:" -> {
                    while (iterator.hasNext()) {
                        val headerLine = iterator.next()
                        if (headerLine == ":endheader") break
                        when {
                            headerLine.startsWith("version=") -> version = headerLine.substringAfter('=')
                            headerLine.startsWith("name=") -> name = headerLine.substringAfter('=')
                            headerLine.startsWith("mods=") -> {
                                modDependencies = headerLine.substringAfter('=').split(',').filter(String::isNotBlank).toSet()
                            }
                            headerLine.startsWith("size=") -> size = parseVec3(headerLine.substringAfter('='))
                            headerLine.startsWith("offset=") -> offset = parseVec3(headerLine.substringAfter('='))
                        }
                    }
                    if (size != BlockPos.ZERO) {
                        templateData = IntArray(size.x * size.y * size.z)
                    }
                }
                line == "validation:" -> {
                    while (iterator.hasNext()) {
                        val validationLine = iterator.next()
                        if (validationLine == ":endvalidation") break
                        when {
                            validationLine.startsWith("survival=") -> isSurvival = validationLine.substringAfter('=').toBoolean()
                            validationLine.startsWith("worldGenEnabled=") -> isWorldGenEnabled = validationLine.substringAfter('=').toBoolean()
                        }
                    }
                }
                line.startsWith("layer:") -> {
                    val rowLines = mutableListOf<String>()
                    while (iterator.hasNext()) {
                        val rowLine = iterator.next()
                        if (rowLine == ":endlayer") break
                        rowLines += rowLine
                    }
                    parseLayer(line, rowLines, size, templateData)
                }
                line == "rule:" -> {
                    var plugin = ""
                    var number = 0
                    val dataLines = mutableListOf<String>()
                    var inData = false

                    while (iterator.hasNext()) {
                        val ruleLine = iterator.next()
                        when {
                            ruleLine == ":endrule" -> break
                            ruleLine.startsWith("plugin=") -> plugin = ruleLine.substringAfter('=')
                            ruleLine.startsWith("number=") -> number = ruleLine.substringAfter('=').toInt()
                            ruleLine == "data:" -> inData = true
                            ruleLine == ":enddata" -> inData = false
                            inData -> dataLines += ruleLine
                        }
                    }

                    val rawData = dataLines.joinToString("\n")
                    val blockName = blockNameRegex.find(rawData)?.groupValues?.get(1)
                    rules[number] = ImportedStructureRule(number, plugin, rawData, blockName)
                }
            }
        }

        if (size == BlockPos.ZERO || templateData.isEmpty()) {
            return null
        }

        return ImportedStructureTemplate(
            name = name,
            version = version,
            size = size,
            offset = offset,
            modDependencies = modDependencies,
            isSurvival = isSurvival,
            isWorldGenEnabled = isWorldGenEnabled,
            blockRules = rules,
            templateData = templateData,
            sourcePath = fileName,
        )
    }

    fun parseLayer(layerHeader: String, rowLines: List<String>, size: BlockPos, templateData: IntArray) {
        val header = layerHeader.substringAfter(':').trim()
        val layerParts = header.split('-').map(String::trim)
        val minLayer = layerParts[0].toInt()
        val maxLayer = layerParts.getOrNull(1)?.toIntOrNull() ?: minLayer

        val rows = parseLayerRows(rowLines)
        for (layer in minLayer..maxLayer) {
            var z = 0
            rows.forEach { row ->
                for (x in 0 until minOf(size.x, row.size)) {
                    val index = (layer * size.x * size.z) + (z * size.x) + x
                    if (index in templateData.indices) {
                        templateData[index] = row[x].toInt()
                    }
                }
                z++
            }
        }
    }

    private fun parseLayerRows(rowLines: List<String>): List<ShortArray> {
        val rows = mutableListOf<ShortArray>()
        rowLines.forEach { rowLine ->
            val rowParts = rowLine.split('x', limit = 2)
            val repeat = if (rowParts.size > 1 && rowParts[0].all(Char::isDigit)) rowParts[0].toInt() else 1
            val payload = if (rowParts.size > 1 && rowParts[0].all(Char::isDigit)) rowParts[1] else rowLine
            val blocks = parseBlocks(payload)
            repeat(repeat) {
                rows += blocks
            }
        }
        return rows
    }

    private fun parseBlocks(row: String): ShortArray {
        val blocks = mutableListOf<Short>()
        row.split(',').forEach { blockPart ->
            val split = blockPart.split('|', limit = 2)
            val value = split[0].trim().toShort()
            val repeat = split.getOrNull(1)?.trim()?.toIntOrNull() ?: 1
            repeat(repeat) {
                blocks += value
            }
        }
        return blocks.toShortArray()
    }

    private fun parseVec3(raw: String): BlockPos {
        val parts = raw.split(',').map { it.trim().toInt() }
        return BlockPos(parts[0], parts[1], parts[2])
    }
}
