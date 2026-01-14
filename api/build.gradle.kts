plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.slf4j)
    api(libs.guava)

    api(libs.adventure.nbt)
    api(libs.adventure.minimessage)
}
