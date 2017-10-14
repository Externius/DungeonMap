package externius.rdmg.helpers;


import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.List;

import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.TrapDescription;

public final class Export {


    private Export() {

    }

    public static String generateHTML(Bitmap bmp, List<RoomDescription> roomDescription, List<TrapDescription> trapDescription) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>\n" +
                "<head>\n" +
                "<title>DungeonMap</title>\n" +
                "<style>table, th, td {border-collapse: collapse;} th, td {padding: 8px; text-align: left; border-bottom: 1px solid #ddd; width: 100%;}td.room{ width: unset;}</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<img src=\"data:image/png;base64,");
        stringBuilder.append(bitmapToBase64String(bmp));
        stringBuilder.append("\">\n" +
                "<table id=\"table_description\">");
        for (int i = 0; i < roomDescription.size(); i++) {
            stringBuilder.append("<tr>\n" +
                    "<td rowspan=\"3\" class=\"room\">");
            stringBuilder.append(roomDescription.get(i).getName());
            stringBuilder.append("</td>\n" +
                    "<td>");
            stringBuilder.append(roomDescription.get(i).getMonster());
            stringBuilder.append("</td>\n" +
                    "</tr>\n" +
                    "<tr><td>");
            stringBuilder.append(roomDescription.get(i).getTreasure());
            stringBuilder.append("</td></tr>");
            stringBuilder.append("<tr><td>");
            stringBuilder.append(roomDescription.get(i).getDoors());
            stringBuilder.append("</td></tr>");
        }
        for (int i = 0; i < trapDescription.size(); i++) {
            stringBuilder.append("<tr><td rowspan=\"2\" class=\"room\">");
            stringBuilder.append(trapDescription.get(i).getName());
            stringBuilder.append("</td></tr>\n" +
                    "<tr><td>");
            stringBuilder.append(trapDescription.get(i).getDescription());
            stringBuilder.append("</td></tr>");
        }
        stringBuilder.append("</table>\n" +
                "</body>\n" +
                "</html>");
        return stringBuilder.toString();
    }

    private static String bitmapToBase64String(Bitmap bmp) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

}

