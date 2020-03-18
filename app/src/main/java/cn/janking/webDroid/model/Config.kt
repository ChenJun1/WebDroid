package cn.janking.webDroid.model

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * 用于webDroid的配置项
 */
class Config private constructor() {
    /**
     * 是否是预览模式
     */
    @Transient
    var preview = false

    /**
     * APP的名称
     */
    var appName = ""

    /**
     * APP包名
     */
    var appPackage = ""

    /**
     * APP图标
     */
    var appIcon = ""

    /**
     * tab的标题集合
     */
    var tabTitles: List<String> = List(0) { "" }

    /**
     * viewPager的url集合
     */
    var tabUrls: List<String> = List(0) { "" }

    /**
     * tab的icon集合，表示文件位置
     */
    var tabIcons: List<String> = List(0) { "" }

    /**
     * tab的个数
     */
    @Transient
    var tabCount = 0

    /**
     * tab的风格
     * 0 : top
     * 1 : bottom
     */
    var tabStyle = 1

    companion object {
        private val gson = Gson()

        /**
         * 单例
         */
        var instance: Config = Config()
            private set

        /**
         * 读取json字符串
         */
        @Throws(JsonSyntaxException::class)
        fun readFromString(configString: String?) {
            instance = gson.fromJson(
                configString,
                Config::class.java
            ).let {
                it?.apply {
                    tabCount = tabTitles.size.coerceAtMost(tabUrls.size).coerceAtMost(tabIcons.size)
                } ?: Config()
            }
        }

        /**
         * 生成json配置文件
         */
        fun toJsonString(): String {
            return gson.toJson(instance)
        }
    }
}