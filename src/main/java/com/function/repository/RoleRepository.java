package com.function.repository;

import com.function.OracleDBConnection;
import com.function.model.Role;
import com.function.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoleRepository {
    private static final String TABLE_NAME = "ROLES_DC2";

    public static List<Role> getAllRoles() throws SQLException {
        List<Role> roles = new ArrayList<>();
        try (Connection conn = OracleDBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME)) {

            while (rs.next()) {
                roles.add(mapResultSetToRole(rs));
            }
        }
        return roles;
    }

    public static Role getRoleById(int roleId) throws SQLException {
        try (Connection conn = OracleDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM " + TABLE_NAME + " WHERE ROLE_ID = ?")) {

            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRole(rs);
            }
            return null;
        }
    }

    public static Role createRole(Role role) throws SQLException {
        try (Connection conn = OracleDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO " + TABLE_NAME + " (NAME, DESCRIPTION) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, role.getName());
            stmt.setString(2, role.getDescription());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (PreparedStatement selectStmt = conn.prepareStatement(
                        "SELECT ROLE_ID FROM " + TABLE_NAME + " WHERE NAME = ? ORDER BY ROLE_ID DESC FETCH FIRST 1 ROW ONLY")) {
                    selectStmt.setString(1, role.getName());
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            role.setRoleId(rs.getInt("ROLE_ID"));
                        }
                    }
                }
                return role;
            }
            throw new SQLException("No se pudo crear el rol");
        }
    }

    public static boolean updateRole(Role role) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET NAME = ?, DESCRIPTION = ? WHERE ROLE_ID = ?";

        try (Connection conn = OracleDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.getName());
            stmt.setString(2, role.getDescription());
            stmt.setInt(3, role.getRoleId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean deleteRole(int roleId) throws SQLException {
        try (Connection conn = OracleDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM " + TABLE_NAME + " WHERE ROLE_ID = ?")) {

            stmt.setInt(1, roleId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static List<User> getUsersByRole(int roleId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, email FROM USERS_DC2 WHERE role_id = ?";
        
        try (Connection conn = OracleDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, roleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    users.add(user);
                }
            }
        }
        return users;
    }

    public static int bulkUpdateUserRole(List<User> users, int newRoleId) throws SQLException {
        if (users == null || users.isEmpty()) {
            return 0;
        }
    
        String sql = "UPDATE USERS_DC2 SET ROLE_ID = ? WHERE USER_ID = ?";
        Connection conn = null;
        try {
            conn = OracleDBConnection.getConnection();
            conn.setAutoCommit(false); // Deshabilitar auto-commit para transacción
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int batchSize = 100; // Tamaño del lote
                int totalUpdated = 0;
                
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    stmt.setInt(1, newRoleId);
                    stmt.setInt(2, user.getUserId());
                    stmt.addBatch();
                    
                    // Ejecutar cada batchSize o al final
                    if ((i + 1) % batchSize == 0 || (i + 1) == users.size()) {
                        int[] batchResults = stmt.executeBatch();
                        totalUpdated += Arrays.stream(batchResults).sum();
                    }
                }
                
                conn.commit(); // Confirmar transacción
                return totalUpdated;
            }
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revertir en caso de error
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Restaurar auto-commit
                conn.close();
            }
        }
    }

    private static Role mapResultSetToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setRoleId(rs.getInt("ROLE_ID"));
        role.setName(rs.getString("NAME"));
        role.setDescription(rs.getString("DESCRIPTION"));
        role.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        return role;
    }
}