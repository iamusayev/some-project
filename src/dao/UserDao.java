package dao;

import entity.UserEntity;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import model.Gender;
import model.Role;
import util.ConnectionManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDao {

    private static final UserDao INSTANCE = new UserDao();


    private static final String FIND_ALL_SQL = "SELECT * FROM users";
    private static final String SAVE_SQL = """
                        
            INSERT INTO users (name, email, password, role, gender, birthday) 
            VALUES (?, ?, ?, ?, ?, ?)
                        
            """;
    private static final String FIND_BY_EMAIL_AND_PASSWORD_SQL = FIND_ALL_SQL + " WHERE email = ? AND password = ? ";

    @SneakyThrows
    public void save(UserEntity entity) {
        try (var connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {
            preparedStatement.setObject(1, entity.getName());
            preparedStatement.setObject(2, entity.getEmail());
            preparedStatement.setObject(3, entity.getPassword());
            preparedStatement.setObject(4, entity.getRole().name());
            preparedStatement.setObject(5, entity.getGender().name());
            preparedStatement.setObject(6, entity.getBirthday());

            preparedStatement.executeUpdate();
        }
    }

    @SneakyThrows
    public List<UserEntity> findAll() {
        try (var connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<UserEntity> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;
        }
    }

    @SneakyThrows
    public Optional<UserEntity> findByEmailAndPassword(String email, String password) {
        try (var connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_EMAIL_AND_PASSWORD_SQL)) {
            preparedStatement.setObject(1, email);
            preparedStatement.setObject(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            UserEntity entity = null;
            if (resultSet.next()) {
                entity = buildUser(resultSet);
            }
            return Optional.ofNullable(entity);
        }
    }

    private UserEntity buildUser(ResultSet resultSet) throws SQLException {
        return UserEntity.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .role(Role.valueOf(resultSet.getString("role")))
                .gender(Gender.valueOf(resultSet.getString("gender")))
                .email(resultSet.getString("email"))
                .password(resultSet.getString("password"))
                .build();
    }


    public static UserDao getInstance() {
        return INSTANCE;
    }
}
