package com.ji.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogUtils {

    public static void logProcess(Process process) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("FFmpeg: " + line);
        }
        process.waitFor();
    }
}
