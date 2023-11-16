plugins {
    id("java")
    id("com.diffplug.spotless") version "6.21.0"


}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(files("libs/toolbox.jar"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.assertj:assertj-core:3.11.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.7.1")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("me.tongfei:progressbar:0.5.5")
//Thanks for using https://jar-download.com
}


tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        targetExclude("build/**")
        importOrderFile("config/spotless/spotless.importorder")
        eclipse("4.28").configFile("config/spotless/spotless-format.xml")
        trimTrailingWhitespace()
        removeUnusedImports()
    }
}

tasks.register("runnableJar", type = Jar::class) {
    manifest.attributes["Main-Class"] = "de.uni_passau.fim.se2.assertion_exctractor.Main"

    from(configurations.runtimeClasspath.get().map { file ->
        if (file.isDirectory)
            file
        else
            zipTree(file)
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    with(tasks.jar.get())
}

