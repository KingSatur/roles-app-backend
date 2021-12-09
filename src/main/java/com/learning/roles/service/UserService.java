package com.learning.roles.service;


import com.learning.roles.domain.User;
import com.learning.roles.domain.exception.EmailExistException;
import com.learning.roles.domain.exception.UsernameExistsException;
import com.learning.roles.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.*;

public interface UserService {

    User registerUser(UserDto userDto) throws EmailExistException,
            UsernameExistsException;

    List<User> getUsers(Long limit, Long page);

    String updateProfileImage(Long id, MultipartFile file);

    User findByUsername(String username);
    User findByEmail(String email);

    User findById(Long id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean deleteUser(Long id);

    void resetPassword(String email);

    User saveUser(UserDto userDto, MultipartFile file) throws EmailExistException, UsernameExistsException;
}
