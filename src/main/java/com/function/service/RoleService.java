package com.function.service;

import com.function.model.Role;
import com.function.model.User;
import com.function.repository.RoleRepository;

import java.sql.SQLException;
import java.util.List;

public class RoleService {

    public static List<Role> getAllRoles() throws SQLException {
        return RoleRepository.getAllRoles();
    }

    public static Role getRoleById(int roleId) throws SQLException {
        return RoleRepository.getRoleById(roleId);
    }

    public static Role createRole(Role role) throws SQLException {
        return RoleRepository.createRole(role);
    }

    public static boolean updateRole(Role role) throws SQLException {
        return RoleRepository.updateRole(role);
    }

    public static boolean deleteRole(int roleId) throws SQLException {
        return RoleRepository.deleteRole(roleId);
    }

    public static List<User> getUsersByRole(int roleId) throws SQLException {
        return RoleRepository.getUsersByRole(roleId);
    }

    public static int bulkUpdateUserRole(List<User> users, int newRoleId) throws SQLException {
        try {
            return RoleRepository.bulkUpdateUserRole(users, newRoleId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
}