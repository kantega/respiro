/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class CachingPasswordChecker implements PasswordChecker {

    private final PasswordChecker wrapped;
    private final Cache<String, AuthenticationResult> cache;

    private final SecretKeyFactory secretKeyFactory;
    private final String salt;
    private final int passwordCacheValidity;

    public CachingPasswordChecker(PasswordChecker wrapped, int passwordCacheValidity, TimeUnit timeUnit) {
        this.wrapped = wrapped;
        this.passwordCacheValidity = passwordCacheValidity;


        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(this.passwordCacheValidity, timeUnit)
                .build();
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] saltBytes = new byte[512];
        new SecureRandom().nextBytes(saltBytes);
        salt = Base64.getEncoder().encodeToString(saltBytes);
    }

    @Override
    public AuthenticationResult checkPassword(String username, String password) {
        try {
            return username == null || username.isEmpty()
                    ? AuthenticationResult.UNAUTHENTICATED
                    : cache.get(getSecureHash(username, password), () -> wrapped.checkPassword(username, password));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSecureHash(String username, String password) {
        String usernameAndPassword = username + password + salt;
        try {
            PBEKeySpec keySpec = new PBEKeySpec(usernameAndPassword.toCharArray(), username.getBytes(), 1000, 512);
            String hash = Base64.getEncoder().encodeToString(secretKeyFactory.generateSecret(keySpec).getEncoded());
            return hash;
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
