package server;

import java.sql.*;

public class DbAuthService implements AuthService {

    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;
    private static PreparedStatement psGetNickname;
    private static PreparedStatement psChangeNick;

    public DbAuthService() {
        try {
            connect();
            clearTable();
            prepareStatements();
            psInsert.setString(1, "qwe");
            psInsert.setString(2, "qwe");
            psInsert.setString(3, "qwe");
            psInsert.executeUpdate();
            psInsert.setString(1, "asd");
            psInsert.setString(2, "asd");
            psInsert.setString(3, "asd");
            psInsert.executeUpdate();
            psInsert.setString(1, "zxc");
            psInsert.setString(2, "zxc");
            psInsert.setString(3, "zxc");
            psInsert.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public static void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    public static void disconnect() {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void clearTable() throws SQLException {
        stmt.executeUpdate("DELETE FROM users;");
    }

    public static void prepareStatements() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO users (login, password, nickname) VALUES ( ? , ? , ?);");
    }

    public static void prepareChangeNickname() throws SQLException {
        psChangeNick = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ? AND password = ?;");
    }

    public static void prepareGetNicknameByLoginAndPassword() throws SQLException {
        psGetNickname = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? and password = ?;");
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) throws SQLException {
        try {
            connect();
            prepareGetNicknameByLoginAndPassword();
            psGetNickname.setString(1, login);
            psGetNickname.setString(2, password);
            ResultSet rs =  psGetNickname.executeQuery();
            String rss = rs.getString("nickname");
            rs.close();
            return rss;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            connect();
            prepareStatements();
            psInsert.setString(1, login);
            psInsert.setString(2, password);
            psInsert.setString(3, nickname);
            psInsert.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return false;
    }

    @Override
    public boolean changeNickname(String oldNickname, String newNickname, String password) {
        try {
            connect();
            prepareChangeNickname();
            psChangeNick.setString(1, newNickname);
            psChangeNick.setString(2, oldNickname);
            psChangeNick.setString(3, password);
            if (psChangeNick.executeUpdate() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return false;
    }

}
