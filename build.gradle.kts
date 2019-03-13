import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val compileKotlin: KotlinCompile by tasks

plugins {
  kotlin("jvm") version "1.3.10"
  id("fabric-loom") version "0.2.0-SNAPSHOT"
}

base {
  archivesBaseName = "illuminate"
}

group = "therealfarfetchd.illuminate"
version = "1.0.0"

java {
  sourceCompatibility = VERSION_1_8
  targetCompatibility = VERSION_1_8
}

compileKotlin.kotlinOptions {
  freeCompilerArgs = listOf(
    "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes",
    "-XXLanguage:+InlineClasses",
    "-Xjvm-default=enable"
  )
  jvmTarget = "1.8"
  javaParameters = true
}

minecraft {
}

dependencies {
  minecraft("com.mojang:minecraft:19w09a")
  mappings("net.fabricmc:yarn:19w09a.10")
  modCompile("net.fabricmc:fabric-loader:0.3.7.109")

  // Fabric API. This is technically optional, but you probably want it anyway.
  modCompile("net.fabricmc:fabric:0.2.3.109")
  compile("net.fabricmc:fabric-language-kotlin:1.3.21-1")
  compileOnly(kotlin("stdlib", "1.3.21"))
  compileOnly(kotlin("stdlib-jdk8", "1.3.21"))

  compile(files("qcommon-croco-1.0.5-dev.jar"))
}
