package com.ji;

import javax.swing.*;

public class Main {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageVideoMakerUIService::new);
        SwingUtilities.invokeLater(ShortsVideoGeneratorUIService::new);
    }
}
