# Compatibitity-Detekt-Rules
A basic extenction of Detekt rules to detect compatibility issues. 

This is made for a bachelor thesis project for University of Twente. 

## usage
In the dependencies block of your build.gradle file, add the following:
```kotlin
detektPlugins("org.shadowbooster.detekt:compatibility-detekt-rules:0.1")
```

or if you're using a libs.version.toml file, add this there:
```kotlin
compatibility-detekt-rules = { module = "org.shadowbooster.detekt:compatibility-detekt-rules", version = "0.1" }
```

and this in your build.gradle file:
```kotlin
detektPlugins(rootProject.libs.shadowbooster.detekt.rules)
```

You can find the latest version [here]()

## Configuration
TODO