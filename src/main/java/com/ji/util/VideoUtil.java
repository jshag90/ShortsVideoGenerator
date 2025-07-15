package com.ji.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import static com.ji.util.FileUtils.getFfprobePath;

public class VideoUtil {

    public static double getVideoDurationInSeconds(File video) {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    getFfprobePath(),
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    video.getAbsolutePath()
            );

            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Optional<String> line = Optional.ofNullable(reader.readLine());
            process.waitFor();
            return Double.parseDouble(line.orElse("30"));
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        } finally {
            process.destroy();
        }
        return 30.0;
    }

}
