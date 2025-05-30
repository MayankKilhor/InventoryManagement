package com.imspos.auth_service.utils;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomNumberUtils {

    public String generateRandomDigits(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();

    }
}
