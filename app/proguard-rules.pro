# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/and/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
-dontobfuscate
-dontwarn org.hamcrest.**
-dontwarn com.squareup.**
-dontwarn com.google.android.**

-keep class com.google.android.* {
   *;
}
-keep class com.google.common.* {
   *;
}
-keep class org.hamcrest.* {
   *;
}
#For design support library
-keep class android.support.design.widget.* { *; }
-keep interface android.support.design.widget.* { *; }

# Keep generic signatures; needed for correct type resolution
-keepattributes Signature

# Keep Gson annotations
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Keep class TypeToken (respectively its generic signature) if present
-if class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken

# Keep any (anonymous) classes extending TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken

# Keep classes with @JsonAdapter annotation
-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *

# Keep fields with any other Gson annotation
# Also allow obfuscation, assuming that users will additionally use @SerializedName or
# other means to preserve the field names
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}

# Keep no-args constructor of classes which can be used with @JsonAdapter
# By default their no-args constructor is invoked to create an adapter instance
-keepclassmembers class * extends com.google.gson.TypeAdapter {
  <init>();
}
-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonSerializer {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonDeserializer {
  <init>();
}

# Keep fields annotated with @SerializedName for classes which are referenced.
# If classes with fields annotated with @SerializedName have a no-args
# constructor keep that as well.
-if class *
-keepclasseswithmembers,allowobfuscation class <1> {
  @com.google.gson.annotations.SerializedName <fields>;
}
-if class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}