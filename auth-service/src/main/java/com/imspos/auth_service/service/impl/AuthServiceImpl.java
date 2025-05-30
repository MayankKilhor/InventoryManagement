package com.imspos.auth_service.service.impl;


import com.imspos.auth_service.exceptions.BadRequestException;
import com.imspos.auth_service.exceptions.DatabaseException;
import com.imspos.auth_service.model.OTP;
import com.imspos.auth_service.model.User;
import com.imspos.auth_service.model.UserRole;
import com.imspos.auth_service.repository.OTPRepository;
import com.imspos.auth_service.repository.UserRepository;
import com.imspos.auth_service.repository.UserRoleRepository;
import com.imspos.auth_service.security.JWT.JwtUtil;
import com.imspos.auth_service.security.UserPrincipal;
import com.imspos.auth_service.utils.RandomNumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthServiceImpl implements UserDetailsService {

    @Autowired
    private RandomNumberUtils randomNumberUtils;

    @Autowired
    private OTPRepository otpRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JavaMailSender emailSender;


    @Autowired
    private @Lazy AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;


    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public String sendOtp(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@(dynasas\\.com|spaceage-in\\.com)$")) {
            throw new IllegalArgumentException("Email must belong to @dynasas.com or @spaceage-in.com domain");
        }
        try {
            String otp = randomNumberUtils.generateRandomDigits(6);
            OTP otpEntity = new OTP();
            otpEntity.setEmail(email);
            otpEntity.setOtp(otp);
            otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(50)); // OTP expires in 5 minutes

            try {
                otpRepository.save(otpEntity);
            } catch (Exception e) {
                throw new DatabaseException("Failed to register on OTP table:- " + e.getMessage());

            }

            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your Signup OTP Code");
            message.setText("Your OTP code is " + otp);
            emailSender.send(message);
            return otp;
        }catch(DatabaseException de){
            throw de;
        }catch (Exception e){
            throw e;
        }
    }

    public void verifyOtp(String email, String otp) {
        try {
            Optional<OTP> otpEntity = otpRepository.findByEmail(email);
            if (otpEntity.isPresent()){
                if( otpEntity.get().getOtp().equals(otp)){
                    if(otpEntity.get().getExpiryTime().isAfter(LocalDateTime.now())) {

                        try {
                            otpEntity.get().setVerified(true);
                            otpRepository.save(otpEntity.get());
                        } catch (Exception e) {
                            throw new DatabaseException("Failed to update verified on otp table:- " + e.getMessage());

                        }
                    }else{
                        throw new BadRequestException("Expired OTP verified from email( "+ email + " )");
                    }
                }else{
                    throw new BadRequestException("Invalid OTP verified from email( "+ email + " )");
                }
            }else{
                throw new BadRequestException("Wrong email address been given,User doesn't exist in Database");
            }
        }catch(BadRequestException be){
            throw be;
        }catch(DatabaseException de){
            throw de;
        }catch(Exception e){
            throw e;
        }
    }

    public User signUp(SignUpRequest signUpRequest){
        try {
            Optional<OTP> otpEntity = otpRepository.findByEmail(signUpRequest.getEmail());
            if (otpEntity.isPresent()){
                if(otpEntity.get().getVerified()) {
                    String prefix = "USER";
                    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String randomDigits = randomNumberUtils.generateRandomDigits(6);
                    String user_id = prefix + "_" + date + "_" + randomDigits;

                    UserRole userRole = userRoleRepository.findById("ROLE_USER").orElseThrow(() -> new IllegalArgumentException("Invalid role id"));
                    User user = new User();
                    user.setUserId(user_id);
                    user.setUsername(signUpRequest.getEmail());
                    user.setName(signUpRequest.getName());
                    user.setPhoneNo(signUpRequest.getPhoneNo());
                    user.setImageUrl(cdnUrl+defaultProfile);
                    if (!signUpRequest.getPassword().equals(signUpRequest.getConfirm_password())) {
                        throw new BadRequestException("Password and Confirm Password not Matching");
                    }
                    user.setPassword_hash(encoder.encode(signUpRequest.getPassword()));
                    user.setUserRole(userRole);
                    user.setCreatedAt(new Date());
                    user.setUpdatedAt(new Date());
                    try {
                        userRepository.save(user);
                    } catch (Exception e) {
                        throw new DatabaseException("Failed to Signup user on user table:- " + e.getMessage());

                    }
                    return user;
                }else{
                    throw new BadRequestException("User is not verified!");
                }
            }else{
                throw new BadRequestException("Wrong email address been given,User doesn't exist in Database");
            }
        }catch(BadRequestException be) {
            throw be;
        }catch(DatabaseException de){
            throw de;
        }catch(Exception e) {
            throw new DatabaseException("Failed to signUp User" + e.getMessage());
        }
    }

    public String verify(LoginRequest loginRequest){
        try {
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            if (authentication.isAuthenticated()) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                return jwtUtil.generateToken(loginRequest.getEmail(), userPrincipal.getUser().getUserId(), userPrincipal.getUser().getUserRole().getRoleId());
            }else {
                throw new BadRequestException("User failed Authentication!");
            }
        }catch(BadRequestException be){
            throw be;
        }catch(Exception e){
            throw new BadRequestException(e.getMessage());
        }

    }


    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user == null){
            logger.error("User not found: " + username);
            // You can throw a custom exception or add more details here
            throw new UsernameNotFoundException("User '" + username + "' not found");
        }
        return new UserPrincipal(user);
    }

    public void changePassword(User user, String oldPassword, String newPassword, String confirmPassword) {
        try{
//            String oldPasswordHash =  encoder.encode(oldPassword);
            String newPasswordHash = encoder.encode(newPassword);
            if (!encoder.matches(oldPassword, user.getPassword_hash())) {
                throw new BadRequestException("User failed authentication!");
            }
            if(!newPassword.equals(confirmPassword)){
                throw new BadRequestException("New Password and Confirm Password not Matching");
            }
            user.setPassword_hash(newPasswordHash);
            try {
                userRepository.save(user);
            } catch (Exception e) {
                throw new DatabaseException(e.getMessage());
            }

        }catch (BadRequestException be){
            throw be;
        }catch (DatabaseException de){
            throw de;
        } catch (Exception e) {
            throw e;
        }
    }

    public String uploadImage(String userId, MultipartFile file) throws IOException {
        try{
            String key = "users/" + userId + "/" + "profilePic";

            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("File is not a valid image");
            }


            File tempFile = csvService.convertMultipartFileToFile(file);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());

            PutObjectRequest request = new PutObjectRequest(bucketName, key, new FileInputStream(tempFile), metadata);

// Upload
            amazonS3.putObject(request);

            return key;

        }catch(BadRequestException be){
            throw be;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
