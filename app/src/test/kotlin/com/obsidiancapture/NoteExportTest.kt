package com.obsidiancapture

import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.ui.settings.NoteExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteExportTest {

    private fun makeNote(
        uid: String = "uid_1",
        title: String = "Test Note",
        body: String = "Test body",
        kind: String = "one_shot",
        status: String = "open",
        tags: String = "[\"tag1\",\"tag2\"]",
        priority: String? = "mid",
        calendar: String? = null,
        date: String? = "2026-02-22",
        source: String = "android",
    ) = NoteEntity(
        uid = uid,
        title = title,
        body = body,
        kind = kind,
        status = status,
        tags = tags,
        priority = priority,
        calendar = calendar,
        date = date,
        source = source,
        created = "2026-02-22T12:00:00Z",
        updated = "2026-02-22T12:00:00Z",
    )

    @Test
    fun `toJson produces valid JSON array`() {
        val notes = listOf(makeNote())
        val json = NoteExportHelper.toJson(notes)
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
    }

    @Test
    fun `toJson contains note uid`() {
        val notes = listOf(makeNote(uid = "uid_abc"))
        val json = NoteExportHelper.toJson(notes)
        assertTrue(json.contains("uid_abc"))
    }

    @Test
    fun `toJson contains title`() {
        val notes = listOf(makeNote(title = "My Title"))
        val json = NoteExportHelper.toJson(notes)
        assertTrue(json.contains("My Title"))
    }

    @Test
    fun `toJson exports tags as array`() {
        val notes = listOf(makeNote(tags = "[\"alpha\",\"beta\"]"))
        val json = NoteExportHelper.toJson(notes)
        assertTrue(json.contains("alpha"))
        assertTrue(json.contains("beta"))
    }

    @Test
    fun `toJson handles empty notes list`() {
        val json = NoteExportHelper.toJson(emptyList())
        // kotlinx prettyPrint formats empty list as "[ ]"
        val trimmed = json.replace("\\s".toRegex(), "")
        assertEquals("[]", trimmed)
    }

    @Test
    fun `toCsv has header row`() {
        val csv = NoteExportHelper.toCsv(emptyList())
        val header = csv.lines().first()
        assertTrue(header.contains("uid"))
        assertTrue(header.contains("title"))
        assertTrue(header.contains("body"))
        assertTrue(header.contains("status"))
    }

    @Test
    fun `toCsv has data row`() {
        val notes = listOf(makeNote(uid = "uid_1", title = "Note"))
        val csv = NoteExportHelper.toCsv(notes)
        val lines = csv.lines().filter { it.isNotBlank() }
        assertEquals(2, lines.size) // header + 1 data row
    }

    @Test
    fun `csvEscape quotes field with comma`() {
        val result = NoteExportHelper.csvEscape("hello, world")
        assertEquals("\"hello, world\"", result)
    }

    @Test
    fun `csvEscape escapes embedded quotes`() {
        val result = NoteExportHelper.csvEscape("say \"hello\"")
        assertEquals("\"say \"\"hello\"\"\"", result)
    }

    @Test
    fun `csvEscape quotes field with newline`() {
        val result = NoteExportHelper.csvEscape("line1\nline2")
        assertEquals("\"line1\nline2\"", result)
    }

    @Test
    fun `csvEscape leaves clean value unquoted`() {
        val result = NoteExportHelper.csvEscape("simple value")
        assertEquals("simple value", result)
    }

    @Test
    fun `csvEscape handles empty string`() {
        val result = NoteExportHelper.csvEscape("")
        assertEquals("", result)
    }

    @Test
    fun `toCsv escapes body with commas`() {
        val notes = listOf(makeNote(body = "has, comma"))
        val csv = NoteExportHelper.toCsv(notes)
        assertTrue(csv.contains("\"has, comma\""))
    }
}
