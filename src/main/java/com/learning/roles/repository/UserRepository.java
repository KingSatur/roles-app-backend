package com.learning.roles.repository;


import com.learning.roles.domain.User;
import com.learning.roles.repository.rowmapper.UserRowMapper;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public class UserRepository {

    private static final String INSERT_QUERY = "INSERT INTO USER_APPLICATION " +
            "(userId, username, firstName, lastName, password, email, " +
            "profileImage, joinDate, lastLoginDate, role, authorities, isActive, isNotLocked) " +
            "VALUES " +
            "(:userId, :username, :firstName, :lastName, " +
            ":password, :email, :profileImage, :joinDate,:lastLoginDate, :role, :authorities," +
            ":isActive, :isNotLocked);";
    private static final String UPDATE_QUERY = "UPDATE USER_APPLICATION " +
            "SET password = :password, email = :email, " +
            "profileImage = :profileImage, lastLoginDate = :lastLoginDate, role = :role," +
            "authorities = :authorities, isActive = :isActive, isNotLocked = :isNotLocked " +
            "WHERE id = :id";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate
                                  namedParameterJdbcTemplate,
                          JdbcTemplate jdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<User> findUserByUsername(String username){
        try {
            MapSqlParameterSource paramSource = new
                    MapSqlParameterSource();
            paramSource.addValue("username", username);
            return Optional.of(
                    this.namedParameterJdbcTemplate.queryForObject(
                            "SELECT * FROM USER_APPLICATION WHERE "
                                    + "username = :username", paramSource,
                            new UserRowMapper()
                    ));
        }
        catch (Exception e){
            return Optional.empty();
        }
    }

    public Optional<User> findUserById(Long id){
        MapSqlParameterSource paramSource = new
                MapSqlParameterSource();
        paramSource.addValue("id", id);
        return Optional.of(
                this.namedParameterJdbcTemplate.queryForObject(
                        "SELECT * FROM USER_APPLICATION WHERE "
                                + "id = :id", paramSource,
                        new UserRowMapper()
                ));
    }

    public Optional<List<User>> findAllUsers(Long limit, Long page){
        Long offset = (page - 1) * limit;
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("limit", limit);
        paramSource.addValue("offset", offset);
        return  Optional.of(
                this.namedParameterJdbcTemplate.
                        query("SELECT * FROM USER_APPLICATION " +
                                        "LIMIT :limit " +
                                        "OFFSET :offset",
                                paramSource,
                                new UserRowMapper()));
    }

    public Optional<User> save(User user){

        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("firstName", user.getFirstName());
        paramSource.addValue("userId", user.getUserId());
        paramSource.addValue("lastName", user.getLastName());
        paramSource.addValue("username", user.getUsername());
        paramSource.addValue("password", user.getPassword());
        paramSource.addValue("email", user.getEmail());
        paramSource.addValue("profileImage", user.getProfileImage());
        paramSource.addValue("lastLoginDate",
                user.getLastLoginDate() != null ?
                        java.sql.Timestamp.from(user.getLastLoginDate())
                        : null);
        paramSource.addValue("joinDate",
                java.sql.Timestamp.from(user.getJoinDate()));
        paramSource.addValue("role", user.getRole());
        paramSource.addValue("authorities",
                user.getAuthorities());
        paramSource.addValue("isActive",
                user.isActive());
        paramSource.addValue("isNotLocked",
                user.isNotLocked());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if(user.getId() != null){
            paramSource.addValue("id", user.getId());
        }
        this.namedParameterJdbcTemplate.update(
                user.getId() == null ? INSERT_QUERY : UPDATE_QUERY
                , paramSource,keyHolder,new String[] { "id" });
        Long id = keyHolder.getKey().longValue();
        user.setId(id);
        return Optional.of(user);
    }

    public Optional<User> findUserByEmail(String email){
        try{
            MapSqlParameterSource paramSource = new
                    MapSqlParameterSource();
            paramSource.addValue("email", email);
            return Optional.of(
                    this.namedParameterJdbcTemplate.queryForObject(
                            "SELECT * FROM USER_APPLICATION" +
                                    " WHERE  email = :email",
                            paramSource, new UserRowMapper())
            );
        }
        catch (IncorrectResultSizeDataAccessException e){
            return Optional.empty();
        }
    }

    public boolean existsByEmail(String email) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("email", email);
        Integer count = this.namedParameterJdbcTemplate.queryForObject("SELECT COUNT(1) FROM USER_APPLICATION U " +
                "WHERE U.email = :email;", paramSource, Integer.class);
        return count > 0;
    }

    public boolean existsByUsername(String username) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("username", username);
        Integer count = this.namedParameterJdbcTemplate.queryForObject("SELECT COUNT(1) FROM USER_APPLICATION U " +
                "WHERE U.username = :username", paramSource, Integer.class);
        return count > 0;
    }

    public boolean deleteUser(Long id){
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("id",id);
        int rowsAffected = this.namedParameterJdbcTemplate.
                update("DELETE FROM USER_APPLICATION U WHERE U.id = :id", paramSource);
        return rowsAffected > 0;
    }
}
