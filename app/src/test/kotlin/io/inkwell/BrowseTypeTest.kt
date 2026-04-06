package io.inkwell

import io.inkwell.data.local.entity.BrowseType
import io.inkwell.data.local.entity.NoteEntity
import io.inkwell.data.local.entity.browseType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class BrowseTypeTest {

    private fun note(
        captureType: String? = null,
        kind: String = "one_shot",
        listName: String? = null,
        listItemsJson: String? = null,
    ) = NoteEntity(
        uid = "test-uid",
        title = "Test",
        body = "Body",
        kind = kind,
        captureType = captureType,
        listName = listName,
        listItemsJson = listItemsJson,
        created = Instant.now().toString(),
        updated = Instant.now().toString(),
    )

    // --- Explicit captureType (highest priority) ---

    @Test
    fun `explicit captureType task`() {
        assertEquals(BrowseType.TASK, note(captureType = "task").browseType)
    }

    @Test
    fun `explicit captureType note`() {
        assertEquals(BrowseType.NOTE, note(captureType = "note").browseType)
    }

    @Test
    fun `explicit captureType list_item`() {
        assertEquals(BrowseType.LIST, note(captureType = "list_item").browseType)
    }

    @Test
    fun `explicit captureType idea`() {
        assertEquals(BrowseType.IDEA, note(captureType = "idea").browseType)
    }

    // --- Fallback: list inferred from listName or listItemsJson ---

    @Test
    fun `inferred list from listName`() {
        assertEquals(BrowseType.LIST, note(listName = "Groceries").browseType)
    }

    @Test
    fun `inferred list from listItemsJson`() {
        assertEquals(BrowseType.LIST, note(listItemsJson = """["milk","eggs"]""").browseType)
    }

    @Test
    fun `blank listName does not trigger list inference`() {
        assertEquals(BrowseType.TASK, note(listName = "  ").browseType)
    }

    // --- Fallback: kind-based inference ---

    @Test
    fun `kind note infers NOTE`() {
        assertEquals(BrowseType.NOTE, note(kind = "note").browseType)
    }

    @Test
    fun `kind brainstorming infers IDEA`() {
        assertEquals(BrowseType.IDEA, note(kind = "brainstorming").browseType)
    }

    // --- Default: task ---

    @Test
    fun `default kind one_shot is TASK`() {
        assertEquals(BrowseType.TASK, note(kind = "one_shot").browseType)
    }

    @Test
    fun `unknown kind falls back to TASK`() {
        assertEquals(BrowseType.TASK, note(kind = "unknown_kind").browseType)
    }

    @Test
    fun `null captureType with no other signals is TASK`() {
        assertEquals(BrowseType.TASK, note().browseType)
    }

    // --- Priority: captureType wins over kind ---

    @Test
    fun `captureType note overrides kind brainstorming`() {
        assertEquals(BrowseType.NOTE, note(captureType = "note", kind = "brainstorming").browseType)
    }

    @Test
    fun `captureType task overrides kind note`() {
        assertEquals(BrowseType.TASK, note(captureType = "task", kind = "note").browseType)
    }

    // --- Enum labels ---

    @Test
    fun `label returns plural form`() {
        assertEquals("Tasks", BrowseType.TASK.label)
        assertEquals("Notes", BrowseType.NOTE.label)
        assertEquals("Lists", BrowseType.LIST.label)
        assertEquals("Ideas", BrowseType.IDEA.label)
    }

    @Test
    fun `singularLabel returns singular form`() {
        assertEquals("task", BrowseType.TASK.singularLabel)
        assertEquals("note", BrowseType.NOTE.singularLabel)
        assertEquals("list", BrowseType.LIST.singularLabel)
        assertEquals("idea", BrowseType.IDEA.singularLabel)
    }
}
