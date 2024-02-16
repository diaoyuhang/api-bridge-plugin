plugins {
    id("org.jetbrains.intellij") version "1.14.2"
}

group = "com.itangcent"
version = properties["plugin_version"]!!

val intellijVersions = arrayOf(
    mapOf("jdk" to 17, "version" to "2023.3.3", "since" to "231"),
    mapOf("jdk" to 15, "version" to "2022.2.3", "since" to "223"),
    mapOf("jdk" to 11, "version" to "2021.2.1", "since" to "212")
)

val javaVersion = JavaVersion.current().majorVersion.toInt()
val (intellijVersion, intellijSince) = intellijVersions.first { javaVersion >= (it["jdk"] as Int) }.let {
    it["version"].toString() to it["since"].toString()
}
println("use intellij $intellijVersion")

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springdoc:springdoc-openapi-ui:1.7.0"){
        exclude("org.slf4j","slf4j-api")
        exclude("org.webjars","swagger-ui")
        exclude("org.springframework","spring-webmvc")
        exclude("org.springframework","spring-web")
        exclude("org.springframework.boot","spring-boot-autoconfigure")
    }

    implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r"){
        exclude("org.slf4j","slf4j-api")
    }

    implementation(project(":common-api")) {
        exclude("org.apache.httpcomponents", "httpclient")
    }

    implementation("com.itangcent:commons:${properties["itangcent_intellij_version"]}") {
        exclude("com.google.inject")
        exclude("com.google.code.gson")
    }


    implementation("com.itangcent:guice-action:${properties["itangcent_intellij_version"]}") {
        exclude("com.google.inject")
        exclude("com.google.code.gson")
    }

    implementation("com.itangcent:intellij-jvm:${properties["itangcent_intellij_version"]}") {
        exclude("com.google.inject")
        exclude("com.google.code.gson")
    }

    implementation("com.itangcent:intellij-idea:${properties["itangcent_intellij_version"]}") {
        exclude("com.google.inject")
        exclude("com.google.code.gson")
    }

    implementation("com.itangcent:intellij-kotlin-support:${properties["itangcent_intellij_version"]}") {
        exclude("com.google.inject")
        exclude("com.google.code.gson")
    }

    implementation("com.itangcent:intellij-groovy-support:${properties["itangcent_intellij_version"]}") {
        exclude("com.google.inject")
        exclude("com.google.code.gson")
    }

//    implementation("com.itangcent:intellij-scala-support:${properties["itangcent_intellij_version"]}") {
//        exclude("com.google.inject")
//        exclude("com.google.code.gson")
//    }

    implementation("com.google.inject:guice:4.2.2") {
        exclude("org.checkerframework", "checker-compat-qual")
        exclude("com.google.guava", "guava")
    }

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")

    // https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation("org.jsoup:jsoup:1.12.1")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.2")

    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation("org.xerial:sqlite-jdbc:3.34.0")

    // https://search.maven.org/artifact/org.mockito.kotlin/mockito-kotlin/3.2.0/jar
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")

    // https://mvnrepository.com/artifact/org.mockito/mockito-inline
    testImplementation("org.mockito:mockito-inline:3.11.0")

    testImplementation("com.itangcent:intellij-idea-test:${properties["itangcent_intellij_version"]}")

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${properties["junit_version"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties["junit_version"]}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${properties["junit_version"]}")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.7.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.8.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

intellij {
    version.set(intellijVersion)
    type.set("IC")
    pluginName.set("easy-api")
    sandboxDir.set("idea-sandbox")
    plugins.set(listOf("java"))
}

tasks {
    patchPluginXml {
        pluginDescription.set(file("parts/pluginDescription.html").readText())
        changeNotes.set(file("parts/pluginChanges.html").readText())

        sinceBuild.set(intellijSince)
        untilBuild.set("")
    }
}
tasks.withType<JavaCompile>(){
    options.encoding="UTF-8"
}
tasks.withType<Javadoc>(){
    options.encoding="UTF-8"
}