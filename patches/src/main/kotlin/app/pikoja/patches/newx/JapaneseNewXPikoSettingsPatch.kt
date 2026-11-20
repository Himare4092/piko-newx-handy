package app.pikoja.patches.newx

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.classDefByOrNull
import app.morphe.patcher.patch.document
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceFile
import app.pikoja.patches.newx.Constants.COMPATIBILITY_NEW_X
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream
import java.util.logging.Logger
import javax.xml.parsers.DocumentBuilderFactory

private const val DEFAULT_STRINGS_PATH = "res/values/strings.xml"
private const val JAPANESE_STRINGS_PATH = "res/values-ja/strings.xml"
private const val BUNDLED_JAPANESE_STRINGS_RESOURCE = "pikoja/values-ja/strings.xml"
private const val NEWX_SETTINGS_HOST_CLASS = "Lapp/morphe/extension/newx/settings/NewXSettingsHost;"
private const val NEWX_SENTINEL_STRING_KEY = "piko_newx_settings_title"

private object BundledResources

/**
 * Translates all Piko NewX Settings into Japanese when applied alongside Piko's NewX patches.
 *
 * The patch name starts with "Japanese" so Morphe executes it before Piko's "NewX: ..." patches
 * and runs its [finalize] block after Piko's `addResourcesPatch` and `newXExtensionPatch`.
 */
@Suppress("unused")
val japaneseNewXPikoSettingsPatch = patch(
    name = "Japanese translation for Piko NewX",
    description = "Translates Piko NewX Settings (including Translator settings) into Japanese. Apply alongside Piko's NewX patches.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_NEW_X)

    execute {
        // Intentionally empty: all work runs in finalize {} after Piko's NewX patches have
        // injected default NewX string resources and merged extension bytecode.
    }

    finalize {
        applyJapaneseStringResources()
        patchTranslatorSettingsBytecode()
    }
}

private fun applyJapaneseStringResources() {
    val logger = Logger.getLogger("JapaneseNewXPikoSettingsPatch")
    val defaultKeys = readDefaultStringKeys()
    val hasNewXDefaultStrings = NEWX_SENTINEL_STRING_KEY in defaultKeys
    val hasNewXExtension = classDefByOrNull(NEWX_SETTINGS_HOST_CLASS) != null

    if (!hasNewXDefaultStrings && !hasNewXExtension) {
        throw PatchException(
            "Piko NewX patches were not detected. Apply this patch together with Piko's NewX patches.",
        )
    }

    val destFile = resourceFile(JAPANESE_STRINGS_PATH)
    if (!destFile.exists()) {
        destFile.parentFile?.mkdirs()
        if (!destFile.createNewFile()) {
            throw PatchException("Could not create $JAPANESE_STRINGS_PATH")
        }
        destFile.writeText("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n</resources>\n")
    }

    val srcStream = openBundledJapaneseStrings()
    var addedCount = 0
    var replacedCount = 0
    var skippedCount = 0

    srcStream.use { stream ->
        val srcDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
        val srcResources = srcDoc.getElementsByTagName("resources").item(0) as? Element
            ?: throw PatchException("Invalid bundled resource XML: missing <resources> in $BUNDLED_JAPANESE_STRINGS_RESOURCE")

        document(JAPANESE_STRINGS_PATH).use { destDoc ->
            val destResources = destDoc.getElementsByTagName("resources").item(0) as? Element
                ?: throw PatchException("Invalid $JAPANESE_STRINGS_PATH: missing <resources>")

            val existingNodes = indexExistingElements(destResources)

            var child = srcResources.firstChild
            while (child != null) {
                if (child.nodeType == Node.ELEMENT_NODE) {
                    val srcElement = child as Element
                    if (srcElement.tagName == "string") {
                        val name = srcElement.getAttribute("name")
                        if (name.isNotEmpty()) {
                            if (hasNewXDefaultStrings && name !in defaultKeys) {
                                skippedCount++
                            } else {
                                val existing = existingNodes[Pair(srcElement.tagName, name)]
                                if (existing != null) {
                                    destResources.removeChild(existing)
                                    replacedCount++
                                } else {
                                    addedCount++
                                }
                                val imported = destDoc.importNode(srcElement, true)
                                destResources.appendChild(imported)
                                existingNodes[Pair(srcElement.tagName, name)] = imported
                            }
                        }
                    }
                }
                child = child.nextSibling
            }
        }
    }

    logger.info(
        "Applied Japanese NewX strings to $JAPANESE_STRINGS_PATH " +
            "(added=$addedCount, replaced=$replacedCount, skipped=$skippedCount).",
    )
}

private fun readDefaultStringKeys(): Set<String> {
    val defaultStringsFile = resourceFile(DEFAULT_STRINGS_PATH)
    if (!defaultStringsFile.exists()) return emptySet()

    val keys = LinkedHashSet<String>()
    defaultStringsFile.inputStream().buffered().use { input ->
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
        val stringNodes = doc.getElementsByTagName("string")
        for (i in 0 until stringNodes.length) {
            val element = stringNodes.item(i) as? Element ?: continue
            val name = element.getAttribute("name")
            if (name.isNotEmpty()) {
                keys.add(name)
            }
        }
    }
    return keys
}

private fun indexExistingElements(resources: Element): MutableMap<Pair<String, String>, Node> {
    val existing = HashMap<Pair<String, String>, Node>()
    var child = resources.firstChild
    while (child != null) {
        if (child.nodeType == Node.ELEMENT_NODE) {
            val element = child as Element
            val name = element.getAttribute("name")
            if (name.isNotEmpty()) {
                existing[Pair(element.tagName, name)] = element
            }
        }
        child = child.nextSibling
    }
    return existing
}

private fun openBundledJapaneseStrings(): InputStream =
    BundledResources::class.java.getResourceAsStream("/$BUNDLED_JAPANESE_STRINGS_RESOURCE")
        ?: BundledResources::class.java.classLoader?.getResourceAsStream(BUNDLED_JAPANESE_STRINGS_RESOURCE)
        ?: throw PatchException("Bundled resource not found: $BUNDLED_JAPANESE_STRINGS_RESOURCE")
