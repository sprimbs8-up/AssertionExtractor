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
