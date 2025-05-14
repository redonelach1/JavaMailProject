package com.java.swing;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.emailclient.EmailSender;
import com.emailclient.EmailSessionManager;
import com.emailclient.EmailReceiver;
import com.emailclient.EmailManager;
import com.models.EmailMessage;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import java.awt.Color;
import java.awt.Font;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class EmailClientGUI extends JFrame {
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private DefaultListModel<EmailMessage> emailListModel = new DefaultListModel<>();
    private JList<EmailMessage> emailList = new JList<>(emailListModel);
    private JTextArea emailContent = new JTextArea();
    private JTree folderTree;
    private String currentFolder = "INBOX";

    public EmailClientGUI() {
        setTitle("Java Email Client");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (EmailSessionManager.getInstance() != null) {
                        EmailSessionManager.getInstance().close();
                    }
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static final Color BACKGROUND_COLOR = new Color(230, 240, 250);
    private static final Color ACTION_PANEL_COLOR = new Color(200, 220, 240);
    private static final Color BUTTON_COLOR = new Color(180, 220, 240);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font EMAIL_LIST_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font EMAIL_CONTENT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private void initUI() {
        // Create main split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.2);

        // Create folder tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Folders");
        DefaultMutableTreeNode inbox = new DefaultMutableTreeNode("INBOX");
        DefaultMutableTreeNode sent = new DefaultMutableTreeNode("SENT");
        DefaultMutableTreeNode draft = new DefaultMutableTreeNode("DRAFT");
        DefaultMutableTreeNode trash = new DefaultMutableTreeNode("TRASH");
        root.add(inbox);
        root.add(sent);
        root.add(draft);
        root.add(trash);
        
        folderTree = new JTree(root);
        folderTree.setFont(EMAIL_LIST_FONT);
        folderTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) folderTree.getLastSelectedPathComponent();
            if (node != null) {
                currentFolder = node.getUserObject().toString();
                refreshEmails();
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(folderTree);
        treeScrollPane.setBackground(BACKGROUND_COLOR);
        mainSplitPane.setLeftComponent(treeScrollPane);

        // Create email list and content split pane
        JSplitPane emailSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        emailSplitPane.setResizeWeight(0.4);

        // Configure email list
        emailList.setCellRenderer(new EmailListCellRenderer());
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.addListSelectionListener(this::emailListSelectionChanged);
        emailList.setFont(EMAIL_LIST_FONT);
        JScrollPane listScrollPane = new JScrollPane(emailList);
        listScrollPane.setBackground(BACKGROUND_COLOR);

        // Configure email content
        emailContent.setEditable(false);
        emailContent.setFont(EMAIL_CONTENT_FONT);
        JScrollPane contentScrollPane = new JScrollPane(emailContent);
        contentScrollPane.setBackground(BACKGROUND_COLOR);

        emailSplitPane.setLeftComponent(listScrollPane);
        emailSplitPane.setRightComponent(contentScrollPane);
        mainSplitPane.setRightComponent(emailSplitPane);

        getContentPane().setBackground(BACKGROUND_COLOR);
        getContentPane().add(mainSplitPane, BorderLayout.CENTER);

        // Create action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(ACTION_PANEL_COLOR);

        JButton replyButton = new JButton("Reply");
        JButton forwardButton = new JButton("Forward");
        JButton deleteButton = new JButton("Delete");
        JButton markReadButton = new JButton("Mark as Read");
        JButton moveToButton = new JButton("Move To");

        for (JButton button : new JButton[]{replyButton, forwardButton, deleteButton, markReadButton, moveToButton}) {
            button.setFont(BUTTON_FONT);
            button.setBackground(BUTTON_COLOR);
            actionPanel.add(button);
        }

        replyButton.addActionListener(e -> prepareEmailAction("Reply"));
        forwardButton.addActionListener(e -> prepareEmailAction("Forward"));
        deleteButton.addActionListener(e -> deleteSelectedEmail());
        markReadButton.addActionListener(e -> toggleReadStatus());
        moveToButton.addActionListener(e -> showMoveToDialog());

        add(actionPanel, BorderLayout.NORTH);

        // Create bottom panel
        JPanel bottomPanel = new JPanel(new GridLayout(1, 4));
        bottomPanel.setBackground(ACTION_PANEL_COLOR);

        JButton composeButton = new JButton("Compose");
        JButton refreshButton = new JButton("Refresh");
        JButton newFolderButton = new JButton("New Folder");
        JButton mailingListsButton = new JButton("Mailing Lists");

        for (JButton button : new JButton[]{composeButton, refreshButton, newFolderButton, mailingListsButton}) {
            button.setFont(BUTTON_FONT);
            button.setBackground(BUTTON_COLOR);
            bottomPanel.add(button);
        }

        composeButton.addActionListener(e -> showComposeDialog("", "", ""));
        refreshButton.addActionListener(e -> refreshEmails());
        newFolderButton.addActionListener(e -> showNewFolderDialog());
        mailingListsButton.addActionListener(e -> showMailingListManagerDialog());

        add(bottomPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::showLoginDialog);
    }

    private void refreshEmails() {
        try {
            // Fetch emails from the server and update the in-memory list
            EmailReceiver.receiveEmail();
            // Now get emails for the current folder from the in-memory list
            List<EmailMessage> emails = EmailReceiver.getEmailsInFolder(currentFolder);
            emailListModel.clear();
            for (EmailMessage email : emails) {
                emailListModel.addElement(email);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch emails: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedEmail() {
        EmailMessage selectedEmail = emailList.getSelectedValue();
        if (selectedEmail != null) {
            EmailReceiver.deleteEmail(selectedEmail.getMessageId());
            refreshEmails();
        }
    }

    private void toggleReadStatus() {
        EmailMessage selectedEmail = emailList.getSelectedValue();
        if (selectedEmail != null) {
            if (selectedEmail.isRead()) {
                EmailReceiver.markAsUnread(selectedEmail.getMessageId());
            } else {
                EmailReceiver.markAsRead(selectedEmail.getMessageId());
            }
            refreshEmails();
        }
    }

    private void showMoveToDialog() {
        EmailMessage selectedEmail = emailList.getSelectedValue();
        if (selectedEmail != null) {
            String[] folders = EmailManager.getInstance().getFolders().toArray(new String[0]);
            String selectedFolder = (String) JOptionPane.showInputDialog(
                this,
                "Select destination folder:",
                "Move To",
                JOptionPane.QUESTION_MESSAGE,
                null,
                folders,
                currentFolder
            );
            
            if (selectedFolder != null) {
                EmailReceiver.moveToFolder(selectedEmail.getMessageId(), selectedFolder);
                refreshEmails();
            }
        }
    }

    private void showNewFolderDialog() {
        String folderName = JOptionPane.showInputDialog(this, "Enter new folder name:");
        if (folderName != null && !folderName.trim().isEmpty()) {
            EmailManager.getInstance().createFolder(folderName);
            refreshFolderTree();
        }
    }

    private void refreshFolderTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) folderTree.getModel().getRoot();
        root.removeAllChildren();
        
        // Add default folders
        root.add(new DefaultMutableTreeNode("INBOX"));
        root.add(new DefaultMutableTreeNode("SENT"));
        root.add(new DefaultMutableTreeNode("DRAFT"));
        root.add(new DefaultMutableTreeNode("TRASH"));
        
        // Add custom folders
        for (String folder : EmailManager.getInstance().getFolders()) {
            if (!folder.equals("INBOX") && !folder.equals("SENT") && 
                !folder.equals("DRAFT") && !folder.equals("TRASH")) {
                root.add(new DefaultMutableTreeNode(folder));
            }
        }
        
        ((DefaultTreeModel) folderTree.getModel()).reload();
    }

    // Custom cell renderer for email list
    private class EmailListCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof EmailMessage) {
                EmailMessage email = (EmailMessage) value;
                String displayText = email.getSubject() + " - From: " + email.getSender();
                
                if (!email.isRead()) {
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                
                if (email.isDeleted()) {
                    setForeground(Color.GRAY);
                } else {
                    setForeground(Color.BLACK);
                }
                
                setText(displayText);
            }
            
            return this;
        }
    }

    private void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(BUTTON_FONT);
        panel.add(emailLabel);
        usernameField.setFont(EMAIL_LIST_FONT);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("App Password:");
        passwordLabel.setFont(BUTTON_FONT);
        panel.add(passwordLabel);
        passwordField.setFont(EMAIL_LIST_FONT);
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            try {
                EmailSessionManager.getInstance(username, password);
                refreshEmails();
            } catch (MessagingException e) {
                JOptionPane.showMessageDialog(this, "Failed to initialize email session: " + e.getMessage(),
                        "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("Login cancelled.");
        }
    }

    private void emailListSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && emailList.getSelectedIndex() != -1) {
            try {
                EmailMessage selectedEmail = emailList.getSelectedValue();
                emailContent.setText("");
                emailContent.append("Subject: " + selectedEmail.getSubject() + "\n\n");
                emailContent.append("From: " + selectedEmail.getSender() + "\n\n");
                emailContent.append(selectedEmail.getContent());
            } catch (Exception ex) {
                emailContent.setText("Error reading email content: " + ex.getMessage());
            }
        }
    }

    private void prepareEmailAction(String actionType) {
        if (emailList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "No email selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            EmailMessage selectedEmail = emailList.getSelectedValue();
            String to = actionType.equals("Reply") ? selectedEmail.getSender() : "";
            String subjectPrefix = actionType.equals("Reply") ? "Re: " : "Fwd: ";
            String subject = subjectPrefix + selectedEmail.getSubject();
            String body = selectedEmail.getContent();

            showComposeDialog(to, subject, body);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error preparing email action: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showComposeDialog(String to, String subject, String body) {
        JDialog composeDialog = new JDialog(this, "Compose Email", true);
        composeDialog.setLayout(new BorderLayout(5, 5));
        composeDialog.getContentPane().setBackground(BACKGROUND_COLOR);

        Box fieldsPanel = Box.createVerticalBox();
        fieldsPanel.setBackground(BACKGROUND_COLOR);

        JTextField toField = new JTextField(to);
        toField.setFont(EMAIL_CONTENT_FONT);
        JTextField subjectField = new JTextField(subject);
        subjectField.setFont(EMAIL_CONTENT_FONT);
        JTextArea bodyArea = new JTextArea(10, 20);
        bodyArea.setText(body);
        bodyArea.setFont(EMAIL_CONTENT_FONT);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);

        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(BUTTON_FONT);
        fieldsPanel.add(toLabel);
        fieldsPanel.add(toField);
        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setFont(BUTTON_FONT);
        fieldsPanel.add(subjectLabel);
        fieldsPanel.add(subjectField);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(ACTION_PANEL_COLOR);

        JButton attachButton = new JButton("Attach Files");
        attachButton.setFont(BUTTON_FONT);
        attachButton.setBackground(BUTTON_COLOR);

        JButton sendButton = new JButton("Send");
        sendButton.setFont(BUTTON_FONT);
        sendButton.setBackground(BUTTON_COLOR);

        JLabel attachedFilesLabel = new JLabel("No files attached");
        attachedFilesLabel.setFont(BUTTON_FONT);

        List<File> attachedFiles = new ArrayList<>();
        attachButton.addActionListener(e -> {
            File[] files = AttachmentChooser.chooseAttachments();
            if (files != null) {
                attachedFiles.addAll(Arrays.asList(files));
                attachedFilesLabel.setText(attachedFiles.size() + " files attached");
            }
        });

        sendButton.addActionListener(e -> {
            String recipient = toField.getText().trim();
            String emailSubject = subjectField.getText().trim();
            String emailBody = bodyArea.getText().trim();

            if (recipient.isEmpty()) {
                JOptionPane.showMessageDialog(composeDialog, 
                    "Please enter a recipient email address.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (emailSubject.isEmpty()) {
                JOptionPane.showMessageDialog(composeDialog, 
                    "Please enter a subject.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sendButton.setEnabled(false);
            attachButton.setEnabled(false);
            sendButton.setText("Sending...");

            // Send email in background
            new Thread(() -> {
                try {
                    EmailSender.sendEmailWithAttachment(recipient, emailSubject, emailBody,
                            attachedFiles.toArray(new File[0]));
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(composeDialog, 
                            "Email sent successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        composeDialog.dispose();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(composeDialog, 
                            "Failed to send email: " + ex.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        sendButton.setEnabled(true);
                        attachButton.setEnabled(true);
                        sendButton.setText("Send");
                    });
                }
            }).start();
        });

        bottomPanel.add(attachButton);
        bottomPanel.add(sendButton);
        bottomPanel.add(attachedFilesLabel);

        composeDialog.add(fieldsPanel, BorderLayout.NORTH);
        composeDialog.add(new JScrollPane(bodyArea), BorderLayout.CENTER);
        composeDialog.add(bottomPanel, BorderLayout.SOUTH);

        composeDialog.pack();
        composeDialog.setLocationRelativeTo(this);
        composeDialog.setVisible(true);
    }

    private void showMailingListManagerDialog() {
        MailingListManagerDialog dialog = new MailingListManagerDialog(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmailClientGUI());
    }
}