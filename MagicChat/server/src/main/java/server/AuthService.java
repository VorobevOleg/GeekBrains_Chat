package server;

import java.sql.SQLException;

public interface AuthService {
    /**
     * Получение никнейма по логину и паролю
     * возвращает никнейм если учетка есть
     * null если пары логин пароль не нашлось
     * */
    String getNicknameByLoginAndPassword(String login, String password) throws SQLException;
    /**
     * Регистрация нового пользователя
     * при успешной регистрации ( логин и никнейм не заняты ) вернет true
     * иначе вернет false
     * */
    boolean registration(String login, String password, String nickname);

    boolean changeNickname (String oldNickname, String newNickname, String password);
}
