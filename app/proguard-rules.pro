-keepattributes Signature,Exceptions,*Annotation*,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson 数据类
-keep class com.example.piano.data.**.dto.** { *; }
-keep class com.example.piano.data.**.response.** { *; }
-keep class com.example.piano.data.**.request.** { *; }
-keep class com.example.piano.core.network.model.** { *; }

# Retrofit API
-keep interface com.example.piano.data.**.api.** { *; }

# 枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 密封类子类
-keep class com.example.piano.ui.**.*State$* { *; }
-keep class com.example.piano.core.network.util.ResponseState$* { *; }
-keep class com.example.piano.core.audio.PitchResult$* { *; }

# ViewModel (Hilt)
-keep class com.example.piano.ui.**.*ViewModel { *; }

# MIDI / Audio
-keep class com.example.piano.core.midi.** { *; }
-keep class com.example.piano.core.audio.** { *; }

# 移除日志
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
