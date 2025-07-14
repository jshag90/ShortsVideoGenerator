package com.ji;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import static com.ji.util.FileUtils.getFfmpegPath;
import static com.ji.util.LogUtils.logProcess;
import static com.ji.util.VideoUtil.getVideoDurationInSeconds;

public class ShortsVideoGeneratorUIService extends JFrame {

    public static File headerImageFile;
    public static File videoFile;
    public static File audioFile;
    public static JLabel imageLabel;
    public static JLabel videoLabel;
    public static JLabel audioLabel;
    public static JTextField outputPathField;
    public static JTextField scaleWidthField;
    public static JTextField scaleHeightField;
    public static JTextField posXField;
    public static JTextField posYField;

    ShortsVideoGeneratorService shortsVideoGeneratorService;

    public ShortsVideoGeneratorUIService() {
        shortsVideoGeneratorService = new ShortsVideoGeneratorService(this);

        setTitle("쇼츠 영상 생성기");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // GUI 구성
        JButton uploadImageBtn = new JButton("배경 프레임 이미지 업로드(900x1600)");
        imageLabel = new JLabel("이미지 파일을 드래그 또는 버튼 업로드");
        uploadImageBtn.addActionListener(this::uploadImage);
        imageLabel.setTransferHandler(new FileDropHandler("image", ShortsVideoGeneratorUIService.this));

        JButton uploadVideoBtn = new JButton("영상 업로드");
        videoLabel = new JLabel("영상 파일을 드래그 또는 버튼 업로드");
        uploadVideoBtn.addActionListener(this::uploadVideo);
        videoLabel.setTransferHandler(new FileDropHandler("video", ShortsVideoGeneratorUIService.this));

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
        audioLabel.setTransferHandler(new FileDropHandler("audio", this));

        JLabel outputPathLabel = new JLabel("출력 경로:");
        outputPathField = new JTextField("C:\\Users\\wltjs\\OneDrive\\바탕 화면\\쇼츠생성기\\shorts_output.mp4", 30);
        JButton browseOutputBtn = new JButton("경로 선택");
        browseOutputBtn.addActionListener(this::browseOutputPath);

        JButton generateBtn = new JButton("쇼츠 영상 생성");
        generateBtn.addActionListener(e -> shortsVideoGeneratorService.runFfmpegTwoStep());

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

}
