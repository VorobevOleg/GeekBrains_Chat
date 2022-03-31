package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import service.ServiceMessages;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextField textField;
    @FXML
    public TextArea textArea;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickname;
    private String login;
    private Stage stage;
    private Stage regStage;
    private RegController regController;
    private Stage changeNickStage;
    private ChangeNicknameController changeNicknameController;
    private ArrayList<String> listHistory;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
        }

        setTitle(nickname);
        textArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF(ServiceMessages.END);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    public void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(ServiceMessages.END)) {
                                break;
                            }
                            if (str.startsWith(ServiceMessages.AUTH_OK)) {
                                nickname = str.split(" ")[1];
                                login = str.split(" ")[2];
                                setAuthenticated(true);

                                if (new File("client/history_" + login + ".txt").exists()) {
                                    listHistory = History.readLastHundredEntries("client/history_" + login + ".txt");
                                    for (String s: listHistory) {
                                        textArea.appendText(s + "\n");
                                    }
                                }

                                break;
                            }
                            if (str.startsWith(ServiceMessages.REG)) {
                                regController.regStatus(str);
                            }

                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }


                    //цикл работы
                    while (authenticated) {

                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(ServiceMessages.END)) {
                                setAuthenticated(false);
                                break;
                            }

                            if (str.startsWith(ServiceMessages.CHNICK)) {
                                changeNicknameController.changeStatus(str);
                            }

                            if (str.startsWith(ServiceMessages.CLIENT_LIST)) {
                                String[] token = str.split(" ");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }

                        } else {
                            textArea.appendText(str + "\n");
                            History.writeHistory("client/history_" + login + ".txt", str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clickBtnSendText(ActionEvent actionEvent) {
        if (textField.getText().length() > 0) {
            try {
                out.writeUTF(textField.getText());
                textField.clear();
                textField.requestFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickBtnAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            String msg = String.format("%s %s %s", ServiceMessages.AUTH,
                    loginField.getText().trim(), passwordField.getText().trim());
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        String title;
        if (nickname.equals("")) {
            title = "Magic chat";
        } else {
            title = String.format("Magic chat - %s", nickname);
        }
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    public void clickClientList(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText(ServiceMessages.W + " " + receiver + " ");
    }

    public void clickBtnReg(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Magic chat registration");
            regStage.setScene(new Scene(root, 500, 425));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fxmlLoader.getController();
            regController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickBtnChangenickname(ActionEvent actionEvent) {
        if (changeNickStage == null) {
            createChangeNickWindow();
        }
        changeNickStage.show();
    }

    private void createChangeNickWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/changeNickname.fxml"));
            Parent root = fxmlLoader.load();
            changeNickStage = new Stage();
            changeNickStage.setTitle("Magic chat - change nickname");
            changeNickStage.setScene(new Scene(root, 500, 425));

            changeNickStage.initModality(Modality.APPLICATION_MODAL);
            changeNickStage.initStyle(StageStyle.UTILITY);

            changeNicknameController = fxmlLoader.getController();
            changeNicknameController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToChangeNickname(String newNickname, String oldNickname, String password) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("%s %s %s %s", ServiceMessages.CHNICK, newNickname, oldNickname, password);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
