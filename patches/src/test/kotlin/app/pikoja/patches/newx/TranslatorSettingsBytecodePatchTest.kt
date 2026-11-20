package app.pikoja.patches.newx

import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction10x
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction21c
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction31c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TranslatorSettingsBytecodePatchTest {
    @Test
    fun `replacement maps exclude JSON keys, language tags, and blank entries`() {
        val allMaps = listOf(
            TRANSLATOR_FRAGMENT_STRINGS,
            TRANSLATOR_CONFIG_STRINGS,
            TRANSLATOR_CUSTOM_TYPE_STRINGS,
            SETTINGS_ACTIVITY_STRINGS,
        )
        val forbiddenKeys = setOf(
            "", "id", "type", "name", "url", "model",
            "en", "es", "pt-BR", "fr", "de", "it", "ja", "ko",
            "zh-CN", "zh-TW", "ru", "ar", "hi", "tr", "vi", "th",
            "tl", "pl", "nl", "uk", "URL", "Hola mundo",
            "https://api.openai.com/v1", "https://libretranslate.com",
        )

        for (map in allMaps) {
            assertTrue(map.isNotEmpty())
            for ((key, value) in map) {
                assertTrue(key.isNotBlank(), "Replacement key must not be blank")
                assertTrue(value.isNotBlank(), "Replacement value for '$key' must not be blank")
                assertFalse(key in forbiddenKeys, "Forbidden non-UI string present in map: '$key'")
            }
        }
    }

    @Test
    fun `replaceStringConstantsInClass rewrites const-string and const-string-jumbo only for target keys`() {
        val classType = "Lapp/morphe/extension/newx/translation/TranslatorFragment;"
        val instructions = listOf(
            ImmutableInstruction21c(Opcode.CONST_STRING, 0, ImmutableStringReference("Translator")),
            ImmutableInstruction31c(Opcode.CONST_STRING_JUMBO, 1, ImmutableStringReference("Add custom translator")),
            ImmutableInstruction21c(Opcode.CONST_STRING, 2, ImmutableStringReference("Hola mundo")),
            ImmutableInstruction21c(Opcode.CONST_STRING, 3, ImmutableStringReference("en")),
            ImmutableInstruction31c(Opcode.CONST_STRING_JUMBO, 4, ImmutableStringReference("https://api.openai.com/v1")),
            ImmutableInstruction21c(Opcode.CONST_STRING, 5, ImmutableStringReference("API key (optional)")),
            ImmutableInstruction10x(Opcode.RETURN_VOID),
        )
        val method = ImmutableMethod(
            classType,
            "buildUi",
            emptyList(),
            "V",
            AccessFlags.PUBLIC.value,
            null,
            null,
            ImmutableMethodImplementation(6, instructions, null, null),
        )
        val classDef = ImmutableClassDef(
            classType,
            AccessFlags.PUBLIC.value,
            "Ljava/lang/Object;",
            emptyList(),
            "TranslatorFragment.java",
            null,
            null,
            listOf(method),
        )

        assertTrue(hasMatchingStringConstant(classDef, TRANSLATOR_FRAGMENT_STRINGS))
        assertFalse(hasMatchingStringConstant(classDef, SETTINGS_ACTIVITY_STRINGS))

        val mutableClass = MutableClass(classDef)
        val replaced = replaceStringConstantsInClass(mutableClass, TRANSLATOR_FRAGMENT_STRINGS)
        assertEquals(3, replaced)

        val updatedStrings = mutableClass.methods.first().implementation!!.instructions
            .filterIsInstance<ReferenceInstruction>()
            .map { (it.reference as StringReference).string }

        assertEquals(
            listOf(
                "翻訳ツール",
                "カスタム翻訳ツールを追加",
                "Hola mundo",
                "en",
                "https://api.openai.com/v1",
                "APIキー（任意）",
            ),
            updatedStrings,
        )

        val updatedOpcodes = mutableClass.methods.first().implementation!!.instructions.map { it.opcode }
        assertEquals(Opcode.CONST_STRING, updatedOpcodes[0])
        assertEquals(Opcode.CONST_STRING_JUMBO, updatedOpcodes[1])
        assertEquals(Opcode.CONST_STRING, updatedOpcodes[5])
    }
}
