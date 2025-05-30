package com.imspos.auth_service.service.impl;


import com.imspos.auth_service.model.ApiAccessControl;
import com.imspos.auth_service.repository.ApiAccessControlRepository;
import com.imspos.auth_service.service.ApiAccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiAccessControlServiceImpl implements ApiAccessControlService, ApplicationListener<ContextRefreshedEvent> {

    private final ApiAccessControlRepository repository;
    private List<ApiAccessControl> accessControlList;

    @Autowired
    public ApiAccessControlServiceImpl(ApiAccessControlRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.accessControlList = repository.findAll();
    }

    public List<ApiAccessControl> getAccessControlList() {
        if (accessControlList == null) {
            throw new IllegalStateException("Access control list not initialized yet");
        }
        return accessControlList;
    }
}
