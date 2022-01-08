package com.learning.roles.service;

import com.learning.roles.constant.FileConstant;
import com.learning.roles.constant.enums.Role;
import com.learning.roles.domain.User;
import com.learning.roles.domain.UserPrincipal;
import com.learning.roles.domain.exception.EmailExistException;
import com.learning.roles.domain.exception.UsernameExistsException;
import com.learning.roles.dto.UserDto;
import com.learning.roles.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Qualifier("UserDetailServicePostgresql")
@Transactional
public class UserServicePostgresql implements UserService, UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    public UserServicePostgresql(
            UserRepository userRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            LoginAttemptService loginAttemptService, EmailService emailService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws
            UsernameNotFoundException {
        Optional<User> userOptional = this.userRepository.
                findUserByEmail(email);
        if(!userOptional.isPresent()){
            LOGGER.error(Instant.now().toString(), "User not found: " + email);
            throw new UsernameNotFoundException("User with that email " +
                    " does not exist");
        }
        User user = userOptional.get();
//        validateLoginAttempt(user);
        user.setLastLoginDate(Instant.now());
        this.userRepository.save(user);
        LOGGER.info(Instant.now().toString(), "Returning found user by email: " + email);
        return new UserPrincipal(user);
    }

    private void validateLoginAttempt(User user) {
        if(user.isNotLocked()){
            if(this.loginAttemptService.
                    hasExceededMaxAttempts(user.getEmail())){
                user.setNotLocked(false);
            }
            else{
                user.setNotLocked(true);
            }
        } else{
            loginAttemptService.
                    evictUserFromLoginAttemptCache(user.getUsername());
        }

    }

    private User mapUserData(UserDto userDto){
        User user = new User();
        user.setId(userDto.getId());
        user.setUserId(generateUserId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUserName());
        user.setJoinDate(Instant.now());
        user.setActive(userDto.isActive());
        user.setNotLocked(userDto.isNotLocked());
        user.setRole(this.getRoleFromString(userDto.getRole()).name());
        user.setAuthorities(this.getRoleFromString(userDto.getRole()).getPermissions());
        user.setProfileImage(getTemporarilyProfileImage(userDto.getUserName()));
        return user;
    }

    private Role getRoleFromString(String role){
        return Role.valueOf(role);
    }

    @Override
    public User registerUser(UserDto userDto)
            throws EmailExistException, UsernameExistsException {
        this.validateEmailAndUsername(userDto);
        String encodedPass = encodePassword(userDto.getPassword());
        User u =  this.mapUserData(userDto);
        u.setPassword(encodedPass);
        u = this.userRepository.save(u).get();
        LOGGER.info(Instant.now().toString(), " User has been registered");
        return u;
    }

    @Override
    public List<User> getUsers(Long limit, Long page) {
        return this.userRepository.findAllUsers(limit, page).get();
    }

    @Override
    public String updateProfileImage(Long id, MultipartFile file) {
        User u = this.userRepository.findUserById(id).get();
        u.setProfileImage(this.getProfileImageUrl(u.getUsername(), file));
        this.userRepository.save(u);
        return u.getProfileImage();
    }

    private String getProfileImageUrl(String username, MultipartFile file) {
        try {
            Path userFolder = Paths.get(FileConstant.USER_FOLDER + username).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)){
                Files.createDirectories(userFolder);
                LOGGER.info(FileConstant.DIRECTORY_CREATED);
            }
            Files.deleteIfExists(Paths.get(userFolder + username + FileConstant.DOT + FileConstant.JPG_EXTENSION));
            Files.copy(file.getInputStream(),
                    userFolder.resolve(username +
                            FileConstant.DOT +
                            FileConstant.JPG_EXTENSION),
                    StandardCopyOption.REPLACE_EXISTING
            );
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(FileConstant.DEFAULT_USER_IMAGE_PATH +
                            username +
                            FileConstant.FORWARD_SLASH +
                            username+
                            FileConstant.DOT +
                            FileConstant.JPG_EXTENSION).toUriString();
        }
        catch (Exception e){
            return null;
        }

    }

    private String getTemporarilyProfileImage(String userName) {
        return "http://localhost:9090/user/image/temp/" + userName;
    }

    private String encodePassword(String passw) {
        return this.bCryptPasswordEncoder.encode(passw);
    }


    private String generateUserId() {
        return UUID.randomUUID().toString().substring(0, 29);
    }

    private boolean validateEmailAndUsername(UserDto userDto) throws EmailExistException, UsernameExistsException {
        if(this.existsByEmail(userDto.getEmail())){
            throw new EmailExistException("Email already exists");
        }
        if(this.existsByUsername(userDto.getUserName())){
            throw new UsernameExistsException("Username already exists");
        }
        return false;
    }



    @Override
    public User findByUsername(String username) {
        return this.userRepository.findUserByUsername(username).get();
    }

    @Override
    public User findByEmail(String email) {
        return this.userRepository.findUserByEmail(email).get();
    }

    @Override
    public User findById(Long id) {
        return this.userRepository.findUserById(id).get();
    }

    @Override
    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }

    @Override
    public boolean deleteUser(Long id) {
        return this.userRepository.deleteUser(id);
    }

    @Override
    public void resetPassword(String email) {
        User u = this.userRepository.findUserByEmail(email).get();
        u.setPassword(this.encodePassword("HOLA_PARCE"));
        this.userRepository.save(u);
    }

    @Override
    public User saveUser(UserDto userDto, MultipartFile file) throws EmailExistException, UsernameExistsException {
        this.validateEmailAndUsername(userDto);
        User u = userDto.getId() != null ? this.userRepository
                .findUserById(userDto.getId()).get():
                this.mapUserData(userDto);
        if(userDto.getId() == null){
             this.userRepository.
                     findUserByUsername(userDto.getUserName());
            String encodedPass =
                    encodePassword(userDto.getPassword());
            u.setPassword(encodedPass);
            u.setEmail(userDto.getEmail());
        }
        if(file != null){
            String urlProfileImage =  this.getProfileImageUrl(u.getUsername(), file);
            u.setProfileImage(urlProfileImage);
        }
        u = this.userRepository.save(u).get();
        LOGGER.info(Instant.now().toString(), "User has been created");
        return u;
    }
}
