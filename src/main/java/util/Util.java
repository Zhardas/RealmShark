package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generic utility class to insert methods used randomly.
 */
public class Util {

    public static boolean showLogs = true;
    private static PrintWriter printWriter;

    /**
     * Fast method to get an integer out of the first 4 bytes of an array.
     *
     * @param bytes Byte array needing an integer extracted from.
     * @return Returns the integer from the first bytes of an array.
     */
    public static int decodeInt(byte[] bytes) {
        return (Byte.toUnsignedInt(bytes[0]) << 24) | (Byte.toUnsignedInt(bytes[1]) << 16) | (Byte.toUnsignedInt(bytes[2]) << 8) | Byte.toUnsignedInt(bytes[3]);
    }

    /**
     * Enable / disable log printouts.
     *
     * @param logs boolean to enable logs.
     */
    public static void setShowLogs(boolean logs) {
        showLogs = logs;
    }

    /**
     * Print error logs to console or to file.
     *
     * @param s String of the log.
     */
    public static void print(String s) {
        if (showLogs) {
            System.out.println(s);
        } else {
            System.out.println(s);
            if (printWriter == null) {
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");
                    LocalDateTime now = LocalDateTime.now();
                    File f = new File("error/" + dtf.format(now) + ".data");
                    if (!f.exists()) {
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(f);
                    printWriter = new PrintWriter(fileWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            printWriter.print("\n" + s);
            printWriter.flush();
        }
    }

    public static String byteArrayPrint(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (byte b : bytes) {
            if (!first) sb.append("|");
            first = false;
            sb.append(String.format("%01x", b));
        }
        sb.append("]");
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l / 2];
        for (int i = 0; i < l; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
