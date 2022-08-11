import dependencies.Dependencies

plugins {
    id(BuildPlugins.ANDROID_APPLICATION)
    id(BuildPlugins.KOTLIN_ANDROID)
}

android {
    namespace = BuildAndroidConfig.APPLICATION_ID
    compileSdk = BuildAndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        applicationId = BuildAndroidConfig.APPLICATION_ID
        minSdk = BuildAndroidConfig.MIN_SDK_VERSION
        targetSdk = BuildAndroidConfig.TARGET_SDK_VERSION
        versionCode = BuildAndroidConfig.VERSION_CODE
        versionName = BuildAndroidConfig.VERSION_NAME
        
        vectorDrawables.useSupportLibrary = BuildAndroidConfig.SUPPORT_LIBRARY_VECTOR_DRAWABLES
    }

    signingConfigs {
        getByName(BuildType.DEBUG) {
            storeFile = file("testkey.keystore")
            storePassword = "testkey"
            keyAlias = "testkey"
            keyPassword = "testkey"
        }

        getByName(BuildType.RELEASE) {
            storeFile = file("testkey.keystore")
            storePassword = "testkey"
            keyAlias = "testkey"
            keyPassword = "testkey"
        }
    }

    buildTypes {
        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isTestCoverageEnabled = BuildTypeDebug.isTestCoverageEnabled
            applicationIdSuffix = BuildTypeDebug.applicationIdSuffix
            versionNameSuffix = BuildTypeDebug.versionNameSuffix
        }
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            isTestCoverageEnabled = BuildTypeRelease.isTestCoverageEnabled
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
    }

    lintOptions {
        isAbortOnError = false
        isCheckDependencies = true
    }

    packagingOptions {
        resources.excludes.addAll(
            arrayOf(
                "plugin.xml",
                ".options",
                "about.xml",
                "plugin.properties",
                "baksmali.properties",
                ".api_description",
                "META-INF/*",
                "README.md",
                "SECURITY.md",
                "about_files/*",
                "OSGI-INF/*",
                "ant_tasks/*",
                "api_database/*",
                "license/*",
                "systembundle.properties",
                "CDC-*_Foundation-*.profile",
                "JRE-1.1.profile",
                "DebugProbesKt.bin",
                "J2SE-*.profile",
                "JavaSE-*.profile",
                "JavaSE_compact*-1.8.profile",
                "OSGi_Minimum-*.profile",
                "OSGI-OPT/**",
                "jdtCompilerAdapter.jar"
            )
        )  
    }
}

dependencies {
    coreLibraryDesugaring(Dependencies.CORE_LIBRARY_DESUGARING)

    debugImplementation(Dependencies.LEAK_CANARY)

    implementation(Dependencies.CFR)
    implementation(Dependencies.DEXLIB2)
    implementation(Dependencies.BAKSMALI)
    implementation(Dependencies.GUAVA)
    implementation(Dependencies.APPCOMPAT)
    implementation "androidx.recyclerview:recyclerview:1.3.0-beta01"
    implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01"
    implementation(Dependencies.MATERIAL)
    implementation(projects.common)
    implementation(projects.soraEditor)
    implementation(projects.androidCompiler)
    implementation(projects.projectCreator)
}