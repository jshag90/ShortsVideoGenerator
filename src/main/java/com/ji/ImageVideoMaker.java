package com.ji;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.ji.util.FileUtils.getFfmpegPath;

public class ImageVideoMaker extends JFrame {

    private DefaultTableModel tableModel;
    private List<File> imageFiles = new ArrayList<>();
    private JTextField pathField;
    private static final String DEFAULT_PATH = "D:\\ShortsGenerator";

    public ImageVideoMaker() {
        setTitle("사진_To_동영상 생성기");
        setSize(650, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 테이블
        tableModel = new DefaultTableModel(new Object[]{"파일명", "지속 시간(초)"}, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // 드래그 앤 드롭 지원
        new DropTarget(scrollPane, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) event.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : droppedFiles) {
                        if (isImageFile(file)) {
                            imageFiles.add(file);
                            tableModel.addRow(new Object[]{file.getName(), "2"});
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 하단 버튼 및 입력창
        pathField = new JTextField(DEFAULT_PATH, 30);
        JButton generateBtn = new JButton("동영상 생성");
        JButton clearBtn = new JButton("목록 초기화");

        generateBtn.addActionListener(e -> generateVideo());
        clearBtn.addActionListener(e -> {
            imageFiles.clear();
            tableModel.setRowCount(0);
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("저장 경로:"));
        inputPanel.add(pathField);
        inputPanel.add(generateBtn);
        inputPanel.add(clearBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private boolean isImageFile(File file) {
        String[] extensions = {"png", "jpg", "jpeg", "bmp", "gif", "webp"};
        String name = file.getName().toLowerCase();
        for (String ext : extensions) {
            if (name.endsWith("." + ext)) return true;
        }
        return false;
    }

    private void generateVideo() {
        if (imageFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "사진을 먼저 업로드해주세요.");
            return;
        }

        String userPath = pathField.getText().trim();
        File outputDir = userPath.isEmpty() ? new File(DEFAULT_PATH) : new File(userPath);
        if (!outputDir.exists()) outputDir.mkdirs();

        try {
            List<String> inputList = new ArrayList<>();
            for (int i = 0; i < imageFiles.size(); i++) {
                File src = imageFiles.get(i);
                String ext = src.getName().substring(src.getName().lastIndexOf('.') + 1);
                File dest = new File(outputDir, String.format("img%03d.%s", i, ext));
                if (dest.exists()) dest.delete();
                copyFile(src, dest);

                double duration = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
                inputList.add(String.format("file '%s'\nduration %.3f", dest.getPath().replace("\\", "/"), duration));
            }

            File last = new File(outputDir, String.format("img%03d.%s", imageFiles.size() - 1,
                    imageFiles.get(imageFiles.size() - 1).getName().split("\\.")[1]));
            inputList.add("file '" + last.getPath().replace("\\", "/") + "'");

            File inputTxt = new File(outputDir, "input.txt");
            if (inputTxt.exists()) inputTxt.delete();
            try (PrintWriter writer = new PrintWriter(inputTxt)) {
                for (String line : inputList) writer.println(line);
            }

            File output = new File(outputDir, "output.mp4");
            if (output.exists()) output.delete();

            ProcessBuilder pb = new ProcessBuilder(
                    getFfmpegPath(),
                    "-f", "concat",
                    "-safe", "0",
                    "-i", inputTxt.getAbsolutePath(),
                    "-vf", "scale=trunc(iw/2)*2:trunc(ih/2)*2",
                    "-vsync", "vfr",
                    "-pix_fmt", "yuv420p",
                    output.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                JOptionPane.showMessageDialog(this, "영상 생성 완료:\n" + output.getAbsolutePath());
                imageFiles.clear();
                tableModel.setRowCount(0);
            } else {
                JOptionPane.showMessageDialog(this, "FFmpeg 실행 실패!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "오류 발생: " + e.getMessage());
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

}
