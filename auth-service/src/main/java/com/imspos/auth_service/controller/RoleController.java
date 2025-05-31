package com.imspos.auth_service.controller;

import com.imspos.auth_service.model.Authority;
import com.imspos.auth_service.model.User;
import com.imspos.auth_service.model.UserRole;
import com.imspos.auth_service.payload.response.ApiAccessControlResult;
import com.imspos.auth_service.payload.response.ApiErrorResponse;
import com.imspos.auth_service.payload.response.ApiResponse;
import com.imspos.auth_service.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth/authorities")
public class RoleController {

    @Autowired
    private RoleService roleService;

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @PostMapping("/defaultConfiguration")
    public ResponseEntity<?> defaultConfiguration() {
        try {
            Map<String, Object> adminUserDetails = new HashMap<>();
            Map<String, Object> userRoleDetails = new HashMap<>();
            Map<String, Object> defaultAuthorityDetails = new HashMap<>();
            Map<String, Object> apiAccessControlDetails = new HashMap<>();

            try {
                User adminUser = roleService.createAdminUser();
                adminUserDetails.put("success",true);
                adminUserDetails.put("name",adminUser.getName());
                adminUserDetails.put("userName",adminUser.getUsername());
                adminUserDetails.put("userId",adminUser.getUserId());
                adminUserDetails.put("userRole",adminUser.getUserRole());
                adminUserDetails.put("created_at",adminUser.getCreatedAt());
                adminUserDetails.put("updated_at",adminUser.getUpdatedAt());
                logger.info("Successfully created Admin User with userId:- "+adminUser.getUserId());
                try{
                    UserRole userRole = roleService.createUserRole(adminUser);
                    userRoleDetails.put("success", true);
                    userRoleDetails.put("roleId", userRole.getRoleId());
                    userRoleDetails.put("name", userRole.getName());
                    userRoleDetails.put("createdBy", userRole.getCreatedBy().getUsername());
                    userRoleDetails.put("description", userRole.getDescription());
                    userRoleDetails.put("authorities", userRole.getAuthorities());
                    userRoleDetails.put("createdAt", userRole.getCreated_at());
                    userRoleDetails.put("updatedAt", userRole.getUpdated_at());
                }catch(Exception e){
                    userRoleDetails.put("success", false);
                    userRoleDetails.put("error","Not able to create USER Role, Error:- "+e.getMessage());
                }
                try{
                    Set<Authority> authorities =  roleService.defaultAuthority(adminUser);
                    defaultAuthorityDetails.put("success", true);
                    defaultAuthorityDetails.put("authorities",authorities);
                }catch(Exception e){
                    defaultAuthorityDetails.put("success",false);
                    defaultAuthorityDetails.put("error","Not able to create Default Authorities, Error:- "+e.getMessage());
                }
                try{
                    ApiAccessControlResult result= roleService.defaultApiAccessControl(adminUser);
                    if(result.getErrors().size()>0){
                        apiAccessControlDetails.put("errors",result.getErrors());
                    }
                    apiAccessControlDetails.put("accessControl",result.getSuccessfulApiAccessControls());
                }catch(Exception e){
                    apiAccessControlDetails.put("success",false);
                    apiAccessControlDetails.put("error","Not able to create Api AccessControl, Error:- "+e.getMessage());
                }


            }catch(Exception e){
                logger.error("Failed to create Admin User, Error:- "+e.getMessage());
                adminUserDetails.put("success", false);
                adminUserDetails.put("error", "Failed to create Admin User, Error:- "+e.getMessage());
                userRoleDetails.put("success", false);
                userRoleDetails.put("error","Admin Role wasn't created!");
                defaultAuthorityDetails.put("success",false);
                defaultAuthorityDetails.put("error","Admin role wasn't created!");
                apiAccessControlDetails.put("succcess",false);
                apiAccessControlDetails.put("error","Admin role wasn't created!");

                ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to initialize Default Configuration!");
                errorResponse.addDetail("admin",adminUserDetails);
                errorResponse.addDetail("userRole",userRoleDetails);
                errorResponse.addDetail("defaultAuthority", defaultAuthorityDetails);
                errorResponse.addDetail("apiAccessControl", apiAccessControlDetails);

                logger.error("Failed to initialize Default Configuration, Error:- "+e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

            ApiResponse apiResponse = new ApiResponse(true,"Successfully initialized Default Configuration!","/");
            apiResponse.addDetail("adminDetails",adminUserDetails);
            apiResponse.addDetail("userRoleDetails",userRoleDetails);
            apiResponse.addDetail("defaultAuthorityDetails", defaultAuthorityDetails);
            apiResponse.addDetail("apiAccessControlDetails", apiAccessControlDetails);

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        }catch(Exception e){
            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to initialize Default Configuration!");
            errorResponse.addDetail("error",e.getMessage());
            logger.error("Failed to initialize Default Configuration, Error:- "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
