# Keep Hilt and Dagger classes
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *

# Keep Firebase and Google Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Data Models (POJOs) used for Serialization (e.g., Firestore)
-keepclassmembers class com.theseuntaylor.hexo.data.model.** { *; }
-keep @com.google.firebase.firestore.IgnoreExtraProperties class *

# Keep Retrofit and OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile