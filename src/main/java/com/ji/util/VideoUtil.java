package com.ji.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.ji.util.FileUtils.getFfprobePath;

public class VideoUtil {

    public static double getVideoDurationInSeconds(File video) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                getFfprobePath(), // FFprobe 경로
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                video.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        process.waitFor();

        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            return 30.0; // 기본 fallback
        }
    }
}
