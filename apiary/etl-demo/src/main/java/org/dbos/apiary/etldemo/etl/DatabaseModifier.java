package org.dbos.apiary.etldemo.etl;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.sql.*;
import java.util.Random;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

@Service
public class DatabaseModifier {
    private static final Random random = new Random();

    public static void startRandomModification(String DB_URL, String USER, String PASS) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                executeRandomAction(DB_URL, USER, PASS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    // stopRandomModification method
    public static void stopRandomModification() {
        // Implement this method to stop the scheduled executor
    }

    private static void executeRandomAction(String DB_URL, String USER, String PASS) throws SQLException {
        int action = random.nextInt(6);
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            switch (action) {
                case 0:
                    addUser(conn);
                    break;
                case 1:
                    removeUser(conn);
                    break;
                case 2:
                    addPost(conn);
                    break;
                case 3:
                    removePost(conn);
                    break;
                case 4:
                    addLike(conn);
                    break;
                case 5:
                    removeLike(conn);
                    break;
            }
        }
    }

    public static Map<String, Long> getTableLength(String DB_URL, String USER, String PASS) throws SQLException {
        Map<String, Long> tableLengths = new HashMap<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, "public", "%", new String[]{"TABLE"});
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                String countQuery = "SELECT COUNT(*) FROM " + tableName;
                
                try (PreparedStatement pstmt = conn.prepareStatement(countQuery);
                     ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        long count = rs.getLong(1);
                        tableLengths.put(tableName, count);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting table lengths: " + e.getMessage());
            throw e;
        }
        
        return tableLengths;
    }

    private static void addUser(Connection conn) throws SQLException {
        int newUserId = getMaxId(conn, "users", "user_id") + 1;
        String sql = "INSERT INTO users (user_id, username, email, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newUserId);
            pstmt.setString(2, "user" + newUserId);
            pstmt.setString(3, "user" + newUserId + "@example.com");
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            System.out.println("Added user: " + newUserId);
        }
    }

    private static void removeUser(Connection conn) throws SQLException {
        Integer userId = getRandomId(conn, "users", "user_id");
        if (userId != null) {
            // Delete likes and posts first
            String deleteLikesQuery = "DELETE FROM likes WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteLikesQuery)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
            
            String deletePostsQuery = "DELETE FROM posts WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deletePostsQuery)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
            // Finally delete the user
            String deleteUserQuery = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserQuery)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
                System.out.println("Removed user: " + userId);
            }
        }
    }

    private static void addPost(Connection conn) throws SQLException {
        Integer userId = getRandomId(conn, "users", "user_id");
        if (userId != null) {
            int newPostId = getMaxId(conn, "posts", "post_id") + 1;
            String sql = "INSERT INTO posts (post_id, user_id, content, created_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newPostId);
                pstmt.setInt(2, userId);
                pstmt.setString(3, "Random post " + newPostId);
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();
                System.out.println("Added post: " + newPostId + " for user: " + userId);
            }
        }
    }

    private static void removePost(Connection conn) throws SQLException {
        Integer postId = getRandomId(conn, "posts", "post_id");
        if (postId != null) {
            // Delete like and posts
            String deleteLikesQuery = "DELETE FROM likes WHERE post_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteLikesQuery)) {
                pstmt.setInt(1, postId);
                pstmt.executeUpdate();
            }
            String sql = "DELETE FROM posts WHERE post_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, postId);
                pstmt.executeUpdate();
                System.out.println("Removed post: " + postId);
            }
        }
    }

    private static void addLike(Connection conn) throws SQLException {
        Integer userId = getRandomId(conn, "users", "user_id");
        Integer postId = getRandomId(conn, "posts", "post_id");
        if (userId != null && postId != null) {
            int newLikeId = getMaxId(conn, "likes", "like_id") + 1;
            String sql = "INSERT INTO likes (like_id, user_id, post_id, created_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newLikeId);
                pstmt.setInt(2, userId);
                pstmt.setInt(3, postId);
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();
                System.out.println("Added like: " + newLikeId + " for post: " + postId + " by user: " + userId);
            }
        }
    }

    private static void removeLike(Connection conn) throws SQLException {
        Integer likeId = getRandomId(conn, "likes", "like_id");
        if (likeId != null) {
            String sql = "DELETE FROM likes WHERE like_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, likeId);
                pstmt.executeUpdate();
                System.out.println("Removed like: " + likeId);
            }
        }
    }

    private static Integer getRandomId(Connection conn, String table, String idColumn) throws SQLException {
        String sql = "SELECT " + idColumn + " FROM " + table + " ORDER BY RANDOM() LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : null;
        }
    }

    private static int getMaxId(Connection conn, String table, String idColumn) throws SQLException {
        String sql = "SELECT MAX(" + idColumn + ") FROM " + table;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}