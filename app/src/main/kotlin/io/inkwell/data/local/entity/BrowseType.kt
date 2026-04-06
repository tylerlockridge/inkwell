package io.inkwell.data.local.entity

/**
 * Classification of a [NoteEntity] into one of the four capture families
 * for inbox browse/filtering purposes.
 *
 * ## Resolution heuristic (documented for I2)
 *
 * 1. If [NoteEntity.captureType] is non-null, map it directly:
 *    - `"task"` → [TASK]
 *    - `"note"` → [NOTE]
 *    - `"list_item"` → [LIST]
 *    - `"idea"` → [IDEA]
 * 2. Else if [NoteEntity.listName] or [NoteEntity.listItemsJson] is non-blank → [LIST]
 * 3. Else if [NoteEntity.kind] == `"note"` → [NOTE]
 * 4. Else if [NoteEntity.kind] == `"brainstorming"` → [IDEA]
 * 5. Otherwise → [TASK]
 *
 * As of I7, the server detail API returns `captureMetadata` with an explicit
 * `captureType`. The sync engine normalizes brainstorming ideas to `"idea"`
 * before storage, so step 1 handles them correctly. Steps 2–5 remain as
 * fallback for legacy data or notes without `captureMetadata`.
 */
enum class BrowseType {
    TASK,
    NOTE,
    LIST,
    IDEA,
    ;

    val label: String
        get() = when (this) {
            TASK -> "Tasks"
            NOTE -> "Notes"
            LIST -> "Lists"
            IDEA -> "Ideas"
        }

    val singularLabel: String
        get() = when (this) {
            TASK -> "task"
            NOTE -> "note"
            LIST -> "list"
            IDEA -> "idea"
        }
}

/** Classify this note into one of the four capture families. */
val NoteEntity.browseType: BrowseType
    get() = when (captureType) {
        "task" -> BrowseType.TASK
        "note" -> BrowseType.NOTE
        "list_item" -> BrowseType.LIST
        "idea" -> BrowseType.IDEA
        else -> when {
            !listName.isNullOrBlank() || !listItemsJson.isNullOrBlank() -> BrowseType.LIST
            kind == "note" -> BrowseType.NOTE
            kind == "brainstorming" -> BrowseType.IDEA
            else -> BrowseType.TASK
        }
    }
