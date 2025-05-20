# Java Email Client

A simple desktop email client built with Java Swing, demonstrating core email functionalities including sending, receiving, folder management, searching, mailing lists, and local archiving.

## Features

*   **Email Sending & Receiving:** Supports sending and receiving emails using SMTP and IMAP/POP3 protocols.
*   **Folder Management:** Organizes emails into standard folders (INBOX, SENT, DRAFT, TRASH) and virtual folders based on sender.
*   **Attachments:** Handles sending emails with multiple attachments.
*   **Mailing Lists:** Allows creating, managing, and using mailing lists for sending group emails.
*   **Search:** Provides search functionality within folders based on subject, sender, body, and date.
*   **Local Archiving:**
    *   **Permanent Archive (`sent_emails`):** Automatically saves sent emails locally as XML files, loaded into the SENT folder view, filtered by the current user.
    *   **Temporary Archive (`archived_emails`):** Saves emails moved to the ARCHIVED folder locally as XML files, loaded into the ARCHIVED folder view. This archive is cleared upon application exit.
*   **Basic Error Handling:** Includes basic handling for connection errors.
*   **Account Management Foundation:** Basic structure for handling a current email account session (initial multi-account UI was present but removed in recent modifications).

## Prerequisites

*   Java Development Kit (JDK) 11 or higher
*   Maven (for dependency management and building)
*   An email account (Gmail requires App Passwords for third-party applications)

## Setup and Running

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/redonelach1/JavaMailProject
    cd <project_directory>
    ```
    If not using Git, navigate to the project's root directory.

2.  **Build the Project with Maven:**
    Open a terminal in the project's root directory and run:
    ```bash
    mvn clean install
    ```
    This will download dependencies and build the project.

3.  **Run the Application:**
    You can run the application using the Maven exec plugin:
    ```bash
    mvn exec:java -Dexec.mainClass="com.java.swing.EmailClientGUI"
    ```
    Alternatively, if you have an IDE configured for Maven (like Eclipse or IntelliJ IDEA), you can run the `main` method in `src/com/java/swing/EmailClientGUI.java` directly from the IDE after importing the Maven project.

## Using the Client

*   Upon launching, you will be prompted to enter your email address and App Password (if using Gmail).
*   Navigate through folders using the tree on the left.
*   Use the buttons at the top and bottom for actions like Compose, Refresh, Search, Archive, etc.
*   Mailing lists and account management features can be accessed via their respective buttons (assuming the UI for account management is re-implemented).

## Note on Gmail and App Passwords

If you are using a Gmail account, you **must** use a Google App Password instead of your regular Gmail password. You need to enable 2-Step Verification on your Google account first, then generate an App Password from your Google Account security settings. 
