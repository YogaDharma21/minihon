package tachiyomi.domain.library.model

sealed interface LibraryDisplayMode {

    data object CompactGrid : LibraryDisplayMode
    data object ComfortableGrid : LibraryDisplayMode
    data object List : LibraryDisplayMode
    data object CoverOnlyGrid : LibraryDisplayMode

    object Serializer {
        fun deserialize(serialized: String): LibraryDisplayMode {
            return LibraryDisplayMode.deserialize(serialized)
        }

        fun serialize(value: LibraryDisplayMode): String {
            return value.serialize()
        }
    }

    companion object {
        val values by lazy { setOf(ComfortableGrid) }
        val default = ComfortableGrid

        fun deserialize(serialized: String): LibraryDisplayMode {
            return ComfortableGrid
        }
    }

    fun serialize(): String {
        return when (this) {
            ComfortableGrid -> "COMFORTABLE_GRID"
            CompactGrid -> "COMPACT_GRID"
            CoverOnlyGrid -> "COVER_ONLY_GRID"
            List -> "LIST"
        }
    }
}
