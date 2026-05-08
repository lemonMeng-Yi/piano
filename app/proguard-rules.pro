# === Retrofit / OkHttp ===
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# === Gson ===
# Gson 内部类
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
# 保留 Gson TypeAdapter / JsonSerializer / JsonDeserializer 实现
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# === Gson 序列化 DTO（保留类完整签名，否则泛型 ParameterizedType 报 ClassCastException） ===
-keep class com.example.piano.data.**.dto.** { *; }
-keep class com.example.piano.data.**.response.** { *; }
# BaseResult 泛型包装类必须保留完整签名
-keep class com.example.piano.core.network.model.** { *; }
# Gson @SerializedName 注解的字段不被混淆
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# 保留无参构造函数（Gson 反序列化用）
-keepclassmembers class * {
    <init>();
}

# === Retrofit API 接口 ===
-keep interface com.example.piano.data.**.api.** { *; }

# === 枚举（valueOf 反射调用） ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === 密封类子类（when 穷举检查） ===
-keep class com.example.piano.ui.home.HotSheetsUiState$* { *; }
-keep class com.example.piano.ui.home.RecentPlaysUiState$* { *; }
-keep class com.example.piano.ui.courses.learn.CoursesUiState$* { *; }
-keep class com.example.piano.ui.courses.learn.CourseDetailUiState$* { *; }
-keep class com.example.piano.ui.courses.sheet.SheetListUiState$* { *; }
-keep class com.example.piano.ui.courses.sheet.SheetFavoritesUiState$* { *; }
-keep class com.example.piano.ui.courses.sheet.SheetDetailUiState$* { *; }
-keep class com.example.piano.ui.courses.sheet.SheetAudioState$* { *; }
-keep class com.example.piano.ui.practice.VirtualKeyboardPracticeUiState$* { *; }
-keep class com.example.piano.core.network.util.ResponseState$* { *; }
-keep class com.example.piano.core.audio.PitchResult$* { *; }

# === 导航参数类 ===
-keep class com.example.piano.navigation.** { *; }

# === Compose ViewModel（Hilt 反射创建） ===
-keep class com.example.piano.ui.**.viewmodel.** { *; }
-keep class com.example.piano.ui.**.*ViewModel { *; }

# === MIDI / Audio 核心实现 ===
-keep class com.example.piano.core.midi.** { *; }
-keep class com.example.piano.core.audio.** { *; }

# === 反射调试信息保留（生产 crash 堆栈可定位） ===
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# === WebView（如有使用） ===
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# === 移除日志（减小体积） ===
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
