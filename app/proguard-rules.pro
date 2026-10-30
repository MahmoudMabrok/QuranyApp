# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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



# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses. *Annotation* keeps @SerializedName / @Expose.
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Gson + R8 full mode.
# Model types below are instantiated only by Gson.fromJson (assets/quran.json,
# quran_clean.json, tafseer.json, and Room TypeConverters). R8 full mode otherwise
# treats them as never constructed, merges or deletes them, and rewrites
# Util.getFullQuranSurahs so the cast fails and the method returns null.
-keep class education.mahmoud.quranyapp.datalayer.model.** { *; }
-keep class education.mahmoud.quranyapp.model.** { *; }
# TypeToken<List<AyahItem>> in Util (no @SerializedName; Gson uses the field names).
-keep class education.mahmoud.quranyapp.datalayer.local.room.AyahItem { *; }

-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit


-dontwarn durdinapps.rxfirebase2.**

# for eventbus
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

