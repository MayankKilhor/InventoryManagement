package com.imspos.auth_service.service;

import com.imspos.auth_service.model.ApiAccessControl;
import com.imspos.auth_service.model.User;
import com.imspos.auth_service.payload.request.LoginRequest;
import com.imspos.auth_service.payload.request.SignUpRequest;
import com.imspos.auth_service.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;

public interface AuthService {

    /**
     * Signs up a new user based on the given sign-up request.
     * @param signUpRequest The sign-up request containing user details.
     * @return The created User object.
     */
    User signUp(SignUpRequest signUpRequest);

    /**
     * Authenticates a user and returns a JWT token if successful.
     * @param loginRequest The login request containing email and password.
     * @return A JWT token as a string.
     */
    String verify(LoginRequest loginRequest);

    /**
     * Changes the password of a given user.
     * @param user The user whose password needs to be changed.
     * @param oldPassword The current password.
     * @param newPassword The new password.
     * @param confirmPassword Confirmation of the new password.
     */
    void changePassword(User user, String oldPassword, String newPassword, String confirmPassword);

    /**
     * Loads the authenticated user as UserPrincipal by username.
     * @param username The username (email) of the user.
     * @return UserPrincipal representation of the user.
     */
    UserPrincipal loadUserByUsername(String username);
}