package app.pikoja.patches.newx

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY_NEW_X = Compatibility(
        name = "NewX",
        packageName = "com.twitter.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x000000,
        targets = listOf(
            AppTarget(version = null),
        ),
    )
}
