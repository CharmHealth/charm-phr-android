# Add project specific ProGuard rules here.
# R8 full mode enabled: obfuscation ON, optimization ON, shrinkResources ON
# mapping.txt generated at app/build/outputs/mapping/commonRelease/mapping.txt — archive per versionCode

#-dontwarn **
-dontoptimize
-dontobfuscate
-dontusemixedcaseclassnames
-verbose

# Crash readability: file names + exact line numbers survive obfuscation; all frames show "SourceFile:N"
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Emit mapping.txt for crash deobfuscation — archive per versionCode, upload to Play Console / Apptics
-printmapping mapping.txt

# Annotations must be retained for Dagger and DataBinding code generation
-keepattributes *Annotation*

# Flatten all non-kept packages into a single obfuscated namespace — further reduces DEX string table
-repackageclasses 'p'

# Application subclass (also covered by the wildcard rule below, kept explicitly for clarity)
-keep class com.charm.phr.mphr.MPHRApplication { *; }

-keep class com.charm.phr.mphr.model.database.entity.** { *; }

-keep class androidx.core.app.CoreComponentFactory { *; }
-keep class javax.annotation.** { *; }

# Enums — keep constants (VALUES/INSTANCE) so ordinal-based TypeConverters and name() calls are safe
-keepclassmembers enum * { *; }

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

-keep class com.zoho.apptics.** { *; }
-dontwarn com.zoho.apptics.**

-keep class com.google.android.material.R$drawable { *; }


# General Android / Kotlin / App rules
-keep class * extends android.app.Application { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.content.ContentProvider { *; }

# Keep Kotlin metadata (for reflection / serialization)
-keepclassmembers class kotlin.Metadata { *; }

# Room (entities, dao, databases)
-keep class androidx.room.** { *; }
-keep @androidx.room.Dao public interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.* { *; }
-keepattributes Signature

# Dagger / Dependency Injection
-keep class dagger.** { *; }
-dontwarn dagger.**
-keep class javax.inject.** { *; }
-dontwarn javax.inject.**

# Firebase Messaging
-keep class com.google.firebase.messaging.FirebaseMessagingService { *; }

# EventBus
-keepclassmembers class ** {
    public void onEvent*(**);
}

# Keep all Joda-Time classes
-keep class org.joda.time.** { *; }
-keepclassmembers class net.danlew.android.joda.R$raw { *; }


# Lottie
-keep class com.airbnb.lottie.** { *; }

# WorkManager
-keep class androidx.work.** { *; }

-dontwarn com.google.zxing.**

# Zoom SDK — keep entire us.zoom.* namespace (SDK has 60+ internal packages beyond us.zoom.sdk
# that use reflection and JNI; us.zoom.sdk.** alone is insufficient)
-keep class us.zoom.** { *; }
-keep class com.zipow.** { *; }
-keep class us.zipow.** { *; }
-keep class org.webrtc.** { *; }
-keep class us.google.protobuf.** { *; }
-keep class com.google.crypto.tink.** { *; }
-keep class androidx.security.crypto.** { *; }

# Gson model fields - no @SerializedName used; field names must survive R8 optimization
-keepclassmembers class com.charm.phr.mphr.model.** {
    <fields>;
}

# Retrofit API interface - dynamic proxy requires all annotated methods to be retained
-keep interface com.charm.phr.mphr.util.WebService { *; }

# WorkManager Workers — class names must survive obfuscation (referenced via Class.forName() in NotificationWorkerFactory)
-keep class com.charm.phr.mphr.service.notification.RegisterDeviceWorker { *; }
-keep class com.charm.phr.mphr.service.notification.UnRegisterDeviceWorker { *; }
-keep class com.charm.phr.mphr.service.reminders.FetchReminderDataWorker { *; }
-keep class com.charm.phr.mphr.service.reminders.ScheduleReminderPerDayWorker { *; }

# Kotlin coroutines — volatile fields must not be renamed (internal coroutine machinery)
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- R8 missing class suppressions (transitive dependencies not shipped in APK) ---

# Google Play Billing (Zoom SDK billing integration)
-dontwarn com.android.billingclient.api.**

# Google HTTP Client (transitive via Google Tink)
-dontwarn com.google.api.client.http.**

# ML Kit Text Recognition (Zoom SDK package detector)
-dontwarn com.google.mlkit.vision.**

# Microsoft Intune MAM (Zoom SDK Intune integration)
-dontwarn com.microsoft.intune.mam.**

# SmartRefreshLayout (Zoom SDK messaging UI)
-dontwarn com.scwang.smart.refresh.**

# Symbol EMDK (Zoom SDK device management)
-dontwarn com.symbol.emdk.**

# Zoom third-party login / CN login / Intune
-dontwarn com.zipow.cnthirdparty.**
-dontwarn us.zoom.intunelib.**
-dontwarn us.zoom.thirdparty.**

# Zoom SDK 6.7.5 — Kotlin UUID and xCrash
-dontwarn kotlin.uuid.ExperimentalUuidApi
-dontwarn kotlin.uuid.Uuid$Companion
-dontwarn kotlin.uuid.Uuid
-dontwarn xcrash.**

# Zoom SDK 6.7.5 — internal classes referenced but not shipped in Meeting SDK AAR
-dontwarn com.google.android.play.core.**
-dontwarn com.zipow.videobox.**
-dontwarn us.zoom.android.**
-dontwarn us.zoom.apm.**
-dontwarn us.zoom.component.**
-dontwarn us.zoom.core.**
-dontwarn us.zoom.meeting.**
-dontwarn us.zoom.uicommon.**
-dontwarn us.zoom.zmsg.**

# AWT / Swing (coroutines dispatcher, not available on Android)
-dontwarn java.awt.**
-dontwarn javax.swing.**

# Annotation processing model API (Zoom SDK build artifacts)
-dontwarn javax.lang.model.**

# ProGuard annotations (Razorpay SDK)
-dontwarn proguard.annotation.**
