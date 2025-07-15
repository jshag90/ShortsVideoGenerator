package com.ji;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.ji.util.FileUtils.*;

public class ImageVideoMakerUIService extends JFrame {

    public static DefaultTableModel tableModel;
    public static List<File> uploadImageFiles = new ArrayList<>();
    public static JTextField pathField;
    public static final String DEFAULT_PATH = "D:\\ShortsGenerator";

    private static final String TITLE = "사진_To_동영상 생성기";
    private static final int FRAME_WIDTH = 650;
    private static final int FRAME_HEIGHT = 450;

    public ImageVideoMakerUIService() {
        setTitle(TITLE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
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
                            uploadImageFiles.add(file);
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

        generateBtn.addActionListener(e -> new ImageVideoMakerService(this).generateVideo());
        clearBtn.addActionListener(e -> {
            uploadImageFiles.clear();
            tableModel.setRowCount(0);
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("저장 경로:"));
        inputPanel.add(pathField);
        inputPanel.add(generateBtn);
        inputPanel.add(clearBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

}
