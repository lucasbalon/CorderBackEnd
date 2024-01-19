package be.technobel.corder.bl.impl;

import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.User;
import be.technobel.corder.dl.models.enums.Role;
import be.technobel.corder.dl.models.enums.Status;
import be.technobel.corder.dl.repositories.UserRepository;
import be.technobel.corder.pl.config.exceptions.DuplicateUserException;
import be.technobel.corder.pl.config.exceptions.InvalidPasswordException;
import be.technobel.corder.pl.config.security.JWTProvider;
import be.technobel.corder.pl.models.dtos.AuthDTO;
import be.technobel.corder.pl.models.forms.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    UserForm userForm;
    LoginForm loginForm;
    PasswordChangeForm passwordChangeForm;

    @BeforeEach
    public void setUp() {
        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.ADMIN);
        userForm = new UserForm(
                "test",
                "Test1234=",
                roles
        );
        loginForm = new LoginForm(
                "test",
                "Test1234="
        );
        passwordChangeForm = new PasswordChangeForm(
                "test",
                "Test1234=",
                "1234=Test"
        );

    }

    @Test
    void register_withExistingLogin() {
        //Arrange
        when(userRepository.existsByLogin(userForm.login())).thenReturn(true);

        //Act & Assert
        assertThrows(DuplicateUserException.class, () -> {
            userService.register(userForm);
        });
    }

    @Test
    void login_withNonExistingUser() {
        //Arrange
        when(userRepository.findByLogin(loginForm.login())).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.login(loginForm);
        });
    }

    @Test
    void changePassword_withValidPasswordChangeForm() {
        //Arrange
        User user = new User(); //build or mock a User
        when(userRepository.findByLogin(passwordChangeForm.user())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(passwordChangeForm.oldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(passwordChangeForm.newPassword())).thenReturn("newEncodedPassword");

        //Act
        userService.changePassword(passwordChangeForm);

        //Assert
        verify(userRepository, times(1)).save(user);

    }
    @Test
    void changePassword_withInvalidPassword() {
        //Arrange
        User user = new User(); //build or mock a User
        when(userRepository.findByLogin(passwordChangeForm.user())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(passwordChangeForm.oldPassword(), user.getPassword())).thenReturn(false);

        //Act & Assert
        assertThrows(InvalidPasswordException.class, () -> {
            userService.changePassword(passwordChangeForm);
        });
    }

    @Test
    void changePassword_withNonExistingUser() {
        //Arrange
        when(userRepository.findByLogin(passwordChangeForm.user())).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.changePassword(passwordChangeForm);
        });
    }
}