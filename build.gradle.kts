import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val compileKotlin: KotlinCompile by tasks

plugins {
  kotlin("jvm") version "1.3.21"
  id("fabric-loom") version "0.2.0-SNAPSHOT"
}

base {
  archivesBaseName = "illuminate"
}

group = "therealfarfetchd.illuminate"
version = "1.0.0"

apply(from = "https://raw.githubusercontent.com/therealfarfetchd/gradle-tools/master/publish.gradle")

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

repositories {
  maven(url = "https://maven.therealfarfetchd.dev/")
}

dependencies {
  minecraft("com.mojang:minecraft:1.14")
  mappings("net.fabricmc:yarn:1.14+build.7")
  modCompile("net.fabricmc:fabric-loader:0.4.6+build.141")

  // Fabric API. This is technically optional, but you probably want it anyway.
  modCompile("net.fabricmc:fabric:0.2.7+build.127")
  compile("net.fabricmc:fabric-language-kotlin:1.3.30-SNAPSHOT")
  compileOnly(kotlin("stdlib", "1.3.21"))
  compileOnly(kotlin("stdlib-jdk8", "1.3.21"))

  modCompile("therealfarfetchd.qcommon", "croco", "1.0.5-5")
}
