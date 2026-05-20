package com.aashushaikh.auth.client;

import com.aashushaikh.auth.dto.CreateUserProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user", path = "/users")
public interface UserServiceClient {

    @PostMapping("/")
    void createUserProfile(@RequestBody CreateUserProfileRequest request);
}
