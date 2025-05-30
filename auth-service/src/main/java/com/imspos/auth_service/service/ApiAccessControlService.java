package com.imspos.auth_service.service;

import com.imspos.auth_service.model.ApiAccessControl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ApiAccessControlService {

    public List<ApiAccessControl> getAccessControlList();
}
