/*
 * Copyright (c) 2022 Contributors to the Collector project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.redhat.quarkus.mandrel.collector.access.auth;

import com.redhat.quarkus.mandrel.collector.access.model.Token;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

@ApplicationScoped
public class TokenRepository implements PanacheRepository<Token> {
    public List<Token> findByUsernameTokenHash(String username, String tokenHash) {
        return find("tokenHash = ?1 and user.username = ?2", tokenHash, username).list();
    }

    public List<Token> findByUsername(String username) {
        return find("user.username = ?1", username).list();
    }

    public static String hash(String cleartext) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-512").digest(cleartext.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot hash on this system.", e);
        }
    }

    public static String randomStringHashed() throws NoSuchAlgorithmException {
        final byte[] bytes = new byte[256];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-512").digest(bytes));
    }

}
