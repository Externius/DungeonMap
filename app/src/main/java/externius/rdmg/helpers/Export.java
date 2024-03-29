package externius.rdmg.helpers;


import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.TrapDescription;
import externius.rdmg.models.RoamingMonsterDescription;

public final class Export {


    private Export() {

    }

    public static String generateHTML(Bitmap bmp, List<RoomDescription> roomDescription, List<TrapDescription> trapDescription, List<RoamingMonsterDescription> roamingMonsterDescription) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("""
                <html>
                <head>
                <title>DungeonMap</title>
                <style>table, th, td {vertical-align: middle;} .wrap {white-space: pre-wrap;} .root {background-color: lightgray; text-align: center; font-weight: bold;}</style>
                </head>
                <body>
                <div>
                <img src="data:image/png;base64,""");
        stringBuilder.append(bitmapToBase64String(bmp));
        stringBuilder.append("\">\n" +
                "<table id=\"table_description\" class=\"wrap\">");
        addRoomDescription(roomDescription, stringBuilder);
        addTrapDescription(trapDescription, stringBuilder);
        addRoamingMonsters(roamingMonsterDescription, stringBuilder);
        stringBuilder.append("""
                </table>
                </div>
                </body>
                </html>""");
        return stringBuilder.toString();
    }

    private static void addRoomDescription(List<RoomDescription> roomDescription, StringBuilder stringBuilder) {
        for (RoomDescription room : roomDescription) {
            stringBuilder.append("<tr><td colspan=\"5\" class=\"root\">");
            stringBuilder.append(room.name());
            stringBuilder.append("</td></tr>\n <tr><td>");
            stringBuilder.append(room.monster());
            stringBuilder.append("</td></tr>\n <tr><td>");
            stringBuilder.append(room.treasure());
            stringBuilder.append("</td></tr>\n <tr><td>");
            stringBuilder.append(room.doors());
            stringBuilder.append("</td></tr>");
        }
    }

    private static void addTrapDescription(List<TrapDescription> trapDescription, StringBuilder stringBuilder) {
        if (trapDescription != null && !trapDescription.isEmpty()) {
            for (TrapDescription trap : trapDescription) {
                appendToStringBuilder(stringBuilder, trap.name(), trap.description());
            }
        }
    }

    private static void addRoamingMonsters(List<RoamingMonsterDescription> roamingMonsterDescription, StringBuilder stringBuilder) {
        if (roamingMonsterDescription != null && !roamingMonsterDescription.isEmpty()) {
            for (RoamingMonsterDescription monster : roamingMonsterDescription) {
                appendToStringBuilder(stringBuilder, monster.name(), monster.description());
            }
        }
    }

    private static void appendToStringBuilder(StringBuilder stringBuilder, String name, String description) {
        stringBuilder.append("<tr><td colspan=\"5\" class=\"root\">");
        stringBuilder.append(name);
        stringBuilder.append("</td></tr>\n <tr><td>");
        stringBuilder.append(description);
        stringBuilder.append("</td></tr>");
    }

    private static String bitmapToBase64String(Bitmap bmp) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            Log.e("Export", "Bitmap to string failed: " + e);
        }
        return "";
    }

}

