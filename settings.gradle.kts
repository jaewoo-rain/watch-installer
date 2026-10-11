// 웨어 인스톨러 — 단독 Gradle 프로젝트.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // libadb-android 는 JitPack 에만 올라와 있다.
        maven("https://jitpack.io") {
            content { includeGroupAndSubgroups("com.github.MuntashirAkon") }
        }
    }
}

rootProject.name = "watch-installer"
include(":app")
