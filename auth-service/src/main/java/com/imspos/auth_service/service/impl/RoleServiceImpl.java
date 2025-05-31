package com.imspos.auth_service.service.impl;

import com.imspos.auth_service.exceptions.*;
import com.imspos.auth_service.model.*;
import com.imspos.auth_service.payload.request.role.*;
import com.imspos.auth_service.payload.response.ApiAccessControlResult;
import com.imspos.auth_service.repository.*;

import com.imspos.auth_service.service.RoleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private ApiAccessControlRepository apiAccessControlRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    public User createAdminUser() {
        // Check if GOD role already exists
        try {
            Optional<UserRole> adminRoleOptional = userRoleRepository.findById("ROLE_ADMIN");

            UserRole adminRole;
            if (!adminRoleOptional.isPresent()) {
                // Create GOD role with full permissions
                adminRole = new UserRole();
                adminRole.setRoleId("ROLE_ADMIN");
                adminRole.setName("Admin");
                adminRole.setDescription("Full access to manage users, roles, and permissions");
                Date creationDate = new Date();
                adminRole.setCreated_at(creationDate);
                adminRole.setUpdated_at(creationDate);


                userRoleRepository.save(adminRole);
            } else {
                adminRole = adminRoleOptional.get();
            }

            // Check if GOD user already exists
            Optional<User> adminUserOptional = Optional.ofNullable(userRepository.findByUsername("admin@dynasas"));

            User adminUser;
            if (!adminUserOptional.isPresent()) {
                // Create GOD user
                adminUser = new User();
                adminUser.setUserId("ADMIN");
                adminUser.setName("Admin");
                adminUser.setUsername("admin@imspos");
                adminUser.setPassword_hash(encoder.encode("password")); // This should be a securely hashed password
                adminUser.setPhoneNo("0000000000");
                adminUser.setUserRole(adminRole);
                Date creationUserDate =new Date();
                adminUser.setCreatedAt(creationUserDate);
                adminUser.setUpdatedAt(creationUserDate);

                userRepository.save(adminUser);
            } else {
                adminUser = adminUserOptional.get();
            }

            return adminUser;
        }catch(Exception e){

            throw new RuntimeException("Not able to create Admin User, Error:- "+e.getMessage());
        }
    }

    private Authority createOrGetAuthority(User user, String authorityName) {
        try {
            Optional<Authority> authorityOptional = authorityRepository.findByName(authorityName);

            Authority authority;
            if (!authorityOptional.isPresent()) {
                authority = new Authority();
                authority.setAuthorityId("ID_"+authorityName);
                authority.setName(authorityName);
                authority.setDescription("Permission to " + authorityName);
                authority.setCreated_at(new Date());
                authority.setCreatedBy(user);
                authority.setUpdated_at(new Date());
                authorityRepository.save(authority);
            } else {
                authority = authorityOptional.get();
            }

            return authority;
        }catch (Exception e){
            logger.error("not able to create Authorities, error:- "+e.getMessage());
            return null;
        }
    }
    public ApiAccessControlResult defaultApiAccessControl(User adminUser) {
        try {
            // Fetch user role
            UserRole role = userRoleRepository.findById(adminUser.getUserRole().getRoleId())
                    .orElseThrow(() -> new RuntimeException("UserRole not found"));

            Set<ApiAccessControl> apiAccessControls = new HashSet<>();
            List<String> authoritiesErrors = new ArrayList<>();

            // List of endpoints to create
            List<String> publicEndpoints = Arrays.asList(
                    "/auth/signUp",
                    "/auth/login"
            );
            Map<String, Set<String>> authoritiesEndpoints = new HashMap<>();
            authoritiesEndpoints.put("/api/authorities/createRole", new HashSet<>(Arrays.asList("ADD_ROLE","FULL_ACCESS")));
            authoritiesEndpoints.put("/api/authorities/assignAuthority", new HashSet<>(Arrays.asList( "ASSIGN_AUTHORITY","FULL_ACCESS")));
            authoritiesEndpoints.put("/api/authorities/removeAuthority", new HashSet<>(Arrays.asList("REMOVE_AUTHORITY","FULL_ACCESS")));
            authoritiesEndpoints.put("/api/authorities/createAuthority", new HashSet<>(Arrays.asList( "ADD_AUTHORITY","FULL_ACCESS")));



            // Iterate through endpoints and attempt to create ApiAccessControl for each
            for (String endpoint : publicEndpoints) {
                try {
                    apiAccessControls.add(createApiAccessControl(endpoint, "POST", new HashSet<>(), true));
                } catch (Exception e) {
                    authoritiesErrors.add("Error creating access control for endpoint: " + endpoint + " - " + e.getMessage());
                }
            }
            for (Map.Entry<String, Set<String>> entry : authoritiesEndpoints.entrySet()) {
                String endpoint = entry.getKey();
                Set<String> authorityNames = entry.getValue();
                try {

                    apiAccessControls.add(createApiAccessControl(endpoint, "POST", authorityNames, false));
                } catch (Exception e) {
                    authoritiesErrors.add("Error creating access control for endpoint: " + endpoint + " - " + e.getMessage());
                }
            }

            // Return both successful ApiAccessControl and errors in the response object
            return new ApiAccessControlResult(apiAccessControls, authoritiesErrors);

        } catch (Exception e) {
            throw e;  // Rethrow the exception if any general error occurs
        }
    }

    public ApiAccessControl createApiAccessControl(String endpointPath, String httpMethod, Set<String> authorityNames, Boolean isPublic) {
        Optional<ApiAccessControl> existingAccessControl = apiAccessControlRepository.findByEndpointPath(endpointPath);
        ApiAccessControl apiAccessControl;
        if (existingAccessControl.isPresent()) {
            // If the endpoint already exists, update it
            apiAccessControl = existingAccessControl.get();
            apiAccessControl.setPublic(isPublic);  // Update public status
            // Clear existing authorities and update with new ones
            apiAccessControl.getAuthorities().clear();
        } else {
            // Create a new ApiAccessControl if it doesn't exist
            apiAccessControl = new ApiAccessControl();
            apiAccessControl.setEndpointPath(endpointPath);
            apiAccessControl.setPublic(isPublic);
        }
        if(!authorityNames.isEmpty()) {
            // Fetch authorities by their names and assign them to ApiAccessControl
            Set<Authority> authorities = new HashSet<>();
            for (String authorityName : authorityNames) {
                Authority authority = authorityRepository.findByName(authorityName).orElseThrow(() ->
                        new RuntimeException("Authority not found: " + authorityName));
                authorities.add(authority);
            }
            apiAccessControl.setAuthorities(authorities);
        }

        // Save the ApiAccessControl entity (either created or updated) to the database
        return apiAccessControlRepository.save(apiAccessControl);
    }

    public UserRole createUserRole(User user) {
        try {
            Optional<UserRole> userRoleOptional = userRoleRepository.findByName("USER");
            UserRole userRole;
            if (!userRoleOptional.isPresent()) {
                userRole = new UserRole();
                userRole.setRoleId("ROLE_USER");
                userRole.setName("User");
                userRole.setDescription("A Basic User");
                userRole.setCreatedBy(user);
                userRole.setAuthorities(new HashSet<>());
                userRole.setCreated_at(new Date());
                userRole.setUpdated_at(new Date());
                try {
                    userRoleRepository.save(userRole);
                } catch (Exception e) {
                    throw new DatabaseException("Unable to save USER Role in user_role table, Error= " + e.getMessage());
                }
            } else {
                logger.info("USER Role  was already present user_role table!");
                userRole = userRoleOptional.get();
            }
            return userRole;
        }catch (DatabaseException de){
            throw de;
        }catch(Exception e){
            throw e;
        }

    }

    public UserRole createRole(User creatorUser, CreateRoleRequest createRoleRequest) {
        try {
            Optional<UserRole> userRoleOptional = userRoleRepository.findById(createRoleRequest.getRoleId());
            UserRole newRole;
            if (!userRoleOptional.isPresent()) {
                newRole = new UserRole();
                newRole.setRoleId(createRoleRequest.getRoleId());
                newRole.setName(createRoleRequest.getName());
                newRole.setDescription(createRoleRequest.getDescription());
                newRole.setCreatedBy(creatorUser);
                newRole.setCreated_at(new Date());
                try {
                    userRoleRepository.save(newRole);
                } catch (Exception e) {
                    throw new DatabaseException("Unable to save "+ createRoleRequest.getRoleId()+" Role in user_role table, Error= " + e.getMessage());
                }
            } else {
                logger.info(createRoleRequest.getRoleId() + " Role  was already present in user_role table!");
                newRole = userRoleOptional.get();
            }
            return newRole;
        }catch (DatabaseException de){
            throw de;
        }catch(Exception e){
            throw e;
        }

    }

    public Authority createAuthority(User creatorUser, CreateAuthorityRequest createAuthorityRequest) {
        try {
            Optional<Authority> authorityOptional = authorityRepository.findById(createAuthorityRequest.getAuthorityId());
            Authority authority;
            if (!authorityOptional.isPresent()) {
                authority = new Authority();
                authority.setAuthorityId(createAuthorityRequest.getAuthorityId());
                authority.setName(createAuthorityRequest.getName());
                authority.setDescription(createAuthorityRequest.getDescription());
                authority.setCreatedBy(creatorUser);
                authority.setCreated_at(new Date());
                try {
                    authorityRepository.save(authority);
                } catch (Exception e) {
                    throw new DatabaseException("Unable to save "+ createAuthorityRequest.getAuthorityId() +" Authority in authorities table, Error= " + e.getMessage());
                }
            } else {
                logger.info(createAuthorityRequest.getAuthorityId()+ " Role  was already present in authorities table!");
                authority = authorityOptional.get();
            }
            return authority;
        }catch (DatabaseException de){
            throw de;
        }catch(Exception e){
            throw e;
        }
    }

    public UserRole assignAuthority(User creatorUser, AssignOrRemoveAuthorityRequest assignOrRemoveAuthorityRequest) {
        UserRole userRole = userRoleRepository.findById(assignOrRemoveAuthorityRequest.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Fetch Authorities by IDs
        Set<Authority> authorities = new HashSet<>();
        Set<Authority> currentAuthorities = userRole.getAuthorities();
        List<String> authoritiesErrors = new ArrayList<>();
        List<Authority> authoritiesToAdd = new ArrayList<>();
        int index = 0;
        for (String authorityId : assignOrRemoveAuthorityRequest.getAuthorityIds()) {
            try {
                Authority authorityToAdd = authorityRepository.findById(authorityId)
                        .orElseThrow(() -> new RuntimeException("Authority not found with id: " + authorityId));
                if (currentAuthorities.contains(authorityToAdd)) {
                    // Add an error if the authority is not assigned to the UserRole
                    authoritiesErrors.add("Authority with id: " + authorityId + "  at index "+index+" is already assigned to the Role( "+assignOrRemoveAuthorityRequest.getRoleId()+" )");
                } else {
                    authoritiesToAdd.add(authorityToAdd);
                }
                authorities.add(authorityToAdd);
            } catch (RuntimeException e) {
                // Add error to the list if authority not found
                authoritiesErrors.add("Invalid authorityId at index " + index + ": " + authorityId+" ,error:- "+e.getMessage());
            }
            index++;
        }

        if (!authoritiesErrors.isEmpty()) {
            // You can throw a custom exception with the list of errors
            throw new AuthoritiesValidationException(authoritiesErrors);
        }

        for(Authority authorityToAdd : authoritiesToAdd){
            currentAuthorities.add(authorityToAdd);
        }

        //TODO ADD updated_by also in UserRole
        userRole.setUpdated_at(new Date());
        // Assign authorities to the UserRole
        userRole.setAuthorities(authorities);


        return userRoleRepository.save(userRole);
    }

    public UserRole removeAuthority(User creatorUser, AssignOrRemoveAuthorityRequest assignOrRemoveAuthorityRequest) {
        // Fetch the UserRole by ID
        UserRole userRole = userRoleRepository.findById(assignOrRemoveAuthorityRequest.getRoleId())
                .orElseThrow(() -> new RuntimeException("UserRole not found"));

        // Fetch current authorities of the UserRole
        Set<Authority> currentAuthorities = userRole.getAuthorities();

        List<String> authoritiesErrors = new ArrayList<>();
        int index = 0;

        List<Authority> authoritiesToRemove = new ArrayList<>();
        // Check if any authorities need to be removed
        for (String authorityId : assignOrRemoveAuthorityRequest.getAuthorityIds()) {
            try {
                Authority authorityToRemove = authorityRepository.findById(authorityId)
                        .orElseThrow(() -> new RuntimeException("Authority not found with id: " + authorityId));

                // Check if the authority is assigned to the UserRole
                if (currentAuthorities.contains(authorityToRemove)) {
                    authoritiesToRemove.add(authorityToRemove);
                } else {
                    // Add an error if the authority is not assigned to the UserRole
                    authoritiesErrors.add("Authority with id: " + authorityId + " at index "+index+" is not assigned to the Role( "+assignOrRemoveAuthorityRequest.getRoleId()+" )");
                }
            } catch (RuntimeException e) {
                // Add error to the list if authority not found
                authoritiesErrors.add("Invalid authorityId at index " + index + ": " + authorityId+" ,error:- "+e.getMessage());
            }
            index++;

        }
        if (!authoritiesErrors.isEmpty()) {
            // You can throw a custom exception with the list of errors
            throw new AuthoritiesValidationException(authoritiesErrors);
        }

        for(Authority authorityToRemove : authoritiesToRemove){
            currentAuthorities.remove(authorityToRemove);
        }

        if (!authoritiesErrors.isEmpty()) {
            // You can throw a custom exception with the list of errors
            throw new AuthoritiesValidationException(authoritiesErrors);
        }


        // Update the timestamp and save the updated UserRole
        userRole.setUpdated_at(new Date());
        userRole.setAuthorities(currentAuthorities);

        // Save the updated UserRole
        return userRoleRepository.save(userRole);


    }

    public Set<Authority> defaultAuthority(User adminUser) {
        try {
            UserRole role = userRoleRepository.findById(adminUser.getUserRole().getRoleId())
                    .orElseThrow(() -> new RuntimeException("UserRole not found"));

            Set<Authority> authorities = new HashSet<>();
            authorities.add(createOrGetAuthority(adminUser, "ADD_ROLE"));
            authorities.add(createOrGetAuthority(adminUser, "ADD_AUTHORITY"));
            authorities.add(createOrGetAuthority(adminUser, "CHANGE_ROLE"));
            authorities.add(createOrGetAuthority(adminUser, "ASSIGN_AUTHORITY"));
            authorities.add(createOrGetAuthority(adminUser, "REMOVE_AUTHORITY"));
            authorities.add(createOrGetAuthority(adminUser, "BOM_UPLOAD"));
            authorities.add(createOrGetAuthority(adminUser, "BOM_GET"));
            authorities.add(createOrGetAuthority(adminUser, "QUALITY_CHECK"));
            authorities.add(createOrGetAuthority(adminUser, "FULL_ACCESS"));
            role.setAuthorities(authorities);
            userRoleRepository.save(role);
            return authorities;
        }catch(Exception e){
            throw e;
        }
    }

    public Set<Authority> getAuthority(String username) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        Set<Authority> authorities ;
        if(userOptional.isPresent()){
            authorities = userOptional.get().getUserRole().getAuthorities();
        }else{
            throw new BadRequestException("Not able to fetch User details of username( "+username+" ), Recheck the value!");
        }
        return authorities;
    }

    public List<Authority> getAllAuthority() {
        List<Authority> authorities =authorityRepository.findAll();
        return authorities;
    }

    public UserRole getRole(String username) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        UserRole role;
        if(userOptional.isPresent()){
            role = userOptional.get().getUserRole();
        }else{
            throw new BadRequestException("Not able to fetch User details of username( "+username+" ), Recheck the value!");
        }
        return role;
    }

    public List<UserRole> getAllRole() {
        List<UserRole> roles =userRoleRepository.findAll();
        return roles;
    }


}


