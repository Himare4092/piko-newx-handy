group = "app.pikoja"

patches {
    about {
        name = "Piko NewX Japanese Patches"
        description = "Morphe patch that translates Piko NewX Settings into Japanese"
        source = "https://github.com/Himare4092/piko-newx-japanese"
        author = "Himare4092"
        contact = "na"
        website = "https://github.com/Himare4092/piko-newx-japanese"
        license = "GNU General Public License v3.0"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(buildAndroid, build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
