pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Force AAPT2 from Maven (avoids x86_64 binary on ARM64)
gradle.startParameter.projectProperties["android.aapt2FromMaven"] = "true"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FocalboardAndroid"
include(":app")
