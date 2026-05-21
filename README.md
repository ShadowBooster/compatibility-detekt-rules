[![](https://jitpack.io/v/ShadowBooster/compatibility-detekt-rules.svg)](https://jitpack.io/#ShadowBooster/compatibility-detekt-rules)
# Compatibitity-Detekt-Rules
A basic extenction of Detekt rules to detect compatibility issues. 

This is made for a bachelor thesis project for University of Twente. 

## usage
Step 1. Add the JitPack repository to your build file.

Add it in your settings.gradle.kts at the end of repositories:
```kotlin
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") }
		}
	}
```

Step 2. Add the dependency
```kotlin
	dependencies {
	        implementation("com.github.ShadowBooster:compatibility-detekt-rules:Tag")
	}
```

## Configuration
TODO