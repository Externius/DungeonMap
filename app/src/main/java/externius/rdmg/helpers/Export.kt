package externius.rdmg.helpers

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.TrapDescription
import java.io.ByteArrayOutputStream
import java.io.IOException

object Export {
    @JvmStatic
    fun generateHTML(
        bmp: Bitmap,
        roomDescription: List<RoomDescription>?,
        trapDescription: List<TrapDescription>?,
        roamingMonsterDescription: List<RoamingMonsterDescription>?
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("""
            <html>
            <head>
            <title>DungeonMap</title>
            <style>table, th, td {vertical-align: middle;} .wrap {white-space: pre-wrap;} .root {background-color: lightgray; text-align: center; font-weight: bold;}</style>
            </head>
            <body>
            <div>
            <img src="data:image/png;base64,
        """.trimIndent())
        stringBuilder.append(bitmapToBase64String(bmp))
        stringBuilder.append("\">\n<table id=\"table_description\" class=\"wrap\">")
        if (roomDescription != null) {
            addRoomDescription(roomDescription, stringBuilder)
        }
        addTrapDescription(trapDescription, stringBuilder)
        addRoamingMonsters(roamingMonsterDescription, stringBuilder)
        stringBuilder.append("""
            </table>
            </div>
            </body>
            </html>
        """.trimIndent())
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

    private fun bitmapToBase64String(bmp: Bitmap): String {
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val bytes = byteArrayOutputStream.toByteArray()
                return Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: IOException) {
            Log.e("Export", "Bitmap to string failed: $e")
        }
        return ""
    }
}
