package com.ji;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import static com.ji.ShortsVideoGeneratorUIService.*;

public class FileDropHandler extends TransferHandler {

    private final String fileType;
    private final ShortsVideoGeneratorUIService shortsVideoGeneratorUIService;

    public FileDropHandler(String fileType, ShortsVideoGeneratorUIService shortsVideoGeneratorUIService) {
        this.fileType = fileType;
        this.shortsVideoGeneratorUIService = shortsVideoGeneratorUIService;
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
                JOptionPane.showMessageDialog(shortsVideoGeneratorUIService, fileType + " 파일 형식이 잘못되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(shortsVideoGeneratorUIService, "파일 드롭 중 오류: " + e.getMessage());
        }
        return false;
    }
}
