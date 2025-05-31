package com.imspos.auth_service.service;

import com.imspos.auth_service.model.*;
import com.imspos.auth_service.payload.request.role.*;
import com.imspos.auth_service.payload.response.ApiAccessControlResult;

import java.util.List;
import java.util.Set;

public interface RoleService {

    User createAdminUser();

    Authority createAuthority(User creatorUser, CreateAuthorityRequest createAuthorityRequest);

    UserRole createUserRole(User user);

    UserRole createRole(User creatorUser, CreateRoleRequest createRoleRequest);

    UserRole assignAuthority(User creatorUser, AssignOrRemoveAuthorityRequest assignOrRemoveAuthorityRequest);

    UserRole removeAuthority(User creatorUser, AssignOrRemoveAuthorityRequest assignOrRemoveAuthorityRequest);

    Set<Authority> defaultAuthority(User adminUser);

    Set<Authority> getAuthority(String username);

    List<Authority> getAllAuthority();

    UserRole getRole(String username);

    List<UserRole> getAllRole();

    ApiAccessControlResult defaultApiAccessControl(User adminUser);

    ApiAccessControl createApiAccessControl(String endpointPath, String httpMethod, Set<String> authorityNames, Boolean isPublic);
}
