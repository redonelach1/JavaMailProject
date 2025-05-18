package com.java.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.emailclient.EmailManager;
import com.emailclient.EmailReceiver;
import com.emailclient.EmailSender;
import com.emailclient.EmailSessionManager;
import com.emailclient.MailingListManager;
import com.emailclient.SMSSender;
import com.models.EmailMessage;
import com.models.MailingList;
import com.emailclient.EmailArchiveService;

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
                    // Add cleanup for temporary archived emails
                    EmailArchiveService.cleanupTemporaryArchive();
              } catch (MessagingException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
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
                // Call refreshEmails for all folder selections, including SENT and ARCHIVED
                refreshEmails(); 
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(folderTree);
        treeScrollPane.setBackground(BACKGROUND_COLOR);
        mainSplitPane.setLeftComponent(treeScrollPane);

        // Create email list and content split pane
        JSplitPane emailSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        emailSplitPane.setResizeWeight(0.4);

        // Create search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(ACTION_PANEL_COLOR);
     // Allowed search types
        String[] searchTypes = { "Subject", "Sender", "Body", "Date" };
        JComboBox<String> searchTypeBox = new JComboBox<>(searchTypes);
        searchTypeBox.setFont(EMAIL_LIST_FONT);
        searchPanel.add(searchTypeBox, BorderLayout.WEST);
        JTextField searchField = new JTextField();
        searchField.setFont(EMAIL_LIST_FONT);
        //searchField.setToolTipText("Search by subject, sender, body or date (YYYY-MM-DD)");
        JButton searchButton = new JButton("Search");
        searchButton.setFont(BUTTON_FONT);
        searchButton.setBackground(BUTTON_COLOR);
        
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            emailListModel.clear();

            // 1) on récupère TOUTES les emails du dossier courant
            List<EmailMessage> emailsInFolder;
            try {
                // force la récupération des nouveaux messages
                EmailReceiver.receiveEmail();
                // puis met à jour l'arborescence des dossiers
                refreshFolderTree();

                // maintenant on peut relire le contenu du dossier sélectionné
                emailsInFolder = EmailReceiver.getEmailsInFolder(currentFolder);

            } catch (MessagingException ex) {
                // log + affichage d'erreur à l'utilisateur
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Impossible de récupérer les e-mails : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);

                // on vide la liste pour éviter du « null »
                emailsInFolder = new ArrayList<>();
            }

            emailsInFolder = EmailReceiver.getEmailsInFolder(currentFolder);

            if (query.isEmpty()) {
                // Si pas de recherche, on recharge simplement la liste brute
                for (EmailMessage email : emailsInFolder) {
                    emailListModel.addElement(email);
                }
            } else {
                String type = (String) searchTypeBox.getSelectedItem();
                List<EmailMessage> results = new ArrayList<>();

                switch (type) {
                    case "Subject":
                        results = emailsInFolder.stream()
                            .filter(m -> m.getSubject() != null
                                      && m.getSubject().toLowerCase().contains(query))
                            .collect(Collectors.toList());
                        break;

                    case "Sender":
                        results = emailsInFolder.stream()
                            .filter(m -> m.getSender() != null
                                      && m.getSender().toLowerCase().contains(query))
                            .collect(Collectors.toList());
                        break;

                    case "Body":
                        results = emailsInFolder.stream()
                            .filter(m -> m.getContent() != null
                                      && m.getContent().toLowerCase().contains(query))
                            .collect(Collectors.toList());
                        break;

                    case "Date":
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        results = emailsInFolder.stream()
                            .filter(m -> {
                                java.util.Date d = m.getReceivedDate();
                                return d != null && sdf.format(d).contains(query);
                            })
                            .collect(Collectors.toList());
                        break;
                }

                // 3) on affiche uniquement les emails filtrés
                for (EmailMessage email : results) {
                    emailListModel.addElement(email);
                }
            }
        });



        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchButton.doClick();
                }
            }
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Configure email list
        emailList.setCellRenderer(new EmailListCellRenderer());
      emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      emailList.addListSelectionListener(this::emailListSelectionChanged);
      emailList.setFont(EMAIL_LIST_FONT);
      JScrollPane listScrollPane = new JScrollPane(emailList);
      listScrollPane.setBackground(BACKGROUND_COLOR);

        // Create a panel to hold both search and email list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Configure email content
      emailContent.setEditable(false);
      emailContent.setFont(EMAIL_CONTENT_FONT);
      JScrollPane contentScrollPane = new JScrollPane(emailContent);
      contentScrollPane.setBackground(BACKGROUND_COLOR);

        emailSplitPane.setLeftComponent(leftPanel);
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
        JButton archiveButton = new JButton("Archive");

        for (JButton button : new JButton[]{replyButton, forwardButton, deleteButton, markReadButton, moveToButton, archiveButton}) {
            button.setFont(BUTTON_FONT);
            button.setBackground(BUTTON_COLOR);
            actionPanel.add(button);
        }

      replyButton.addActionListener(e -> prepareEmailAction("Reply"));
      forwardButton.addActionListener(e -> prepareEmailAction("Forward"));
        deleteButton.addActionListener(e -> deleteSelectedEmail());
        markReadButton.addActionListener(e -> toggleReadStatus());
        moveToButton.addActionListener(e -> showMoveToDialog());
        archiveButton.addActionListener(e -> archiveSelectedEmail());

      add(actionPanel, BorderLayout.NORTH);

        // Create bottom panel
        JPanel bottomPanel = new JPanel(new GridLayout(1, 5));
      bottomPanel.setBackground(ACTION_PANEL_COLOR);

      JButton composeButton = new JButton("Compose");
        JButton refreshButton = new JButton("Refresh");
        JButton newFolderButton = new JButton("New Folder");
        JButton mailingListsButton = new JButton("Mailing Lists");
        JButton sendSMSButton = new JButton("Send SMS");
        JButton switchAccountButton = new JButton("Switch Account");

        for (JButton button : new JButton[]{composeButton, refreshButton, newFolderButton, mailingListsButton, sendSMSButton, switchAccountButton}) {
            button.setFont(BUTTON_FONT);
            button.setBackground(BUTTON_COLOR);
            bottomPanel.add(button);
        }

      composeButton.addActionListener(e -> showComposeDialog("", "", ""));
        refreshButton.addActionListener(e -> refreshEmails());
        newFolderButton.addActionListener(e -> showNewFolderDialog());
        mailingListsButton.addActionListener(e -> showMailingListManagerDialog());
        sendSMSButton.addActionListener(e -> showSendSMSDialog());
        switchAccountButton.addActionListener(e -> switchAccount());
        
      add(bottomPanel, BorderLayout.SOUTH);

      SwingUtilities.invokeLater(this::showLoginDialog);
    }

    private void showSendSMSDialog() {
        JDialog smsDialog = new JDialog(this, "Send SMS Message", true);
        smsDialog.setLayout(new GridLayout(5, 2, 10, 10));
        smsDialog.setSize(400, 250);
        
        JLabel nomExpediteurLabel = new JLabel("Sender Name:");
        JTextField nomExpediteurField = new JTextField("");
        
        JLabel destLabel = new JLabel("Destination Phone:");
        JTextField destField = new JTextField("");
        
        JLabel messageLabel = new JLabel("Message:");
        JTextField messageField = new JTextField("");
        
        JButton sendButton = new JButton("Send");
        JButton cancelButton = new JButton("Cancel");
        
        JLabel statusLabel = new JLabel("");
        
        sendButton.addActionListener(e -> {
            final String inputDestNumber = destField.getText().trim();
            final String message = messageField.getText().trim();
            final String nomExpediteur = nomExpediteurField.getText().trim();
            
            if (inputDestNumber.isEmpty() || message.isEmpty() || nomExpediteur.isEmpty()) {
                JOptionPane.showMessageDialog(smsDialog, "Please fill in all fields");
                return;
            }
            
            sendButton.setEnabled(false);
            statusLabel.setText("Sending...");
            
            Thread sendSmsThread = new Thread(() -> {
                try {
                    SMSSender smsSender = new SMSSender();
                    smsSender.sendEmailAsSMS(nomExpediteur, inputDestNumber, message);

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("SMS sent successfully!");
                        sendButton.setEnabled(true);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error: " + ex.getMessage());
                        sendButton.setEnabled(true);
                        JOptionPane.showMessageDialog(smsDialog,
                            "Failed to send SMS: " + ex.getMessage(),
                            "SMS Error",
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
            });
            sendSmsThread.setDaemon(true); // Make this a daemon thread
            sendSmsThread.start();
        });
        
        cancelButton.addActionListener(e -> smsDialog.dispose());
        
        smsDialog.add(nomExpediteurLabel);
        smsDialog.add(nomExpediteurField);
        smsDialog.add(destLabel);
        smsDialog.add(destField);
        smsDialog.add(messageLabel);
        smsDialog.add(messageField);
        smsDialog.add(statusLabel);
        smsDialog.add(new JLabel(""));
        smsDialog.add(sendButton);
        smsDialog.add(cancelButton);
        
        smsDialog.setLocationRelativeTo(this);
        smsDialog.setVisible(true);
    }

    private void refreshEmails() {
    	  try {
    	    // Don't fetch from IMAP if viewing archived emails or sent emails from local archive
    	    if (!currentFolder.equals("ARCHIVED") && !currentFolder.equals("SENT")) {
    	        EmailReceiver.receiveEmail();       // fetch + addMessage(…) → folders list updated
    	        refreshFolderTree();                // populate the JTree from the up‑to‑date folders
            }

    	    List<EmailMessage> emails;
    	    if (currentFolder.equals("INBOX") || currentFolder.equals("DRAFT") || currentFolder.equals("TRASH")) {
    	      emails = EmailReceiver.getEmailsInFolder(currentFolder);
    	    } else if (currentFolder.equals("ARCHIVED")) {
                emails = EmailManager.getInstance().getArchivedEmails(); // Load from local archive
            } else if (currentFolder.equals("SENT")) {
                // Load sent emails from permanent local archive, filtered by current user
                String currentUserEmail = EmailSessionManager.getUsername(); // Get the current user email

                if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
                   emails = EmailArchiveService.loadSentEmails(currentUserEmail);
                } else {
                   // Handle case where no account is active or email is missing
                   emails = new ArrayList<>();
                   JOptionPane.showMessageDialog(this,
                        "No active account to load sent emails.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
    	      emails = EmailManager.getInstance()
    	               .getEmailsBySenderBucket(currentFolder);
    	    }

    	    emailListModel.clear();
    	    emails.forEach(emailListModel::addElement);
    	  } catch (Exception e) {
    	    e.printStackTrace();
    	    JOptionPane.showMessageDialog(this,
    	      "Failed to fetch emails: " + e.getMessage(),
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

        // 1) dossiers standards
        for (String f : List.of("INBOX","SENT","DRAFT","TRASH", "ARCHIVED")) {
            root.add(new DefaultMutableTreeNode(f));
        }

        // 2) dossiers virtuels par expéditeur (excluding ARCHIVED)
        EmailManager.getInstance().getFolders().stream()
            .filter(f -> !List.of("INBOX","SENT","DRAFT","TRASH", "ARCHIVED").contains(f))
            .sorted()
            .forEach(f -> root.add(new DefaultMutableTreeNode(f)));

        ((DefaultTreeModel) folderTree.getModel()).reload();
    }
    
    private class EmailListCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof EmailMessage) {
                EmailMessage email = (EmailMessage) value;
                String displayText;
                
                // Show "To: email" for SENT folder, "From: email" for others
                if (currentFolder.equals("SENT")) {
                    String recipient = email.getRecipients().isEmpty() ? "No recipient" : email.getRecipients().get(0);
                    displayText = email.getSubject() + " - To: " + recipient;
                } else {
                    displayText = email.getSubject() + " - From: " + email.getSender();
                }
                
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
            EmailMessage sel = emailList.getSelectedValue();
            StringBuilder sb = new StringBuilder();

            sb.append("Subject: ").append(sel.getSubject()).append("\n");
            sb.append("From: ").append(sel.getSender()).append("\n");

            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.util.Date dateToShow = sel.getReceivedDate();
            if (dateToShow != null) {
                sb.append("Date: ").append(fmt.format(dateToShow)).append("\n");
            } else {
                sb.append("Date: N/A\n");
            }

            sb.append("────────────────────────────────────────\n\n");
            sb.append(sel.getContent()).append("\n\n");

            List<String> atts = sel.getAttachmentPaths();
            if (atts != null && !atts.isEmpty()) {
                sb.append("Attachments:\n");
                for (String path : atts) {
                    sb.append("  • ").append(new java.io.File(path).getName()).append("\n");
                }
            }

            emailContent.setText(sb.toString());
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
        composeDialog.setLayout(new BorderLayout());

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
      JTextField toField = new JTextField(to);
      JTextField subjectField = new JTextField(subject);
        JTextArea bodyArea = new JTextArea(body, 10, 40);
      bodyArea.setLineWrap(true);
      bodyArea.setWrapStyleWord(true);

        fieldsPanel.add(new JLabel("To:"));
      fieldsPanel.add(toField);
        fieldsPanel.add(new JLabel("Subject:"));
      fieldsPanel.add(subjectField);

      JPanel bottomPanel = new JPanel();
      JButton attachButton = new JButton("Attach Files");
      JButton sendButton = new JButton("Send");
        JButton scheduleButton = new JButton("Schedule");
        JButton mailingListButton = new JButton("Use Mailing List");
      JLabel attachedFilesLabel = new JLabel("No files attached");

      List<File> attachedFiles = new ArrayList<>();

      attachButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            if (fileChooser.showOpenDialog(composeDialog) == JFileChooser.APPROVE_OPTION) {
                attachedFiles.clear();
                attachedFiles.addAll(Arrays.asList(fileChooser.getSelectedFiles()));
                attachedFilesLabel.setText(attachedFiles.size() + " file(s) attached");
            }
        });

        mailingListButton.addActionListener(e -> {
            MailingListManager manager = MailingListManager.getInstance();
            List<MailingList> lists = manager.getMailingLists();
            if (lists.isEmpty()) {
                JOptionPane.showMessageDialog(composeDialog,
                    "No mailing lists available. Please create one first.",
                    "No Mailing Lists",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] listNames = lists.stream()
                .map(MailingList::getName)
                .toArray(String[]::new);

            String selectedList = (String) JOptionPane.showInputDialog(
                composeDialog,
                "Select a mailing list:",
                "Choose Mailing List",
                JOptionPane.QUESTION_MESSAGE,
                null,
                listNames,
                listNames[0]);

            if (selectedList != null) {
                MailingList list = manager.getMailingListByName(selectedList);
                toField.setText(String.join(", ", list.getMembers()));
            }
        });

        sendButton.addActionListener(e -> {
            sendButton.setEnabled(false);
            attachButton.setEnabled(false);
            sendButton.setText("Sending...");

            Thread sendEmailThread = new Thread(() -> {
                try {
                    EmailSender.sendEmailWithAttachment(toField.getText().trim(), subjectField.getText().trim(), bodyArea.getText().trim(),
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
            });
            sendEmailThread.setDaemon(true);
            sendEmailThread.start();
        });

        scheduleButton.addActionListener(e -> {
            String recipient = toField.getText().trim();
            String emailSubject = subjectField.getText().trim();
            String emailBody = bodyArea.getText().trim();

            if (recipient.isEmpty() || emailSubject.isEmpty() || emailBody.isEmpty()) {
                JOptionPane.showMessageDialog(composeDialog,
                    "Please fill in all fields before scheduling",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            JSpinner dateTimeSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(dateTimeSpinner, "yyyy-MM-dd HH:mm");
            dateTimeSpinner.setEditor(timeEditor);

            int option = JOptionPane.showConfirmDialog(
                composeDialog,
                dateTimeSpinner,
                "Select Date and Time to Send",
                JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                java.util.Date scheduledDate = (java.util.Date) dateTimeSpinner.getValue();
                if (scheduledDate.before(new java.util.Date())) {
                    JOptionPane.showMessageDialog(composeDialog,
                        "Scheduled time must be in the future.",
                        "Invalid Time",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    EmailSender.scheduleEmailWithAttachment(
                        recipient, emailSubject, emailBody,
                        attachedFiles.toArray(new File[0]),
                        scheduledDate
                    );
                    JOptionPane.showMessageDialog(composeDialog,
                        "Email scheduled successfully for: " + scheduledDate,
                        "Scheduled", JOptionPane.INFORMATION_MESSAGE);
                    composeDialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(composeDialog,
                        "Failed to schedule email: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

      bottomPanel.add(attachButton);
        bottomPanel.add(mailingListButton);
      bottomPanel.add(sendButton);
        bottomPanel.add(scheduleButton);
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
    private void switchAccount() {
        try {
            EmailSessionManager.getInstance().close();
        } catch (Exception ignored) {}
        emailListModel.clear();
        emailContent.setText("");
        usernameField.setText("");
        passwordField.setText("");
        showLoginDialog();
    }

    private void loadArchivedEmails() {
        emailListModel.clear(); // Clear the list before loading archived emails
        List<EmailMessage> archivedEmails = EmailArchiveService.loadArchivedEmails();
        for (EmailMessage email : archivedEmails) {
            emailListModel.addElement(email);
        }
    }

    private void archiveSelectedEmail() {
        EmailMessage selectedEmail = emailList.getSelectedValue();
        if (selectedEmail != null) {
            // Move the email to the ARCHIVED folder
            EmailManager.getInstance().moveToFolder(selectedEmail.getMessageId(), "ARCHIVED");
            // The moveToFolder method now triggers the local archiving in EmailManager

            // Refresh the email list to reflect the change
            refreshEmails();
        }
    }

}