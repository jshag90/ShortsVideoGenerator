package com.ji.util;

import java.io.*;

public class FileUtils {

    public static String getFfmpegPath() {
        return "C:\\ffmpeg\\bin\\ffmpeg.exe"; // 본인의 ffmpeg 경로
    }

    public static String getFfprobePath() {
        return "C:\\ffmpeg\\bin\\ffprobe.exe"; // 본인의 ffmpeg 경로
    }

    public static void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    public static boolean isImageFile(File file) {
        String[] extensions = {"png", "jpg", "jpeg", "bmp", "gif", "webp"};
        String name = file.getName().toLowerCase();
        for (String ext : extensions) {
            if (name.endsWith("." + ext)) return true;
        }
        return false;
    }
}
