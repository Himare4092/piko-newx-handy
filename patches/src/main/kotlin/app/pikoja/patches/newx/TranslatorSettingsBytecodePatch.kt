package app.pikoja.patches.newx

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.classDefForEach
import app.morphe.patcher.patch.mutableClassDefBy
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction31c
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import java.util.logging.Logger

private const val TRANSLATOR_FRAGMENT_CLASS = "Lapp/morphe/extension/newx/translation/TranslatorFragment;"
private const val TRANSLATOR_FRAGMENT_INNER_PREFIX = "Lapp/morphe/extension/newx/translation/TranslatorFragment$"
private const val TRANSLATOR_CONFIG_CLASS = "Lapp/morphe/extension/newx/translation/TranslatorConfig;"
private const val TRANSLATOR_CUSTOM_TYPE_CLASS = "Lapp/morphe/extension/newx/translation/TranslatorConfig\$CustomType;"
private const val SETTINGS_ACTIVITY_CLASS = "Lapp/morphe/extension/crimera/settings/PikoSettingsActivity;"

internal val TRANSLATOR_FRAGMENT_STRINGS: Map<String, String> = mapOf(
    "Translator" to "翻訳ツール",
    "Add custom translator" to "カスタム翻訳ツールを追加",
    "Translate to" to "翻訳先の言語",
    "Add translator" to "翻訳ツールを追加",
    "Edit translator" to "翻訳ツールを編集",
    "Save" to "保存",
    "Test" to "テスト",
    "Delete" to "削除",
    "Test OK: " to "テスト成功: ",
    "Test failed: " to "テスト失敗: ",
    "Free \u00B7 translates post text" to "無料 \u00B7 ポスト本文を翻訳",
    "Free \u00B7 translates the whole post by ID" to "無料 \u00B7 IDからポスト全体を翻訳",
    "Any OpenAI-compatible API, e.g. https://api.deepseek.com, https://openrouter.ai/api/v1, http://localhost:11434/v1" to
        "任意のOpenAI互換API（例: https://api.deepseek.com、https://openrouter.ai/api/v1、http://localhost:11434/v1）",
    "Free keys end in :fx and use the free endpoint automatically." to
        "無料キーは :fx で終わり、自動的に無料エンドポイントが使用されます。",
    "Name" to "名前",
    "Model" to "モデル",
    "API key" to "APIキー",
    "API key (optional)" to "APIキー（任意）",
)

internal val TRANSLATOR_CONFIG_STRINGS: Map<String, String> = mapOf(
    "Device language" to "端末の言語",
    "English" to "英語",
    "Spanish" to "スペイン語",
    "Portuguese (Brazil)" to "ポルトガル語（ブラジル）",
    "French" to "フランス語",
    "German" to "ドイツ語",
    "Italian" to "イタリア語",
    "Japanese" to "日本語",
    "Korean" to "韓国語",
    "Chinese (Simplified)" to "中国語（簡体字）",
    "Chinese (Traditional)" to "中国語（繁体字）",
    "Russian" to "ロシア語",
    "Arabic" to "アラビア語",
    "Hindi" to "ヒンディー語",
    "Indonesian" to "インドネシア語",
    "Turkish" to "トルコ語",
    "Vietnamese" to "ベトナム語",
    "Thai" to "タイ語",
    "Filipino" to "フィリピン語",
    "Polish" to "ポーランド語",
    "Dutch" to "オランダ語",
    "Ukrainian" to "ウクライナ語",
    "Enter a name" to "名前を入力してください",
    "Enter a valid URL" to "有効なURLを入力してください",
    "Enter a model" to "モデルを入力してください",
    "Enter an API key" to "APIキーを入力してください",
    "Unknown translation provider type" to "不明な翻訳プロバイダータイプです",
)

internal val TRANSLATOR_CUSTOM_TYPE_STRINGS: Map<String, String> = mapOf(
    "OpenAI-compatible" to "OpenAI互換",
)

internal val SETTINGS_ACTIVITY_STRINGS: Map<String, String> = mapOf(
    "Back" to "戻る",
)

private fun replacementsForClass(type: String): Map<String, String>? =
    when {
        type == TRANSLATOR_FRAGMENT_CLASS || type.startsWith(TRANSLATOR_FRAGMENT_INNER_PREFIX) ->
            TRANSLATOR_FRAGMENT_STRINGS
        type == TRANSLATOR_CONFIG_CLASS -> TRANSLATOR_CONFIG_STRINGS
        type == TRANSLATOR_CUSTOM_TYPE_CLASS -> TRANSLATOR_CUSTOM_TYPE_STRINGS
        type == SETTINGS_ACTIVITY_CLASS -> SETTINGS_ACTIVITY_STRINGS
        else -> null
    }

internal fun hasMatchingStringConstant(
    classDef: ClassDef,
    replacements: Map<String, String>,
): Boolean {
    for (method in classDef.methods) {
        val implementation = method.implementation ?: continue
        for (instruction in implementation.instructions) {
            val opcode = instruction.opcode
            if (opcode != Opcode.CONST_STRING && opcode != Opcode.CONST_STRING_JUMBO) continue
            val value = ((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string ?: continue
            if (value in replacements) return true
        }
    }
    return false
}

internal fun replaceStringConstantsInClass(
    mutableClass: MutableClass,
    replacements: Map<String, String>,
): Int {
    var replacedCount = 0
    for (method in mutableClass.methods) {
        val implementation = method.implementation ?: continue
        val instructions = implementation.instructions
        for (index in instructions.indices) {
            val instruction = instructions[index]
            val opcode = instruction.opcode
            if (opcode != Opcode.CONST_STRING && opcode != Opcode.CONST_STRING_JUMBO) continue
            val reference = (instruction as? ReferenceInstruction)?.reference as? StringReference ?: continue
            val translated = replacements[reference.string] ?: continue
            val register = (instruction as OneRegisterInstruction).registerA
            val newRef = ImmutableStringReference(translated)
            val newInstruction = if (opcode == Opcode.CONST_STRING_JUMBO) {
                BuilderInstruction31c(Opcode.CONST_STRING_JUMBO, register, newRef)
            } else {
                BuilderInstruction21c(Opcode.CONST_STRING, register, newRef)
            }
            method.replaceInstruction(index, newInstruction)
            replacedCount++
        }
    }
    return replacedCount
}

internal fun patchTranslatorSettingsBytecode() {
    val logger = Logger.getLogger("JapaneseNewXPikoSettingsPatch")
    val candidates = mutableListOf<Pair<ClassDef, Map<String, String>>>()

    classDefForEach { classDef ->
        val replacements = replacementsForClass(classDef.type) ?: return@classDefForEach
        if (hasMatchingStringConstant(classDef, replacements)) {
            candidates.add(classDef to replacements)
        }
    }

    var totalReplaced = 0
    var modifiedClasses = 0
    for ((classDef, replacements) in candidates) {
        val mutableClass = mutableClassDefBy(classDef)
        val count = replaceStringConstantsInClass(mutableClass, replacements)
        if (count > 0) {
            totalReplaced += count
            modifiedClasses++
        }
    }

    logger.info("Translated $totalReplaced hardcoded settings strings across $modifiedClasses class(es).")
}
