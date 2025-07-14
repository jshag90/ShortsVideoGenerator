package com.ji;

import javax.swing.*;
import java.io.*;

import static com.ji.ShortsVideoGeneratorUIService.*;
import static com.ji.util.FileUtils.getFfmpegPath;
import static com.ji.util.LogUtils.logProcess;
import static com.ji.util.VideoUtil.getVideoDurationInSeconds;

public class ShortsVideoGeneratorService {

    ShortsVideoGeneratorUIService shortsVideoGeneratorUIService;

    public ShortsVideoGeneratorService(ShortsVideoGeneratorUIService shortsVideoGeneratorUIService) {
        this.shortsVideoGeneratorUIService = shortsVideoGeneratorUIService;
    }

    public void runFfmpegTwoStep() {
        if (headerImageFile == null || videoFile == null) {
            JOptionPane.showMessageDialog(shortsVideoGeneratorUIService, "이미지와 영상 파일을 모두 업로드하세요.");
            return;
        }

        String outputPath = outputPathField.getText().trim();
        if (outputPath.isEmpty()) {
            outputPath = "C:\\Users\\wltjs\\OneDrive\\바탕 화면\\쇼츠생성기\\shorts_output.mp4";
        }
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();

        String videoOnlyPath = parentDir.getAbsolutePath() + "\\video_only.mp4";

        try {
            double duration = getVideoDurationInSeconds(videoFile);
            String durationStr = String.format("%.2f", duration);

            // 입력값 읽기
            String scaleWidth = scaleWidthField.getText().trim();
            String scaleHeight = scaleHeightField.getText().trim();
            String posX = posXField.getText().trim();
            String posY = posYField.getText().trim();

            String filterComplex = String.format(
                    "[0:v]loop=-1:size=1:start=0,scale=1080:1920[vbg];" +
                            "[1:v]scale=%s:%s[vfg];" +
                            "[vbg][vfg]overlay=%s:%s[v]",
                    scaleWidth, scaleHeight, posX, posY
            );

            String[] step1 = {
                    getFfmpegPath(),
                    "-i", headerImageFile.getAbsolutePath(),
                    "-i", videoFile.getAbsolutePath(),
                    "-filter_complex", filterComplex,
                    "-map", "[v]",
                    "-c:v", "libx264",
                    "-preset", "veryfast",
                    "-t", durationStr,
                    "-y", videoOnlyPath
            };

            ProcessBuilder pb1 = new ProcessBuilder(step1);
            pb1.redirectErrorStream(true);
            logProcess(pb1.start());

            // Step 2: 배경음악 합성
            if (audioFile != null) {
                String[] step2 = {
                        getFfmpegPath(),
                        "-i", videoOnlyPath,
                        "-i", audioFile.getAbsolutePath(),
                        "-map", "0:v:0",
                        "-map", "1:a:0",
                        "-c:v", "copy",
                        "-c:a", "aac",
                        "-shortest",
                        "-y", outputFile.getAbsolutePath()
                };
                ProcessBuilder pb2 = new ProcessBuilder(step2);
                pb2.redirectErrorStream(true);
                logProcess(pb2.start());
            } else {
                new File(videoOnlyPath).renameTo(outputFile);
            }

            JOptionPane.showMessageDialog(shortsVideoGeneratorUIService, "쇼츠 영상 생성 완료: " + outputFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(shortsVideoGeneratorUIService, "FFmpeg 실행 중 오류:\n" + ex.getMessage());
        }
    }

}
