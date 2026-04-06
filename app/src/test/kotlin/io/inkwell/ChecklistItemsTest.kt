package io.inkwell

import io.inkwell.data.local.entity.ChecklistItem
import io.inkwell.data.local.entity.ChecklistItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistItemsTest {

    // ---- parse: null / blank / empty ----

    @Test
    fun `parse null returns empty list`() {
        assertEquals(emptyList<ChecklistItem>(), ChecklistItems.parse(null))
    }

    @Test
    fun `parse blank returns empty list`() {
        assertEquals(emptyList<ChecklistItem>(), ChecklistItems.parse(""))
        assertEquals(emptyList<ChecklistItem>(), ChecklistItems.parse("  "))
    }

    @Test
    fun `parse empty array returns empty list`() {
        assertEquals(emptyList<ChecklistItem>(), ChecklistItems.parse("[]"))
    }

    @Test
    fun `parse malformed json returns empty list`() {
        assertEquals(emptyList<ChecklistItem>(), ChecklistItems.parse("not json"))
    }

    // ---- parse: legacy string array ----

    @Test
    fun `parse legacy string array`() {
        val items = ChecklistItems.parse("""["milk","eggs","bread"]""")
        assertEquals(3, items.size)
        assertEquals("milk", items[0].text)
        assertFalse(items[0].checked)
        assertEquals("eggs", items[1].text)
        assertFalse(items[1].checked)
        assertEquals("bread", items[2].text)
        assertFalse(items[2].checked)
    }

    @Test
    fun `parse legacy single item`() {
        val items = ChecklistItems.parse("""["only item"]""")
        assertEquals(1, items.size)
        assertEquals("only item", items[0].text)
        assertFalse(items[0].checked)
    }

    // ---- parse: structured object array ----

    @Test
    fun `parse structured array`() {
        val json = """[{"text":"milk","checked":true},{"text":"eggs","checked":false}]"""
        val items = ChecklistItems.parse(json)
        assertEquals(2, items.size)
        assertEquals("milk", items[0].text)
        assertTrue(items[0].checked)
        assertEquals("eggs", items[1].text)
        assertFalse(items[1].checked)
    }

    @Test
    fun `parse structured with missing checked defaults to false`() {
        val json = """[{"text":"item"}]"""
        val items = ChecklistItems.parse(json)
        assertEquals(1, items.size)
        assertFalse(items[0].checked)
    }

    @Test
    fun `parse structured skips items with missing text`() {
        val json = """[{"checked":true},{"text":"valid","checked":false}]"""
        val items = ChecklistItems.parse(json)
        assertEquals(1, items.size)
        assertEquals("valid", items[0].text)
    }

    // ---- serialize ----

    @Test
    fun `serialize empty list returns null`() {
        assertNull(ChecklistItems.serialize(emptyList()))
    }

    @Test
    fun `serialize produces structured format`() {
        val items = listOf(
            ChecklistItem("milk", true),
            ChecklistItem("eggs", false),
        )
        val json = ChecklistItems.serialize(items)
        assertNotNull(json)
        // Parse it back to verify round-trip
        val parsed = ChecklistItems.parse(json)
        assertEquals(2, parsed.size)
        assertEquals("milk", parsed[0].text)
        assertTrue(parsed[0].checked)
        assertEquals("eggs", parsed[1].text)
        assertFalse(parsed[1].checked)
    }

    // ---- round-trip ----

    @Test
    fun `legacy parse then serialize preserves content`() {
        val legacy = """["a","b","c"]"""
        val items = ChecklistItems.parse(legacy)
        val serialized = ChecklistItems.serialize(items)
        val reparsed = ChecklistItems.parse(serialized)
        assertEquals(3, reparsed.size)
        assertEquals("a", reparsed[0].text)
        assertFalse(reparsed[0].checked)
    }

    // ---- toggle ----

    @Test
    fun `toggle unchecked item becomes checked`() {
        val items = listOf(ChecklistItem("a", false), ChecklistItem("b", false))
        val toggled = ChecklistItems.toggle(items, 0)
        assertTrue(toggled[0].checked)
        assertFalse(toggled[1].checked)
    }

    @Test
    fun `toggle checked item becomes unchecked`() {
        val items = listOf(ChecklistItem("a", true))
        val toggled = ChecklistItems.toggle(items, 0)
        assertFalse(toggled[0].checked)
    }

    @Test
    fun `toggle out of bounds returns original list`() {
        val items = listOf(ChecklistItem("a", false))
        val toggled = ChecklistItems.toggle(items, 5)
        assertEquals(items, toggled)
    }

    @Test
    fun `toggle negative index returns original list`() {
        val items = listOf(ChecklistItem("a", false))
        val toggled = ChecklistItems.toggle(items, -1)
        assertEquals(items, toggled)
    }

    @Test
    fun `toggle does not mutate other items`() {
        val items = listOf(
            ChecklistItem("a", true),
            ChecklistItem("b", false),
            ChecklistItem("c", true),
        )
        val toggled = ChecklistItems.toggle(items, 1)
        assertTrue(toggled[0].checked)    // unchanged
        assertTrue(toggled[1].checked)    // toggled
        assertTrue(toggled[2].checked)    // unchanged
    }

    @Test
    fun `toggle on empty list returns empty list`() {
        val toggled = ChecklistItems.toggle(emptyList(), 0)
        assertTrue(toggled.isEmpty())
    }
}
