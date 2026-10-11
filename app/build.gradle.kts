plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.snown.watchinstaller"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.snown.watchinstaller"
        // 무선 디버깅 페어링은 Android 11(API 30)부터
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            // 내부 도구 — 배포 안 함. 디버그 빌드만 쓴다.
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += setOf("META-INF/versions/9/OSGI-INF/MANIFEST.MF", "META-INF/DEPENDENCIES")
        }
    }
}

dependencies {
    // ADB 프로토콜(무선 페어링 SPAKE2 + TLS 연결) — docs/DEVELOPMENT.md 의 "라이브러리 선택" 참고
    implementation("com.github.MuntashirAkon:libadb-android:3.1.1")
    // 워치 adbd 와 TLS 1.3 으로 붙기 위한 Conscrypt(라이브러리 README 권장)
    implementation("org.conscrypt:conscrypt-android:2.5.3")
    // 페어링용 자체 서명 인증서 생성(libadb 가 끌어오는 bcprov-jdk15to18 과 같은 버전)
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.81")

    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    testImplementation("junit:junit:4.13.2")
}
