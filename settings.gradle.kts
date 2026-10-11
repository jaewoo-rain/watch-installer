// 워치 설치 도우미 — 스논 Flutter 앱과 별개인 독립 Gradle 프로젝트.
// frontend/android/settings.gradle.kts 에 include 하지 않는다.
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
