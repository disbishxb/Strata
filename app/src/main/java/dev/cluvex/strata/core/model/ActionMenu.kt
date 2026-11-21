package dev.cluvex.strata.core.model

import dev.cluvex.strata.R

enum class ActionMenu(val label: String) {
    SELECT("选择"),
    OPEN("打开"),
    OPEN_WITH("打开方式"),
    OPEN_AS_TEXT("作为文本打开"),
    HEX("十六进制查看"),
    RENAME("重命名"),
    SHARE("分享"),
    CHECKSUM("校验和"),
    EXTRACT("解压"),
    VIEW("预览压缩包"),
    SIGN_APK("签名 APK"),
    COMPRESS("压缩"),
    BOOKMARK("添加书签"),
    CREATE_LINK("创建链接"),
    SPLIT("分割文件"),
    ENCRYPT("加密"),
    DECRYPT("解密"),
    PROPERTIES("属性"),
    DELETE("删除")
}
