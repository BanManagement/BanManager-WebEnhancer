package me.confuser.banmanager.webenhancer.common.security;

// Based on Spring Security 5.7
// https://github.com/spring-projects/spring-security/blob/5.7.x/crypto/src/main/java/org/springframework/security/crypto/argon2/Argon2PasswordEncoder.java
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

import java.security.SecureRandom;
import me.confuser.banmanager.webenhancer.common.bouncycastle.crypto.generators.Argon2BytesGenerator;
import me.confuser.banmanager.webenhancer.common.bouncycastle.crypto.params.Argon2Parameters;

/**
 * <p>
 * Implementation of PasswordEncoder that uses the Argon2 hashing function. Clients can
 * optionally supply the length of the salt to use, the length of the generated hash, a
 * cpu cost parameter, a memory cost parameter and a parallelization parameter.
 * </p>
 *
 * <p>
 * Note:
 * </p>
 * <p>
 * The currently implementation uses Bouncy castle which does not exploit
 * parallelism/optimizations that password crackers will, so there is an unnecessary
 * asymmetry between attacker and defender.
 * </p>
 *
 * @author Simeon Macke
 * @since 5.3
 */
public class Argon2PasswordEncoder {

	private static final int DEFAULT_SALT_LENGTH = 16;

	private static final int DEFAULT_HASH_LENGTH = 32;

	private static final int DEFAULT_PARALLELISM = 1;

	private static final int DEFAULT_MEMORY = 1 << 12;

	private static final int DEFAULT_ITERATIONS = 3;

	private final int hashLength;

	private final int parallelism;

	private final int memory;

	private final int iterations;

	private final BytesKeyGenerator saltGenerator;

	public Argon2PasswordEncoder() {
		this(DEFAULT_SALT_LENGTH, DEFAULT_HASH_LENGTH, DEFAULT_PARALLELISM, DEFAULT_MEMORY, DEFAULT_ITERATIONS);
	}

	public Argon2PasswordEncoder(int saltLength, int hashLength, int parallelism, int memory, int iterations) {
		this.hashLength = hashLength;
		this.parallelism = parallelism;
		this.memory = memory;
		this.iterations = iterations;
		this.saltGenerator = new SecureRandomBytesKeyGenerator(saltLength);
	}

	public String encode(CharSequence rawPassword) {
		byte[] salt = this.saltGenerator.generateKey();
		byte[] hash = new byte[this.hashLength];
		// @formatter:off
		Argon2Parameters params = new Argon2Parameters
				.Builder(Argon2Parameters.ARGON2_i)
				.withSalt(salt)
				.withParallelism(this.parallelism)
				.withMemoryAsKB(this.memory)
				.withIterations(this.iterations)
				.build();
		// @formatter:on
		Argon2BytesGenerator generator = new Argon2BytesGenerator();
		generator.init(params);
		generator.generateBytes(rawPassword.toString().toCharArray(), hash);
		return Argon2EncodingUtils.encode(hash, params);
	}

  public interface BytesKeyGenerator {

    /**
     * Get the length, in bytes, of keys created by this generator. Most unique keys are
     * at least 8 bytes in length.
     */
    int getKeyLength();

    /**
     * Generate a new key.
     */
    byte[] generateKey();
  }

  /**
   * A KeyGenerator that uses {@link SecureRandom} to generate byte array-based keys.
   * <p>
   * No specific provider is used for the {@code SecureRandom}, so the platform default will
   * be used.
   *
   * @author Keith Donald
   */
  public final class SecureRandomBytesKeyGenerator implements BytesKeyGenerator {

    private static final int DEFAULT_KEY_LENGTH = 8;

    private final SecureRandom random;

    private final int keyLength;

    /**
     * Creates a secure random key generator using the defaults.
     */
    public SecureRandomBytesKeyGenerator() {
      this(DEFAULT_KEY_LENGTH);
    }

    /**
     * Creates a secure random key generator with a custom key length.
     */
    public SecureRandomBytesKeyGenerator(int keyLength) {
      this.random = new SecureRandom();
      this.keyLength = keyLength;
    }

    @Override
    public int getKeyLength() {
      return this.keyLength;
    }

    @Override
    public byte[] generateKey() {
      byte[] bytes = new byte[this.keyLength];
      this.random.nextBytes(bytes);
      return bytes;
    }

  }
}
