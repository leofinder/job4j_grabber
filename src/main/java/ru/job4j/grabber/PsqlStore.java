package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream input = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Store store = new PsqlStore(config);
        store.save(new Post(0, "First Post", "http://example.com/first-post", "This is the text of the first post.", LocalDateTime.of(2023, 1, 1, 10, 12, 53)));
        store.save(new Post(0, "Second Post",  "http://example.com/second-post", "This is the text of the second post.", LocalDateTime.of(2023, 1, 2, 11, 36, 0)));
        store.save(new Post(0, "Third Post", "http://example.com/third-post", "This is the text of the third post.",  LocalDateTime.of(2023, 1, 3, 12, 0, 54)));
        store.save(new Post(0, "Fourth Post", "http://example.com/fourth-post", "This is the text of the fourth post.",  LocalDateTime.of(2023, 1, 4, 13, 21, 7)));
        store.save(new Post(0, "Fifth Post", "http://example.com/fifth-post", "This is the text of the fifth post.",  LocalDateTime.of(2023, 1, 5, 14, 45, 45)));

        System.out.println(store.findById(2));
        System.out.println(store.findById(5));
        System.out.println(store.findById(10));

        List<Post> posts = store.getAll();
        posts.forEach(System.out::println);
    }

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("rabbit.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        String url = config.getProperty("rabbit.url");
        String user = config.getProperty("rabbit.username");
        String password = config.getProperty("rabbit.password");
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Post getPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?) ON CONFLICT (link) DO NOTHING",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(getPost(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    post = getPost(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}