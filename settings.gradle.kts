plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "cardinal"

includeBuild("build-src")

include("code-generators")
include("testing")

include("jmh-benchmarks")
include("jcstress-tests")

include("demo")
