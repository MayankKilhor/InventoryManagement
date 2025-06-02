package com.imspos.product_service.payload.response;



import com.imspos.auth_service.model.ApiAccessControl;

import java.util.List;
import java.util.Set;

public class ApiAccessControlResult {
    private Set<ApiAccessControl> successfulApiAccessControls;
    private List<String> errors;

    public ApiAccessControlResult(Set<ApiAccessControl> successfulApiAccessControls, List<String> errors) {
        this.successfulApiAccessControls = successfulApiAccessControls;
        this.errors = errors;
    }

    public Set<ApiAccessControl> getSuccessfulApiAccessControls() {
        return successfulApiAccessControls;
    }

    public void setSuccessfulApiAccessControls(Set<ApiAccessControl> successfulApiAccessControls) {
        this.successfulApiAccessControls = successfulApiAccessControls;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
