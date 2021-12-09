package com.learning.roles.repository.rowmapper;

import com.learning.roles.domain.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("userId"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("profileImage"),
                rs.getTimestamp("lastLoginDate") != null ?
                rs.getTimestamp("lastLoginDate").
                        toInstant(): null,
                rs.getTimestamp("joinDate").toInstant(),
                    rs.getString("role"),
                ((String[]) rs.getArray("authorities").getArray()),
                rs.getBoolean("isActive"),
                rs.getBoolean("isNotLocked"));
    }
}
