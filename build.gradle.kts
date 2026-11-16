import groovy.json.JsonOutput

plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
}

tasks.compileJava {
    options.release = 21
    options.encoding = "UTF-8"
}

tasks.processResources {
    val properties = mapOf("version" to version)
    inputs.properties(properties)
    filesMatching("paper-plugin.yml") {
        expand(properties)
    }
}

val generateResourcePack by tasks.registering {
    val output = layout.buildDirectory.dir("resourcepack")
    outputs.dir(output)
    doLast {
        fun roman(number: Int): String {
            val numerals = listOf(
                100 to "C", 90 to "XC", 50 to "L", 40 to "XL",
                10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I",
            )
            var rest = number
            return buildString {
                for ((value, numeral) in numerals) {
                    while (rest >= value) {
                        append(numeral)
                        rest -= value
                    }
                }
            }
        }

        val root = output.get().asFile.apply { deleteRecursively() }
        val translations = (11..255).associate { "enchantment.level.$it" to roman(it) }
        root.resolve("assets/minecraft/lang").apply { mkdirs() }
            .resolve("en_us.json").writeText(JsonOutput.prettyPrint(JsonOutput.toJson(translations)))
        val pack = mapOf(
            "description" to "EnchantCap: enchantment levels XI to CCLV",
            "pack_format" to 34,
            "supported_formats" to listOf(34, 1000),
            "min_format" to 34,
            "max_format" to 1000,
        )
        root.resolve("pack.mcmeta").writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mapOf("pack" to pack))))
    }
}

val resourcePack by tasks.registering(Zip::class) {
    from(generateResourcePack)
    archiveFileName = "EnchantCap-ResourcePack-$version.zip"
    destinationDirectory = layout.buildDirectory.dir("distributions")
}

tasks.assemble {
    dependsOn(resourcePack)
}
