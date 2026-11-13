package externius.rdmg.helpers

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.core.graphics.scale
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.TrapDescription
import java.io.ByteArrayOutputStream

object Export {
    const val MAX_EXPORT_BITMAP_DIMENSION = 2048

    @JvmStatic
    fun generateHTML(
        bmp: Bitmap?,
        roomDescription: List<RoomDescription>?,
        trapDescription: List<TrapDescription>?,
        roamingMonsterDescription: List<RoamingMonsterDescription>?,
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(
            """
            <html>
            <head>
            <title>DungeonMap</title>
            <style>table, th, td {vertical-align: middle;} .wrap {white-space: pre-wrap;} .root {background-color: lightgray; text-align: center; font-weight: bold;}</style>
            </head>
            <body>
            <div>
        """.trimIndent()
        )
        val base64Img = bitmapToBase64String(bmp)
        if (base64Img.isNotEmpty()) {
            stringBuilder.append("<img src=\"data:image/png;base64,")
                .append(base64Img)
                .append("\">\n")
        }
        stringBuilder.append("<table id=\"table_description\" class=\"wrap\">")
        roomDescription?.let {
            addRoomDescription(it, stringBuilder)
        }
        addTrapDescription(trapDescription, stringBuilder)
        addRoamingMonsters(roamingMonsterDescription, stringBuilder)
        stringBuilder.append(
            """
            </table>
            </div>
            </body>
            </html>
        """.trimIndent()
        )
        return stringBuilder.toString()
    }

    private fun addRoomDescription(
        roomDescription: List<RoomDescription>,
        stringBuilder: StringBuilder
    ) {
        for ((name, treasure, monster, doors) in roomDescription) {
            stringBuilder.append("<tr><td colspan=\"5\" class=\"root\">")
            stringBuilder.append(name)
            stringBuilder.append("</td></tr>\n <tr><td>")
            stringBuilder.append(monster)
            stringBuilder.append("</td></tr>\n <tr><td>")
            stringBuilder.append(treasure)
            stringBuilder.append("</td></tr>\n <tr><td>")
            stringBuilder.append(doors)
            stringBuilder.append("</td></tr>")
        }
    }

    private fun addTrapDescription(
        trapDescription: List<TrapDescription>?,
        stringBuilder: StringBuilder
    ) {
        if (!trapDescription.isNullOrEmpty()) {
            for ((name, description) in trapDescription) {
                appendToStringBuilder(stringBuilder, name, description)
            }
        }
    }

    private fun addRoamingMonsters(
        roamingMonsterDescription: List<RoamingMonsterDescription>?,
        stringBuilder: StringBuilder
    ) {
        if (!roamingMonsterDescription.isNullOrEmpty()) {
            for ((name, description) in roamingMonsterDescription) {
                appendToStringBuilder(stringBuilder, name, description)
            }
        }
    }

    private fun appendToStringBuilder(
        stringBuilder: StringBuilder,
        name: String?,
        description: String?
    ) {
        stringBuilder.append("<tr><td colspan=\"5\" class=\"root\">")
        stringBuilder.append(name)
        stringBuilder.append("</td></tr>\n <tr><td>")
        stringBuilder.append(description)
        stringBuilder.append("</td></tr>")
    }

    private fun bitmapToBase64String(bmp: Bitmap?): String {
        if ((bmp == null) || bmp.isRecycled) return ""
        return runCatching {
            val processedBmp = getScaledBitmapIfNeeded(bmp)
            val shouldRecycleProcessed = processedBmp != bmp
            try {
                val initialCapacity =
                    (processedBmp.width * processedBmp.height / 2).coerceAtLeast(1024)
                ByteArrayOutputStream(initialCapacity).use { byteArrayOutputStream ->
                    processedBmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
                }
            } finally {
                if (shouldRecycleProcessed && !processedBmp.isRecycled) {
                    processedBmp.recycle()
                }
            }
        }.onFailure { e ->
            Log.e("Export", "Bitmap to string failed: $e")
        }.getOrDefault("")
    }

    private fun getScaledBitmapIfNeeded(bmp: Bitmap): Bitmap {
        val maxDim = maxOf(bmp.width, bmp.height)
        if (maxDim <= MAX_EXPORT_BITMAP_DIMENSION) return bmp
        val scale = MAX_EXPORT_BITMAP_DIMENSION.toFloat() / maxDim
        val targetW = (bmp.width * scale).toInt().coerceAtLeast(1)
        val targetH = (bmp.height * scale).toInt().coerceAtLeast(1)
        return bmp.scale(targetW, targetH)
    }
}
