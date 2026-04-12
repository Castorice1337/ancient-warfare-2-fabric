package com.columbina.content.logistics.warehouse

data class WarehouseStockFilter(
    var itemId: String? = null,
    var quantity: Int = 0,
) {
    fun encode(): String = "${itemId.orEmpty()}|$quantity"

    companion object {
        fun decode(encoded: String): WarehouseStockFilter {
            val parts = encoded.split('|', limit = 2)
            return WarehouseStockFilter(
                itemId = parts.getOrNull(0)?.ifBlank { null },
                quantity = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            )
        }
    }
}

data class WarehouseLinkFilter(
    var itemId: String? = null,
    var compareValue: Int = 0,
    var quantity: Int = 0,
    var equalitySignType: EqualitySignType = EqualitySignType.EQUAL_TO,
) {
    enum class EqualitySignType {
        EQUAL_TO,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL_TO,
        LESS_THAN_OR_EQUAL_TO,
        ;

        fun next(): EqualitySignType = entries[(ordinal + 1) % entries.size]

        fun previous(): EqualitySignType = entries[(ordinal + entries.size - 1) % entries.size]
    }

    fun encode(): String = listOf(itemId.orEmpty(), compareValue, quantity, equalitySignType.name).joinToString("|")

    companion object {
        fun decode(encoded: String): WarehouseLinkFilter {
            val parts = encoded.split('|', limit = 4)
            return WarehouseLinkFilter(
                itemId = parts.getOrNull(0)?.ifBlank { null },
                compareValue = parts.getOrNull(1)?.toIntOrNull() ?: 0,
                quantity = parts.getOrNull(2)?.toIntOrNull() ?: 0,
                equalitySignType = parts.getOrNull(3)?.let(EqualitySignType::valueOf) ?: EqualitySignType.EQUAL_TO,
            )
        }
    }
}

fun encodeStockFilters(filters: List<WarehouseStockFilter>): String = filters.joinToString(";") { it.encode() }

fun decodeStockFilters(encoded: String): List<WarehouseStockFilter> =
    if (encoded.isBlank()) emptyList() else encoded.split(';').filter { it.isNotBlank() }.map(WarehouseStockFilter::decode)

fun encodeLinkFilters(filters: List<WarehouseLinkFilter>): String = filters.joinToString(";") { it.encode() }

fun decodeLinkFilters(encoded: String): List<WarehouseLinkFilter> =
    if (encoded.isBlank()) emptyList() else encoded.split(';').filter { it.isNotBlank() }.map(WarehouseLinkFilter::decode)
