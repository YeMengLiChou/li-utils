package com.li.utils.framework.util

import android.util.Base64
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 *
 * AES 加密工具类
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */
object AESUtils {
    // 加密
    fun encrypt(
        src: String,
        key: String,
        iv: String,
        charset: Charset = Charset.forName("UTF-8"),
    ): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val rawKey = key.toByteArray()
        val keySpec = SecretKeySpec(rawKey, "AES")
        val ivSpec = IvParameterSpec(iv.toByteArray()) // 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(src.toByteArray(charset))
        return Base64.encode(encrypted, Base64.DEFAULT).toString(charset)
    }

    // 解密
    fun decrypt(
        src: String,
        key: String,
        iv: String,
        charset: Charset = Charset.forName("UTF-8")
    ): String {
        val rawKey = key.toByteArray(charset("ASCII"))
        val keySpec = SecretKeySpec(rawKey, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSpec = IvParameterSpec(iv.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val encrypted: ByteArray = Base64.decode(src, Base64.DEFAULT) //先用base64解密
        val original = cipher.doFinal(encrypted)
        return String(original, charset)
    }

}