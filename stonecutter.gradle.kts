plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.10-SNAPSHOT" apply false
    id("dev.kikugie.j52j") version "1.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.5.+" apply false
}

stonecutter active "1.21.5-fabric"

// Builds every version into `build/libs/{mod.version}/`
stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledClean", stonecutter.chiseled) {
    group = "project"
    ofTask("clean")
}

stonecutter registerChiseled tasks.register("chiseledRunClient", stonecutter.chiseled) {
    group = "project"
    ofTask("runClient")
}
