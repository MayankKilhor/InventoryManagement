package com.imspos.auth_service.controller;

import com.imspos.auth_service.exceptions.BadRequestException;
import com.imspos.auth_service.exceptions.DatabaseException;
import com.imspos.auth_service.model.User;
import com.imspos.auth_service.payload.request.ChangePasswordRequest;
import com.imspos.auth_service.payload.request.LoginRequest;
import com.imspos.auth_service.payload.request.SignUpRequest;
import com.imspos.auth_service.payload.request.UpdateDetailsRequest;
import com.imspos.auth_service.payload.response.ApiErrorResponse;
import com.imspos.auth_service.payload.response.ApiResponse;
import com.imspos.auth_service.repository.UserRepository;
import com.imspos.auth_service.security.JWT.JwtUtil;
import com.imspos.auth_service.security.UserPrincipal;
import com.imspos.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;




    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Auth Service";
    }


    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest, HttpServletRequest httpServletRequest){
        try{
            User user = authService.signUp(signUpRequest);
            String jwt = jwtUtil.generateToken(user.getUsername(), user.getUserId(), user.getUserRole().getRoleId());

            ApiResponse apiResponse = new ApiResponse(true,"Successfully SignUp the user","/");

            apiResponse.addDetail("userId",user.getUserId());
            apiResponse.addDetail("jwt", jwt);
            apiResponse.addDetail("username",user.getUsername());
            apiResponse.addDetail("name",user.getName());
            apiResponse.addDetail("userrole",user.getUserRole().getRoleId());
            apiResponse.addDetail("location",user.getLocation());
            apiResponse.addDetail("phone_no",user.getPhoneNo());
            apiResponse.addDetail("imageUrl",user.getImageUrl());
            apiResponse.addDetail("designation",user.getDesignation());


            logger.info("Successfully Registered the user, userId= "+user.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        }catch (BadRequestException be){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to SignUp User!");
            errorResponse.addDetail("error",be.getMessage());
            logger.error("Failed to SignUp User, Error:- "+be.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }catch(DatabaseException de) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to SignUp User!");
            errorResponse.addDetail("error",de.getMessage());
            logger.error("Failed to SignUp User, Error:- "+de.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }catch (Exception e) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(false, "Failed to SignUp User!");
            errorResponse.addDetail("error", e.getMessage());
            logger.error("Failed to SignUp User, Error:- "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            String jwtResponse = authService.verify(loginRequest);
            User user = userRepository.findByUsername(loginRequest.getEmail());


            ApiResponse apiResponse = new ApiResponse(true, "Successfully Logged In the user", "/");
            apiResponse.addDetail("jwt", jwtResponse);
            apiResponse.addDetail("username",loginRequest.getEmail());
            apiResponse.addDetail("name",user.getName());
            apiResponse.addDetail("userrole",user.getUserRole().getRoleId());
            apiResponse.addDetail("location",user.getLocation());
            apiResponse.addDetail("phone_no",user.getPhoneNo());
            apiResponse.addDetail("imageUrl",user.getImageUrl());
            apiResponse.addDetail("designation",user.getDesignation());

            logger.info("Successfully Logged In the user, email= " + loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        }catch (BadRequestException be){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to Logged In the user!");
            errorResponse.addDetail("error",be.getMessage());



            logger.error("Failed to Logged In the user, Error:- "+be.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }catch(Exception e){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to Logged In the user!");
            errorResponse.addDetail("error",e.getMessage());


            logger.error("Failed to Logged In the user, Error:- "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }


    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest, Principal principal) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
            User user = userPrincipal.getUser();

            authService.changePassword(user,changePasswordRequest.getOld_password(),changePasswordRequest.getNew_password(),changePasswordRequest.getConfirm_password());


            ApiResponse apiResponse = new ApiResponse(true, "Successfully Changed the Password", "/");

            apiResponse.addDetail("username",user.getUsername());
            apiResponse.addDetail("name",user.getName());
            apiResponse.addDetail("userrole",user.getUserRole().getRoleId());
            apiResponse.addDetail("phone_no",user.getPhoneNo());
            apiResponse.addDetail("location",user.getLocation());
            apiResponse.addDetail("imageUrl",user.getImageUrl());
            apiResponse.addDetail("designation",user.getDesignation());

            logger.info("Successfully Changed the password, email= " + user.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        }catch(BadRequestException be){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to change the password!");
            errorResponse.addDetail("error",be.getMessage());

            logger.error("Failed to change the password, Error:- "+be.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }catch(DatabaseException de){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to change the password!");
            errorResponse.addDetail("error",de.getMessage());

            logger.error("Failed to change the password, Error:- "+de.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }catch(Exception e){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to change the password!");
            errorResponse.addDetail("error",e.getMessage());

            logger.error("Failed to change the password, Error:- "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    @PostMapping("/updatedetails")
    public  ResponseEntity<?> updateDetails(@Valid @RequestBody UpdateDetailsRequest updateDetailsRequest, Principal principal, HttpServletRequest request) throws  Exception {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
            User user = userPrincipal.getUser();

            user.setDesignation(updateDetailsRequest.getDesignation());
            user.setPhoneNo(updateDetailsRequest.getPhoneNo());
            user.setLocation(updateDetailsRequest.getLocation());
            user.setName(updateDetailsRequest.getName());

            userRepository.save(user);

            ApiResponse apiResponse = new ApiResponse(true, "Successfully Updated the User Information", "/");

            apiResponse.addDetail("username",user.getUsername());
            apiResponse.addDetail("name",user.getName());
            apiResponse.addDetail("userrole",user.getUserRole().getRoleId());
            apiResponse.addDetail("phone_no",user.getPhoneNo());
            apiResponse.addDetail("location",user.getLocation());
            apiResponse.addDetail("imageUrl",user.getImageUrl());
            apiResponse.addDetail("designation",user.getDesignation());

            logger.info("Successfully Updated the Profile Image, email= " + user.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        }catch(BadRequestException be){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to Update the profile image!");
            errorResponse.addDetail("error",be.getMessage());

            logger.error("Failed to update the profile image, Error:- "+be.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }catch(Exception e){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to Update the profile image!");
            errorResponse.addDetail("error",e.getMessage());

            logger.error("Failed to update the profile image, Error:- "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


}
