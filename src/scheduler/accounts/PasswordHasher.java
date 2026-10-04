package scheduler.accounts;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted PBKDF2 password hashing using only the JDK.
 *
 * Stored format: {@code pbkdf2_sha512$<iterations>$<base64 salt>$<base64 hash>}.
 * The iteration count travels with each hash, so it can be raised later
 * without breaking passwords that were hashed under the old setting.
 */
public final class PasswordHasher {

    private static final String PREFIX = "pbkdf2_sha512";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    // OWASP Password Storage Cheat Sheet recommendation for PBKDF2-HMAC-SHA512
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 512;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {}

    /** Hashes a plain-text password with a fresh random salt. */
    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
    }

    /** True if the password matches the stored hash; false for a wrong password or a malformed hash. */
    public static boolean verify(String password, String storedHash) {
        if (password == null || !isHash(storedHash)) {
            return false;
        }
        try {
            String[] parts = storedHash.split("\\$");
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, iterations);
            // constant-time comparison so response time doesn't leak how much matched
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException malformed) {
            return false;
        }
    }

    /** True if the value looks like a hash produced by this class (used to upgrade legacy plain-text rows). */
    public static boolean isHash(String value) {
        return value != null && value.startsWith(PREFIX + "$") && value.split("\\$").length == 4;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 is not available in this JDK", e);
        }
    }
}
