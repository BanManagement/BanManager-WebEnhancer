package me.confuser.banmanager.webenhancer.common.security;

// Based on Spring Security 5.7
// https://github.com/spring-projects/spring-security/blob/5.7.x/crypto/src/main/java/org/springframework/security/crypto/argon2/Argon2EncodingUtils.java
// Modified to reduce dependencies

/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Base64;

import me.confuser.banmanager.webenhancer.common.bouncycastle.crypto.params.Argon2Parameters;
import me.confuser.banmanager.webenhancer.common.bouncycastle.util.Arrays;

/**
 * Utility for encoding and decoding Argon2 hashes.
 *
 * Used by {@link Argon2PasswordEncoder}.
 *
 * @author Simeon Macke
 * @since 5.3
 */
final class Argon2EncodingUtils {

	private static final Base64.Encoder b64encoder = Base64.getEncoder().withoutPadding();

	private Argon2EncodingUtils() {
	}

	/**
	 * Encodes a raw Argon2-hash and its parameters into the standard Argon2-hash-string
	 * as specified in the reference implementation
	 * (https://github.com/P-H-C/phc-winner-argon2/blob/master/src/encoding.c#L244):
	 *
	 * {@code $argon2<T>[$v=<num>]$m=<num>,t=<num>,p=<num>$<bin>$<bin>}
	 *
	 * where {@code <T>} is either 'd', 'id', or 'i', {@code <num>} is a decimal integer
	 * (positive, fits in an 'unsigned long'), and {@code <bin>} is Base64-encoded data
	 * (no '=' padding characters, no newline or whitespace).
	 *
	 * The last two binary chunks (encoded in Base64) are, in that order, the salt and the
	 * output. If no salt has been used, the salt will be omitted.
	 * @param hash the raw Argon2 hash in binary format
	 * @param parameters the Argon2 parameters that were used to create the hash
	 * @return the encoded Argon2-hash-string as described above
	 * @throws IllegalArgumentException if the Argon2Parameters are invalid
	 */
	static String encode(byte[] hash, Argon2Parameters parameters) throws IllegalArgumentException {
		StringBuilder stringBuilder = new StringBuilder();
		switch (parameters.getType()) {
		case Argon2Parameters.ARGON2_d:
			stringBuilder.append("$argon2d");
			break;
		case Argon2Parameters.ARGON2_i:
			stringBuilder.append("$argon2i");
			break;
		case Argon2Parameters.ARGON2_id:
			stringBuilder.append("$argon2id");
			break;
		default:
			throw new IllegalArgumentException("Invalid algorithm type: " + parameters.getType());
		}
		stringBuilder.append("$v=").append(parameters.getVersion()).append("$m=").append(parameters.getMemory())
				.append(",t=").append(parameters.getIterations()).append(",p=").append(parameters.getLanes());
		if (parameters.getSalt() != null) {
			stringBuilder.append("$").append(b64encoder.encodeToString(parameters.getSalt()));
		}
		stringBuilder.append("$").append(b64encoder.encodeToString(hash));
		return stringBuilder.toString();
	}

}
