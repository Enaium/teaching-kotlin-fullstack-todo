plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jimmer)
    application
}

group = "cn.enaium"
version = "1.0.0"

application {
    mainClass = "cn.enaium.TodoKt"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["development"] ?: "false"}")
}

dependencies {
    implementation(libs.bundles.api)
}