package app.pikoja.patches.newx

import org.w3c.dom.Element
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JapaneseStringsValidationTest {
    private data class StringEntry(
        val name: String,
        val value: String,
        val translatable: Boolean,
    )

    private val formatSpecifierRegex = Regex("""%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]""")
    private val templateTokenRegex = Regex("""\{[^{}]+\}""")
    private val unescapedQuoteRegex = Regex("""(?<!\\)['"]""")

    private fun parseStringsXml(resourcePath: String): List<StringEntry> {
        val stream: InputStream? = javaClass.getResourceAsStream(resourcePath)
        assertNotNull(stream, "Missing resource: $resourcePath")

        val entries = mutableListOf<StringEntry>()
        stream.use { input ->
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
            val nodes = doc.getElementsByTagName("string")
            for (i in 0 until nodes.length) {
                val el = nodes.item(i) as Element
                val name = el.getAttribute("name")
                val translatable = el.getAttribute("translatable") != "false"
                entries.add(StringEntry(name = name, value = el.textContent, translatable = translatable))
            }
        }
        return entries
    }

    @Test
    fun `all translatable NewX strings are translated with zero duplicates`() {
        val enEntries = parseStringsXml("/reference/newx-en-strings.xml")
        val jaEntries = parseStringsXml("/pikoja/values-ja/strings.xml")

        val enSeen = mutableSetOf<String>()
        enEntries.forEach { assertTrue(enSeen.add(it.name), "Duplicate English key: ${it.name}") }

        val jaSeen = mutableSetOf<String>()
        jaEntries.forEach { assertTrue(jaSeen.add(it.name), "Duplicate Japanese key: ${it.name}") }

        val expectedKeys = enEntries.filter { it.translatable }.map { it.name }.toSet()
        val actualKeys = jaEntries.map { it.name }.toSet()

        val missing = expectedKeys - actualKeys
        val extra = actualKeys - expectedKeys
        assertTrue(missing.isEmpty(), "Missing Japanese translations: $missing")
        assertTrue(extra.isEmpty(), "Unexpected Japanese keys: $extra")
        assertEquals(485, actualKeys.size, "Expected 485 translatable NewX strings")
    }

    @Test
    fun `every Japanese string is non-blank and matches format specifiers and tokens`() {
        val enByKey = parseStringsXml("/reference/newx-en-strings.xml")
            .filter { it.translatable }
            .associateBy { it.name }
        val jaEntries = parseStringsXml("/pikoja/values-ja/strings.xml")

        for (ja in jaEntries) {
            assertTrue(ja.value.isNotBlank(), "Blank Japanese translation for ${ja.name}")
            val en = enByKey.getValue(ja.name)

            val enSpecifiers = formatSpecifierRegex.findAll(en.value).map { it.value }.sorted().toList()
            val jaSpecifiers = formatSpecifierRegex.findAll(ja.value).map { it.value }.sorted().toList()
            assertEquals(
                enSpecifiers,
                jaSpecifiers,
                "Format specifier mismatch for ${ja.name}: EN='${en.value}', JA='${ja.value}'",
            )

            val enTokens = templateTokenRegex.findAll(en.value).map { it.value }.sorted().toList()
            val jaTokens = templateTokenRegex.findAll(ja.value).map { it.value }.sorted().toList()
            assertEquals(
                enTokens,
                jaTokens,
                "Template token mismatch for ${ja.name}: EN='${en.value}', JA='${ja.value}'",
            )
        }
    }

    @Test
    fun `every Japanese string passes Android XML string escaping rules`() {
        val jaEntries = parseStringsXml("/pikoja/values-ja/strings.xml")

        for (ja in jaEntries) {
            val value = ja.value
            val isRawQuoted = value.length >= 2 && value.startsWith('"') && value.endsWith('"')
            if (!isRawQuoted) {
                assertFalse(
                    value.contains('\n') || value.contains('\r'),
                    "Literal newline found in non-raw string ${ja.name}: '$value'",
                )
                assertFalse(
                    unescapedQuoteRegex.containsMatchIn(value),
                    "Unescaped quote found in ${ja.name}: '$value'",
                )
            }
        }
    }
}
