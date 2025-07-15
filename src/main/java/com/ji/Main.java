package com.ji;

import javax.swing.*;

public class Main {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageVideoMaker().setVisible(true));
        SwingUtilities.invokeLater(ShortsVideoGeneratorUIService::new);
    }
}
