package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import service.ServiceMessages;

public class ChangeNicknameController {

    @FXML
    public TextField oldNicknameField;
    @FXML
    public TextField newNicknameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextArea textArea;

    private Controller controller;

    @FXML
    public void clickBtnChangeNickname(ActionEvent actionEvent) {
        String newNickname =  newNicknameField.getText().trim();
        String oldNickname = oldNicknameField.getText().trim();
        String password = passwordField.getText().trim();
        controller.tryToChangeNickname(newNickname, oldNickname, password);
    }

    public void changeStatus(String result) {
        if (result.equals(ServiceMessages.CHNICK_OK)) {
            textArea.appendText("Смена никнейма прошла успешно\n");
        } else {
            textArea.appendText("Смена никнейма не получилась. Нет такого никнейма\n");
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
