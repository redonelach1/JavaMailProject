package com.java.swing;

import javax.swing.*;
import java.io.File;

public class AttachmentChooser {
  public static File[] chooseAttachments() {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setMultiSelectionEnabled(true);
      int option = fileChooser.showOpenDialog(null);
      if (option == JFileChooser.APPROVE_OPTION) {
          return fileChooser.getSelectedFiles();
      }
      return new File[] {};
  }
}
