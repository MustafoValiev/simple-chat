import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

public class ClientGUI extends JFrame implements ActionListener, AdjustmentListener {
    private JTextField usernameTextField;
    private JLabel authResultText;

    private final CardLayout layout;
    private final JPanel mainPanel;

    private JPanel chatArea;

    private JScrollPane chatAreaScrollPane;

    private JTextField messageTextField;

    DataInputStream inputStream;
    DataOutputStream outputStream;

    private final int window_width = 600;
    private final int window_height = 800;

    private static String username;

    private boolean isLoggingIn;

    public ClientGUI(DataInputStream inputStream, DataOutputStream dataOutputStream) {
        super("Client");

        this.inputStream = inputStream;
        this.outputStream = dataOutputStream;

        layout = new CardLayout();
        mainPanel = new JPanel(layout);

        JPanel chatPanel = createChatPanel();
        JPanel authPanel = createAuthPanel();

        mainPanel.add(authPanel, "authpanel");
        mainPanel.add(chatPanel, "chatpanel");

        layout.first(mainPanel);
        add(mainPanel);

        setFocusable(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(window_width, window_height));
        setLocationRelativeTo(null);
        setVisible(true);

        StartThreads();
    }

    public void SetWindowTitle(String title) {
        this.setTitle(title);
    }

    private JPanel createAuthPanel() {
        var jPanel = new JPanel();

        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

        jPanel.add(Box.createVerticalGlue());

        var icon = new ImageIcon("images/inha_logo.png");
        var text = new JLabel(icon);
        text.setText("CHAT");
        text.setHorizontalTextPosition(JLabel.CENTER);
        text.setVerticalTextPosition(JLabel.BOTTOM);
        text.setForeground(Constants.ButtonBackground);
        text.setFont(new Font("Arial", Font.BOLD, 72));
        text.setAlignmentX(0.5f);
        jPanel.add(text);

        jPanel.add(Box.createVerticalStrut(50));

        usernameTextField = new JTextField(20);
        usernameTextField.setMaximumSize(new Dimension(300, 30));
        usernameTextField.setAlignmentX(0.5f);
        usernameTextField.setForeground(Color.GRAY);
        usernameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                var usernameValue = usernameTextField.getText();
                if (usernameValue.length() >= Constants.MAX_USERNAME_LENGTH && !(evt.getKeyChar() == KeyEvent.VK_DELETE || evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
                    getToolkit().beep();
                    evt.consume();
                    authResultText.setText(String.format("Max limit is %d characters. Try again!", Constants.MAX_USERNAME_LENGTH));
                    authResultText.setForeground(Constants.RED);
                    authResultText.setVisible(true);
                    usernameTextField.setText(usernameValue.substring(0, Constants.MAX_USERNAME_LENGTH));
                } else {
                    authResultText.setVisible(false);
                }
            }
        });
        jPanel.add(usernameTextField);

        jPanel.add(Box.createVerticalStrut(10));

        var button = new JButton("Enter");
        button.setAlignmentX(0.5f);
        button.setForeground(Constants.WHITE);
        button.setBackground(Constants.ButtonBackground);
        button.addActionListener(this);
        button.setFocusPainted(false);
        jPanel.add(button);

        jPanel.add(Box.createVerticalStrut(10));

        authResultText = new JLabel();
        authResultText.setForeground(Constants.BLACK);
        authResultText.setFont(new Font("Arial", Font.PLAIN, 18));
        authResultText.setAlignmentX(0.5f);
        authResultText.setVisible(false);
        jPanel.add(authResultText);

        jPanel.add(Box.createVerticalGlue());

        jPanel.setBackground(Constants.WHITE);

        return jPanel;
    }

    private JPanel createChatPanel() {
        var jPanel = new JPanel();

        jPanel.setLayout(new BorderLayout());

        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.PAGE_AXIS));
        chatArea.setBackground(Constants.ChatAreaBackground);

        chatAreaScrollPane = new JScrollPane(chatArea);
        chatAreaScrollPane.getVerticalScrollBar().addAdjustmentListener(this);
        chatAreaScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);

        var bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        bottomPanel.setBackground(Constants.WHITE);
        var constraints = new GridBagConstraints();

        messageTextField = new JTextField(20);
        messageTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                var messageValue = messageTextField.getText();
                if (messageValue.length() >= Constants.MAX_MESSAGE_LENGTH && !(evt.getKeyChar() == KeyEvent.VK_DELETE || evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
                    getToolkit().beep();
                    evt.consume();
                    messageTextField.setText(messageValue.substring(0, Constants.MAX_MESSAGE_LENGTH));
                }
            }
        });
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.9;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(5, 5, 5, 5);
        bottomPanel.add(messageTextField, constraints);

        var sendButton = new JButton("Send");
        sendButton.setForeground(Constants.WHITE);
        sendButton.setBackground(Constants.ButtonBackground);
        sendButton.addActionListener(this);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.1;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.insets = new Insets(5, 5, 5, 5);
        bottomPanel.add(sendButton, constraints);

        jPanel.add(chatAreaScrollPane, BorderLayout.CENTER);
        jPanel.add(bottomPanel, BorderLayout.PAGE_END);

        return jPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        switch (e.getActionCommand().toLowerCase()) {
            case "enter": {
                if (isLoggingIn) return;

                authResultText.setVisible(false);

                var username = usernameTextField.getText().trim();

                if (username.isEmpty()) {
                    authResultText.setText("Username cannot be empty. Try again!");
                    authResultText.setForeground(Constants.RED);
                    authResultText.setVisible(true);
                } else if (username.matches("^[0-9]+.*")) {
                    authResultText.setText("Username cannot start with numbers. Try again!");
                    authResultText.setForeground(Constants.RED);
                    authResultText.setVisible(true);
                } else {
                    try {
                        isLoggingIn = true;
                        ClientGUI.username = username;
                        outputStream.writeUTF(String.format("enter/%s", username));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                break;
            }
            case "send": {
                var message = messageTextField.getText().trim();
                if (!message.isEmpty()) {
                    messageTextField.setText("");
                    this.AddMessageToChatArea(message, username);

                    try {
                        outputStream.writeUTF(String.format("chat/%s", message));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                break;
            }
        }
    }

    private void AddMessageToChatArea(String message, String username) {
        var owner = username.equals(ClientGUI.username) ? Constants.MessageOwner.SELF : Constants.MessageOwner.OTHER;

        var jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.setBackground(Constants.TRANSPARENT);

        var textWrapper = new JPanel();
        textWrapper.setLayout(new BorderLayout());
        textWrapper.setBackground(Constants.TRANSPARENT);
        textWrapper.setMaximumSize(new Dimension(window_width / 2, 50));

        var messageWrapper = new JPanel(new FlowLayout(owner == Constants.MessageOwner.SELF ? FlowLayout.RIGHT : FlowLayout.LEFT));
        messageWrapper.setBackground(owner == Constants.MessageOwner.SELF
                ? Constants.MessageSELFBackground
                : Constants.MessageOTHERBackground);
        messageWrapper.setMaximumSize(new Dimension(window_width / 2, 31));

        var messageText = new JLabel(message, SwingConstants.RIGHT);
        messageText.setForeground(Color.WHITE);

        messageWrapper.add(messageText);

        var usernameWrapper = new JPanel(new FlowLayout(owner == Constants.MessageOwner.SELF ? FlowLayout.RIGHT : FlowLayout.LEFT));
        usernameWrapper.setBackground(Constants.TRANSPARENT);
        usernameWrapper.setMaximumSize(new Dimension(window_width / 2, 15));

        var usernameText = new JLabel(owner == Constants.MessageOwner.SELF ? "me" : username, SwingConstants.LEADING);
        usernameText.setForeground(Color.GRAY);
        usernameText.setFont(new Font("Arial", Font.PLAIN, 10));

        usernameWrapper.add(usernameText);

        textWrapper.add(usernameWrapper, BorderLayout.PAGE_START);
        textWrapper.add(messageWrapper, BorderLayout.PAGE_END);

        if (owner == Constants.MessageOwner.SELF) {
            jPanel.add(Box.createHorizontalGlue());
            jPanel.add(textWrapper);
        } else {
            jPanel.add(textWrapper);
            jPanel.add(Box.createHorizontalGlue());
        }

        chatArea.add(jPanel);
        chatArea.add(Box.createVerticalStrut(5));

        chatArea.updateUI();

        var vertical = chatAreaScrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private void StartThreads() {
        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // read the message sent to this client
                        String message = inputStream.readUTF();
                        String[] parts = message.split("/");
                        String operation = parts[0];

                        switch (operation) {
                            case "enter": {
                                var type = parts[1];

                                if (type.equals("success")) {
                                    SetWindowTitle("Client: " + ClientGUI.username);

                                    authResultText.setForeground(Constants.GREEN);
                                    authResultText.setText(parts[2]);
                                    authResultText.setVisible(true);

                                    Thread.sleep(2000);

                                    int delay = 3;
                                    for (var i = 0; i < delay; i++) {
                                        authResultText.setText(String.format("Redirecting in %d seconds.", delay - i));
                                        Thread.sleep(1000);
                                    }
                                    if (parts.length == 4) {
                                        var joinedMessages = parts[3];
                                        var items = joinedMessages.split("#");

                                        for (var i = items.length - 1; i >= 0; i--) {
                                            var item = items[i];
                                            var itemParts = item.split("@");
                                            var itemUsername = itemParts[0].trim();
                                            var itemMessage = itemParts[1].trim();

                                            AddMessageToChatArea(itemMessage, itemUsername);
                                        }
                                    }

                                    layout.next(mainPanel);

                                    var vertical = chatAreaScrollPane.getVerticalScrollBar();
                                    vertical.setValue(vertical.getMaximum());
                                } else if (type.equals("error")) {
                                    authResultText.setForeground(Constants.RED);
                                    authResultText.setText(parts[2]);
                                    authResultText.setVisible(true);
                                }
                                isLoggingIn = false;
                                break;
                            }
                            case "chat": {
                                var sender = parts[1];
                                var sender_message = parts[2];

                                AddMessageToChatArea(sender_message, sender);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });

        readMessage.start();
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        chatArea.updateUI();
    }
}
