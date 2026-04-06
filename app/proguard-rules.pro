# Add project specific ProGuard rules here.
# Keep model classes for serialization
-keep class com.focalboard.android.data.model.** { *; }
-keepclassmembers class com.focalboard.android.data.model.** { *; }

# Keep Retrofit
-keepattributes Signature
-keepattributes Annotation
-keep class retrofit2.** { *; }
