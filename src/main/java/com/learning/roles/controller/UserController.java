package com.learning.roles.controller;

import com.learning.roles.constant.FileConstant;
import com.learning.roles.constant.SecurityConstant;
import com.learning.roles.domain.User;
import com.learning.roles.domain.UserPrincipal;
import com.learning.roles.domain.exception.EmailExistException;
import com.learning.roles.domain.exception.UserNotFoundException;
import com.learning.roles.domain.exception.UsernameExistsException;
import com.learning.roles.dto.HttpResponse;
import com.learning.roles.dto.LoginDto;
import com.learning.roles.dto.UpdateImageDto;
import com.learning.roles.dto.UserDto;
import com.learning.roles.service.UserService;
import com.learning.roles.utility.JWTTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import java.io.IOException;

@RestController
@RequestMapping(path = {"/user"})
public class UserController extends ExceptionHandling {

    private final UserService userService;
    private final JWTTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService,
                          JWTTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager)
    {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserDto userSignUpDto) throws
            UserNotFoundException, EmailExistException, UsernameExistsException {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.registerUser(userSignUpDto)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginDto> login(@RequestBody UserDto userSignUpDto) throws
            UserNotFoundException, EmailExistException, UsernameExistsException {
        authenticate(userSignUpDto.getEmail(), userSignUpDto.getPassword());
        User user = this.userService.findByEmail(userSignUpDto.getEmail());
        UserPrincipal userPrincipal = new UserPrincipal(user);
        return new ResponseEntity(LoginDto.builder().userDto(user).token(getJwtHeader(userPrincipal)).build(), HttpStatus.OK);

    }

    @PreAuthorize("hasAnyAuthority('user:create')")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<User> addUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("userName") String userName,
            @RequestParam("role") String role,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNotLocked") String isNotLocked,
            @RequestPart(value = "profileImage", required = false)
                    MultipartFile file
    ) throws EmailExistException, UsernameExistsException,
            IOException {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.saveUser(
                        UserDto.builder()
                                .firstName(firstName)
                                .lastName(lastName)
                                .userName(userName)
                                .password(password)
                                .email(email)
                                .isActive(Boolean.parseBoolean(isActive))
                                .isNotLocked(Boolean.parseBoolean((isNotLocked)))
                                .role(role).build(), file)
        );
    }

    @PreAuthorize("hasAnyAuthority('user:update')")
    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            value = {"/{id}"})
    public ResponseEntity<User> updateUser(
            @PathVariable("id") Long id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("userName") String userName,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNotLocked") String isNotLocked,
            @RequestParam("role") String role,
            @RequestPart(value = "profileImage", required = false)
                    MultipartFile file
    ) throws EmailExistException, UsernameExistsException{
        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.saveUser(
                        UserDto.builder()
                                .id(id)
                                .firstName(firstName)
                                .lastName(lastName)
                                .userName(userName)
                                .isActive(Boolean.parseBoolean(isActive))
                                .isNotLocked(Boolean.parseBoolean((isNotLocked)))
                                .role(role).build(), file)
        );
    }


    @PreAuthorize("hasAnyAuthority('user:read')")
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username){
        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.findByUsername(username)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id){
        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.findById(id)
        );
    }

    @PreAuthorize("hasAnyAuthority('user:read')")
    @GetMapping
    public ResponseEntity<List<User>> getUsers(@RequestParam Long limit, @RequestParam Long page){
        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.getUsers(limit, page)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(
            @PathVariable("id") long id){
        this.userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                new HttpResponse(
                        HttpStatus.NO_CONTENT.value(),
                        HttpStatus.NO_CONTENT,
                        HttpStatus.NO_CONTENT.getReasonPhrase(),
                        "User has been deleted successfully"
                )
        );
    }

    @PostMapping("/reset-password/{email}")
    public ResponseEntity<HttpResponse> resetPassword(
            @PathVariable("email") String email){
        this.userService.resetPassword(email);
        return ResponseEntity.status(HttpStatus.OK).body(
                new HttpResponse(
                        HttpStatus.OK.value(),
                        HttpStatus.OK,
                        HttpStatus.OK.getReasonPhrase(),
                        "Email has been sent to: " +
                                email
                ));
    }

    @PostMapping("/{id}/update-profile-image")
    public ResponseEntity updateProfileImage(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file){
        return ResponseEntity.status(HttpStatus.OK).body(
                new UpdateImageDto(
                        this.userService.updateProfileImage(id, file)
                )
              );
    }

    @GetMapping(path = "/image/{username}/{filename}",
            produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(
            @PathVariable("username") String username,
            @PathVariable("filename") String filename)
            throws IOException {
        return Files.readAllBytes(
                Paths.get(
                        FileConstant.USER_FOLDER +
                                username +
                                FileConstant.FORWARD_SLASH +
                                filename));
    }

    @GetMapping(path = "/image/temp/{username}",
            produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getTempImage(
            @PathVariable("username") String username) throws IOException {
        URL url = new URL(FileConstant.TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
        try{
            InputStream inputStream = url.openStream();
            int bytesRead;
            byte[] chunk = new byte[1024];
            while ((bytesRead = inputStream.read(chunk)) > 0){
                byteArrayInputStream.write(chunk, 0, bytesRead);
            }
        }catch (Exception e){

        }
        return byteArrayInputStream.toByteArray();
    }


    private String getJwtHeader(UserPrincipal userPrincipal){
        return this.jwtTokenProvider.
                        generateJwtToken(userPrincipal);
    }

    private void authenticate(String email, String password) {
        this.authenticationManager.authenticate(new
                UsernamePasswordAuthenticationToken(email, password));
    }


}
