package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.List;

public class ShortsVideoGenerator extends JFrame {

    private File headerImageFile;
    private File videoFile;
    private File audioFile;
    private JLabel imageLabel;
    private JLabel videoLabel;
    private JLabel audioLabel;
    private JTextField outputPathField;
    // 영상 크기 및 위치 입력 필드
    private JTextField scaleWidthField;
    private JTextField scaleHeightField;
    private JTextField posXField;
    private JTextField posYField;


    public ShortsVideoGenerator() {
        setTitle("쇼츠 영상 생성기");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // GUI 구성
        JButton uploadImageBtn = new JButton("배경 프레임 이미지 업로드(900x1600)");
        imageLabel = new JLabel("이미지 파일을 드래그 또는 버튼 업로드");
        uploadImageBtn.addActionListener(this::uploadImage);
        imageLabel.setTransferHandler(new FileDropHandler("image"));

        JButton uploadVideoBtn = new JButton("영상 업로드");
        videoLabel = new JLabel("영상 파일을 드래그 또는 버튼 업로드");
        uploadVideoBtn.addActionListener(this::uploadVideo);
        videoLabel.setTransferHandler(new FileDropHandler("video"));

        // 영상 크기 입력 필드
        JLabel scaleLabel = new JLabel("영상 크기 (width x height):");
        scaleWidthField = new JTextField("880", 4);
        scaleHeightField = new JTextField("880", 4);

        // 영상 위치 입력 필드
        JLabel positionLabel = new JLabel("영상 시작 위치 (x, y):");
        posXField = new JTextField("100", 4);
        posYField = new JTextField("610", 4);

        JButton uploadAudioBtn = new JButton("배경음악 업로드");
        audioLabel = new JLabel("오디오 파일을 드래그 또는 버튼 업로드");
        uploadAudioBtn.addActionListener(this::uploadAudio);
        audioLabel.setTransferHandler(new FileDropHandler("audio"));

        JLabel outputPathLabel = new JLabel("출력 경로:");
        outputPathField = new JTextField("C:\\Users\\wltjs\\OneDrive\\바탕 화면\\쇼츠생성기\\shorts_output.mp4", 30);
        JButton browseOutputBtn = new JButton("경로 선택");
        browseOutputBtn.addActionListener(this::browseOutputPath);

        JButton generateBtn = new JButton("쇼츠 영상 생성");
        generateBtn.addActionListener(e -> runFffmpegTwoStep());

        JPanel panel = new JPanel(new GridLayout(7, 1));
        panel.add(buildRow(uploadImageBtn, imageLabel));
        panel.add(buildRow(uploadVideoBtn, videoLabel));
        panel.add(buildRow(scaleLabel, scaleWidthField, scaleHeightField));
        panel.add(buildRow(positionLabel, posXField, posYField));
        panel.add(buildRow(uploadAudioBtn, audioLabel));
        panel.add(buildRow(outputPathLabel, outputPathField, browseOutputBtn));

        add(panel, BorderLayout.CENTER);
        add(generateBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel buildRow(Component... components) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Component c : components) row.add(c);
        return row;
    }

    private void uploadImage(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            headerImageFile = chooser.getSelectedFile();
            imageLabel.setText(headerImageFile.getName());
        }
    }

    private void uploadVideo(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            videoFile = chooser.getSelectedFile();
            videoLabel.setText(videoFile.getName());
        }
    }

    private void uploadAudio(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            audioFile = chooser.getSelectedFile();
            audioLabel.setText(audioFile.getName());
        }
    }

    private void browseOutputPath(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            outputPathField.setText(dir.getAbsolutePath() + "\\shorts_output.mp4");
        }
    }

    private void runFffmpegTwoStep() {
        if (headerImageFile == null || videoFile == null) {
            JOptionPane.showMessageDialog(this, "이미지와 영상 파일을 모두 업로드하세요.");
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

           /* String[] step1 = {
                    getFffmpegPath(),
                    "-i", headerImageFile.getAbsolutePath(),
                    "-i", videoFile.getAbsolutePath(),
                    "-filter_complex",
                    "[0:v]loop=-1:size=1:start=0,scale=1080:1920[vbg];" +
                            "[1:v]scale=1080:960[vfg];" +
                            "[vbg][vfg]overlay=0:480[v]",
                    "-map", "[v]",
                    "-c:v", "libx264",
                    "-preset", "veryfast",
                    "-t", durationStr,
                    "-y", videoOnlyPath
            };*/

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
                    getFffmpegPath(),
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
                        getFffmpegPath(),
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
                // 오디오가 없으면 영상만 복사
                new File(videoOnlyPath).renameTo(outputFile);
            }

            JOptionPane.showMessageDialog(this, "쇼츠 영상 생성 완료: " + outputFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "FFmpeg 실행 중 오류:\n" + ex.getMessage());
        }
    }

    private double getVideoDurationInSeconds(File video) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "C:\\ffmpeg\\bin\\ffprobe.exe", // FFprobe 경로
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


    private void logProcess(Process process) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("FFmpeg: " + line);
        }
        process.waitFor();
    }

    private static String getFffmpegPath() {
        return "C:\\ffmpeg\\bin\\ffmpeg.exe"; // 본인의 ffmpeg 경로
    }

    private class FileDropHandler extends TransferHandler {
        private final String fileType;

        public FileDropHandler(String fileType) {
            this.fileType = fileType;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            try {
                Transferable t = support.getTransferable();
                List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                if (files.isEmpty()) return false;

                File file = files.get(0);
                String name = file.getName().toLowerCase();

                if (fileType.equals("image") && name.matches(".*\\.(jpg|jpeg|png|bmp|gif)$")) {
                    headerImageFile = file;
                    imageLabel.setText(file.getName());
                    return true;
                } else if (fileType.equals("video") && name.matches(".*\\.(mp4|mov|avi|mkv|flv|wmv)$")) {
                    videoFile = file;
                    videoLabel.setText(file.getName());
                    return true;
                } else if (fileType.equals("audio") && name.matches(".*\\.(mp3|wav|aac)$")) {
                    audioFile = file;
                    audioLabel.setText(file.getName());
                    return true;
                } else {
                    JOptionPane.showMessageDialog(ShortsVideoGenerator.this, fileType + " 파일 형식이 잘못되었습니다.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(ShortsVideoGenerator.this, "파일 드롭 중 오류: " + e.getMessage());
            }
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShortsVideoGenerator::new);
    }
}
