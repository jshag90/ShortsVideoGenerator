package com.ji;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.ji.ImageVideoMakerUIService.*;
import static com.ji.util.FileUtils.*;

public class ImageVideoMakerService  {

    ImageVideoMakerUIService imageVideoMakerUIService;

    public ImageVideoMakerService(ImageVideoMakerUIService imageVideoMakerUIService) {
        this.imageVideoMakerUIService = imageVideoMakerUIService;
    }

    public void generateVideo() {
        if (uploadImageFiles.isEmpty()) {
            JOptionPane.showMessageDialog(imageVideoMakerUIService, "사진을 먼저 업로드해주세요.");
            return;
        }

        String userPath = pathField.getText().trim();
        File outputDir = userPath.isEmpty() ? new File(DEFAULT_PATH) : new File(userPath);
        if (!outputDir.exists()) outputDir.mkdirs();

        try {
            List<String> inputList = new ArrayList<>();
            for (int i = 0; i < uploadImageFiles.size(); i++) {
                File src = uploadImageFiles.get(i);
                String ext = src.getName().substring(src.getName().lastIndexOf('.') + 1);
                File dest = new File(outputDir, String.format("img%03d.%s", i, ext));
                if (dest.exists()) dest.delete();
                copyFile(src, dest);

                double duration = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
                inputList.add(String.format("file '%s'\nduration %.3f", dest.getPath().replace("\\", "/"), duration));
            }

            File last = new File(outputDir, String.format("img%03d.%s", uploadImageFiles.size() - 1,
                    uploadImageFiles.get(uploadImageFiles.size() - 1).getName().split("\\.")[1]));
            inputList.add("file '" + last.getPath().replace("\\", "/") + "'");

            File inputTxt = new File(outputDir, "input.txt");
            if (inputTxt.exists()) inputTxt.delete();
            try (PrintWriter writer = new PrintWriter(inputTxt)) {
                for (String line : inputList) writer.println(line);
            }

            File output = new File(outputDir, "output.mp4");
            if (output.exists()) output.delete();

            ProcessBuilder processBuilder = new ProcessBuilder(
                    getFfmpegPath(),
                    "-f", "concat",
                    "-safe", "0",
                    "-i", inputTxt.getAbsolutePath(),
                    "-vf", "scale=trunc(iw/2)*2:trunc(ih/2)*2",
                    "-vsync", "vfr",
                    "-pix_fmt", "yuv420p",
                    output.getAbsolutePath()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                JOptionPane.showMessageDialog(imageVideoMakerUIService, "영상 생성 완료:\n" + output.getAbsolutePath());
                uploadImageFiles.clear();
                tableModel.setRowCount(0);
            } else {
                JOptionPane.showMessageDialog(imageVideoMakerUIService, "FFmpeg 실행 실패!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(imageVideoMakerUIService, "오류 발생: " + e.getMessage());
        }
    }

}
