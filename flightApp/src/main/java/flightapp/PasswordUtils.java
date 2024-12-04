package flightapp;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


/**
 * A collection of utility methods to help with managing passwords
 */
public class PasswordUtils {
  /**
   * Generates a cryptographically-secure salted password.
   */
  public static byte[] saltAndHashPassword(String password) {
    byte[] salt = generateSalt();
    byte[] saltedHash = hashWithSalt(password, salt);

    // combine both byte[]
    byte[] result = new byte[saltedHash.length + salt.length];
    int result_index = 0;
    for (int i = 0; i < saltedHash.length; i++) {
      result[result_index] = saltedHash[i];
      result_index++;
    }
    for (int i = 0; i < salt.length; i++) {
      result[result_index] = salt[i];
      result_index++;
    }
    return result;
  }

  /**
   * Verifies whether the plaintext password can be hashed to provided salted hashed password.
   */
  public static boolean plaintextMatchesSaltedHash(String plaintext, byte[] saltedHashed) {
    int saltIndex = 0; //keep track of where the salt starts for later

    //get the hashed password
    byte[] hashedPas = new byte[saltedHashed.length - SALT_LENGTH_BYTES];
    for(int i=0; i<hashedPas.length; i++){
      hashedPas[i] = saltedHashed[i];
      saltIndex++;
    }

    //get the salt
    byte[] salt = new byte[SALT_LENGTH_BYTES];
    for(int i=0; i<SALT_LENGTH_BYTES; i++){
      salt[i] = saltedHashed[i+saltIndex];
    }

    //hash the inputted password
    byte[] ptHash = hashWithSalt(plaintext, salt);

    //check that the passwords are the same
    if(hashedPas.length != ptHash.length){
      return false;
    }
    for(int i=0; i<hashedPas.length; i++){
      if(hashedPas[i] != ptHash[i]){
        return false;
      }
    }
    return true;
  }
  
  // Password hashing parameter constants.
  private static final int HASH_STRENGTH = 65536;
  private static final int KEY_LENGTH_BYTES = 128;
  private static final int SALT_LENGTH_BYTES = 16;

  /**
   * Generate a small bit of randomness to serve as a password "salt"
   */
  static byte[] generateSalt() {
    SecureRandom r = new SecureRandom();
    byte[] salt = new byte[SALT_LENGTH_BYTES];
    r.nextBytes(salt);
    return salt;
  }

  /**
   * Uses the provided salt to generate a cryptographically-secure hash of the provided password.
   * The resultant byte array will be KEY_LENGTH_BYTES bytes long.
   */
  static byte[] hashWithSalt(String password, byte[] salt)
    throws IllegalStateException {
    // Specify the hash parameters, including the salt
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt,
                                  HASH_STRENGTH, KEY_LENGTH_BYTES * 8 /* length in bits */);

    // Hash the whole thing
    SecretKeyFactory factory = null;
    byte[] hash = null; 
    try {
      factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      hash = factory.generateSecret(spec).getEncoded();
      return hash;
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IllegalStateException();
    }
  }

}
