package com.li.utils.framework.manager

import android.app.Application
import android.util.Log
import com.li.utils.ext.common.fromJson
import com.li.utils.ext.common.toJson
import io.fastkv.FastKV
import io.fastkv.FastKVConfig
import io.fastkv.interfaces.FastCipher
import io.fastkv.interfaces.FastEncoder
import io.fastkv.interfaces.FastLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 键值对管理器
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */
object KVManager {
    private var initialized = false

    private lateinit var mApplication: Application

    private const val KV_NAME_ENCRYPTED = "kv_encrypted"

    private const val KV_NAME_UNENCRYPTED = "kv_unencrypted"

    /**
     * 自定义 Encoder 集合
     * */
    private val encoders: MutableList<FastEncoder<*>> = mutableListOf(
        SetStringEncoder
    )

    /**
     * 加密专用 kv
     * */
    val encryptedKv: FastKV by lazy {
        checkInitialized()
        FastKV.Builder(mApplication, KV_NAME_ENCRYPTED)
            .cipher(LCipher) // 加密
            .encoder(encoders.toTypedArray())
            .build()
    }

    /**
     * 无加密 kv
     * */
    val kv: FastKV by lazy {
        checkInitialized()
        FastKV.Builder(mApplication, KV_NAME_UNENCRYPTED)
            .encoder(encoders.toTypedArray())
            .build()
    }

    /**
     * 初始化
     * @param application
     * @param encoders
     * */
    fun init(application: Application, encoders: MutableList<FastEncoder<*>>) {
        initialized = true
        mApplication = application
        KVManager.encoders.addAll(encoders)
        FastKVConfig.setLogger(Logger)
        FastKVConfig.setExecutor(Dispatchers.Default.asExecutor())

    }

    private fun checkInitialized() {
        check(initialized) {
            "You need init KvManager first!"
        }
    }

    object SetStringEncoder : FastEncoder<Set<String>> {
        override fun tag(): String {
            return "Set<String>"
        }

        override fun encode(obj: Set<String>): ByteArray {
            return obj.toJson()!!.toByteArray()
        }

        override fun decode(bytes: ByteArray, offset: Int, length: Int): Set<String> {
            return bytes.decodeToString(offset, offset + length).fromJson<Set<String>>()!!
        }
    }

    // ===========================================================

    /** 日志 */
    internal object Logger : FastLogger {
        override fun i(name: String, message: String) {
            Log.i(name, message)
        }

        override fun w(name: String, e: Exception) {
            Log.w(name, e.message.toString())
            e.printStackTrace()
        }

        override fun e(name: String, e: Exception) {
            Log.e(name, e.message.toString())
            e.printStackTrace()
        }
    }

    /**
     * AES 加密
     * */
    internal object LCipher : FastCipher {

        private const val key = "ef8373c04bfd11ea"

        private const val iv = "0102030405060708"

        private val numberCipher = NumberCipher(sha256(key.toByteArray()))

        private val encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        private val decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        private val keySpec = SecretKeySpec(key.toByteArray(), "AES")

        private val ivSpec = IvParameterSpec(iv.toByteArray()) // 使用CBC模式，需要一个向量iv，可增加加密算法的强度

        init {
            encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        }

        override fun encrypt(src: ByteArray): ByteArray = encryptCipher.doFinal(src)

        override fun encrypt(src: Int): Int = numberCipher.encryptInt(src)

        override fun encrypt(src: Long): Long = numberCipher.decryptLong(src)

        override fun decrypt(dst: ByteArray): ByteArray = decryptCipher.doFinal(dst)

        override fun decrypt(dst: Int): Int = numberCipher.decryptInt(dst)

        override fun decrypt(dst: Long): Long = numberCipher.decryptLong(dst)

        private fun sha256(bytes: ByteArray): ByteArray {
            try {
                return MessageDigest.getInstance("SHA-256").digest(bytes)
            } catch (ignore: Exception) {
            }
            return bytes.copyOf(32)
        }
    }


    /**
     * Use encryption operator of AES to encrypt/decrypt number.
     * <br></br>
     * Link to [LongEncrypt](https://github.com/BillyWei01/LongEncrypt/tree/master)
     * and [aes.c](https://github.com/openluopworld/aes_128/blob/master/aes.c)
     */
    internal class NumberCipher(key: ByteArray?) {
        private val key: ByteArray

        /**
         * @param key require a key with length of 32.
         */
        init {
            require(!(key == null || key.size != KEY_LEN)) { "The key must be length of $KEY_LEN" }
            this.key = key
        }

        fun encryptLong(value: Long): Long {
            val state = long2Bytes(value)
            for (i in 0 until ROUND) {
                val offset = i shl 3
                for (j in 0..7) {
                    // AddRoundKey and SubBytes
                    state[j] = S_BOX[state[j].toInt() xor key[offset + j].toInt() and 0xFF]
                }
                shift_rows(state)
                multiply(state)
                multiply_4(state)
            }
            for (j in 0..7) {
                state[j] = (state[j].toInt() xor key[(ROUND shl 3) + j].toInt()).toByte()
            }
            return bytes2Long(state)
        }

        fun decryptLong(value: Long): Long {
            val state = long2Bytes(value)
            for (j in 0..7) {
                state[j] = (state[j].toInt() xor key[(ROUND shl 3) + j].toInt()).toByte()
            }
            for (i in ROUND - 1 downTo 0) {
                inv_multiply(state, 0)
                inv_multiply(state, 4)
                inv_shift_rows(state)
                val offset = i shl 3
                for (j in 0..7) {
                    state[j] = (INV_S_BOX[state[j].toInt() and 0xFF].toInt() xor key[offset + j].toInt()).toByte()
                }
            }
            return bytes2Long(state)
        }

        fun encryptInt(value: Int): Int {
            val state = int2Bytes(value)
            for (i in 0 until ROUND) {
                val offset = i shl 2
                for (j in 0..3) {
                    state[j] = S_BOX[state[j].toInt() xor key[offset + j].toInt() and 0xFF]
                }
                multiply(state)
            }
            for (j in 0..3) {
                state[j] = (state[j].toInt() xor key[(ROUND shl 2) + j].toInt()).toByte()
            }
            return bytes2Int(state)
        }

        fun decryptInt(value: Int): Int {
            val state = long2Bytes(value.toLong())
            for (j in 0..3) {
                state[j] = (state[j].toInt() xor key[(ROUND shl 2) + j].toInt()).toByte()
            }
            for (i in ROUND - 1 downTo 0) {
                inv_multiply(state, 0)
                for (j in 0..3) {
                    val offset = i shl 2
                    state[j] = (INV_S_BOX[state[j].toInt() and 0xFF].toInt() xor key[offset + j].toInt()).toByte()
                }
            }
            return bytes2Int(state)
        }

        companion object {
            private const val ROUND = 3
            const val KEY_LEN = (ROUND + 1) * 8
            private val S_BOX = byteArrayOf(
                99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118,
                -54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64,
                -73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21,
                4, -57, 35, -61, 24, -106, 5, -102, 7, 18, -128, -30, -21, 39, -78, 117,
                9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124,
                83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49,
                -48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, 127, 80, 60, -97, -88,
                81, -93, 64, -113, -110, -99, 56, -11, -68, -74, -38, 33, 16, -1, -13, -46,
                -51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115,
                96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37,
                -32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121,
                -25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8,
                -70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118,
                112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98,
                -31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33,
                -116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22
            )
            private val INV_S_BOX = byteArrayOf(
                82, 9, 106, -43, 48, 54, -91, 56, -65, 64, -93, -98, -127, -13, -41, -5,
                124, -29, 57, -126, -101, 47, -1, -121, 52, -114, 67, 68, -60, -34, -23, -53,
                84, 123, -108, 50, -90, -62, 35, 61, -18, 76, -107, 11, 66, -6, -61, 78,
                8, 46, -95, 102, 40, -39, 36, -78, 118, 91, -94, 73, 109, -117, -47, 37,
                114, -8, -10, 100, -122, 104, -104, 22, -44, -92, 92, -52, 93, 101, -74, -110,
                108, 112, 72, 80, -3, -19, -71, -38, 94, 21, 70, 87, -89, -115, -99, -124,
                -112, -40, -85, 0, -116, -68, -45, 10, -9, -28, 88, 5, -72, -77, 69, 6,
                -48, 44, 30, -113, -54, 63, 15, 2, -63, -81, -67, 3, 1, 19, -118, 107,
                58, -111, 17, 65, 79, 103, -36, -22, -105, -14, -49, -50, -16, -76, -26, 115,
                -106, -84, 116, 34, -25, -83, 53, -123, -30, -7, 55, -24, 28, 117, -33, 110,
                71, -15, 26, 113, 29, 41, -59, -119, 111, -73, 98, 14, -86, 24, -66, 27,
                -4, 86, 62, 75, -58, -46, 121, 32, -102, -37, -64, -2, 120, -51, 90, -12,
                31, -35, -88, 51, -120, 7, -57, 49, -79, 18, 16, 89, 39, -128, -20, 95,
                96, 81, 127, -87, 25, -75, 74, 13, 45, -27, 122, -97, -109, -55, -100, -17,
                -96, -32, 59, 77, -82, 42, -11, -80, -56, -21, -69, 60, -125, 83, -103, 97,
                23, 43, 4, 126, -70, 119, -42, 38, -31, 105, 20, 99, 85, 33, 12, 125
            )

            /*
        private static byte mul2(byte a) {
            return (byte) (((a & 0x80) != 0) ? ((a << 1) ^ 0x1b) : (a << 1));
        }
        */
            private val mul2 = ByteArray(256)

            init {
                for (i in 0..127) {
                    mul2[i] = (i shl 1).toByte()
                }
                for (i in 128..255) {
                    mul2[i] = (i shl 1 xor 0x1b).toByte()
                }
            }

            /*
         * [b0]	  [02 03 01 01]   [b0]
         * [b1]	= [01 02 03 01] . [b1]
         * [b2]	  [01 01 02 03]   [b2]
         * [b3]	  [03 01 01 02]   [b3]
         */
            private fun multiply(b: ByteArray) {
                val a0 = (b[0].toInt() xor b[1].toInt()).toByte()
                val a1 = (b[1].toInt() xor b[2].toInt()).toByte()
                val a2 = (b[2].toInt() xor b[3].toInt()).toByte()
                val a3 = (b[3].toInt() xor b[0].toInt()).toByte()
                val t = (a0.toInt() xor a2.toInt()).toByte()
                b[0] = (b[0].toInt() xor (mul2[a0.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
                b[1] = (b[1].toInt() xor (mul2[a1.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
                b[2] = (b[2].toInt() xor (mul2[a2.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
                b[3] = (b[3].toInt() xor (mul2[a3.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
            }

            private fun multiply_4(b: ByteArray) {
                val a0 = (b[4].toInt() xor b[5].toInt()).toByte()
                val a1 = (b[5].toInt() xor b[6].toInt()).toByte()
                val a2 = (b[6].toInt() xor b[7].toInt()).toByte()
                val a3 = (b[7].toInt() xor b[4].toInt()).toByte()
                val t = (a0.toInt() xor a2.toInt()).toByte()
                b[4] = (b[4].toInt() xor (mul2[a0.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
                b[5] = (b[5].toInt() xor (mul2[a1.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
                b[6] = (b[6].toInt() xor (mul2[a2.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
                b[7] = (b[7].toInt() xor (mul2[a3.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
            }

            /*
         * [d0]	  [0e 0b 0d 09]   [b0]
         * [d1]	= [09 0e 0b 0d] . [b1]
         * [d2]	  [0d 09 0e 0b]   [b2]
         * [d3]	  [0b 0d 09 0e]   [b3]
         */
            private fun inv_multiply(b: ByteArray, i: Int) {
                var u = (b[i].toInt() xor b[i + 2].toInt()).toByte()
                var v = (b[i + 1].toInt() xor b[i + 3].toInt()).toByte()
                if (i == 0) {
                    multiply(b)
                } else if (i == 4) {
                    multiply_4(b)
                } else {
                    throw IllegalArgumentException("invalid i:$i")
                }
                u = mul2[mul2[u.toInt() and 0xFF].toInt() and 0xFF]
                v = mul2[mul2[v.toInt() and 0xFF].toInt() and 0xFF]
                val t = mul2[u.toInt() xor v.toInt() and 0xFF]
                u = (u.toInt() xor t.toInt()).toByte()
                v = (v.toInt() xor t.toInt()).toByte()
                b[i] = (b[i].toInt() xor u.toInt()).toByte()
                b[i + 1] = (b[i + 1].toInt() xor v.toInt()).toByte()
                b[i + 2] = (b[i + 2].toInt() xor u.toInt()).toByte()
                b[i + 3] = (b[i + 3].toInt() xor v.toInt()).toByte()
            }

            private fun shift_rows(state: ByteArray) {
                val t1 = state[7]
                val t0 = state[6]
                state[7] = state[5]
                state[6] = state[4]
                state[5] = state[3]
                state[4] = state[2]
                state[3] = state[1]
                state[2] = state[0]
                state[1] = t1
                state[0] = t0
            }

            private fun inv_shift_rows(state: ByteArray) {
                val t0 = state[0]
                val t1 = state[1]
                state[0] = state[2]
                state[1] = state[3]
                state[2] = state[4]
                state[3] = state[5]
                state[4] = state[6]
                state[5] = state[7]
                state[6] = t0
                state[7] = t1
            }

            fun int2Bytes(value: Int): ByteArray {
                val state = ByteArray(4)
                state[3] = (value shr 24).toByte()
                state[2] = (value shr 16).toByte()
                state[1] = (value shr 8).toByte()
                state[0] = value.toByte()
                return state
            }

            fun bytes2Int(state: ByteArray): Int {
                return (state[3].toInt() and 0xFF shl 24) +
                        (state[2].toInt() and 0xFF shl 16) +
                        (state[1].toInt() and 0xFF shl 8) +
                        (state[0].toInt() and 0xFF)
            }

            fun long2Bytes(value: Long): ByteArray {
                val state = ByteArray(8)
                state[7] = (value shr 56).toByte()
                state[6] = (value shr 48).toByte()
                state[5] = (value shr 40).toByte()
                state[4] = (value shr 32).toByte()
                state[3] = (value shr 24).toByte()
                state[2] = (value shr 16).toByte()
                state[1] = (value shr 8).toByte()
                state[0] = value.toByte()
                return state
            }

            fun bytes2Long(state: ByteArray): Long {
                return (state[7].toLong() shl 56) +
                        ((state[6].toInt() and 0xFF).toLong() shl 48) +
                        ((state[5].toInt() and 0xFF).toLong() shl 40) +
                        ((state[4].toInt() and 0xFF).toLong() shl 32) +
                        ((state[3].toInt() and 0xFF).toLong() shl 24) +
                        ((state[2].toInt() and 0xFF).toLong() shl 16) +
                        ((state[1].toInt() and 0xFF).toLong() shl 8) +
                        (state[0].toInt() and 0xFF).toLong()
            }
        }
    }
}
