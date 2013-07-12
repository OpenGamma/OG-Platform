package com.opengamma.auth;

import com.opengamma.lambdava.functions.Function1;
import org.mindrot.jbcrypt.BCrypt;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import static com.opengamma.lambdava.streams.Lambdava.functional;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class Signer {

  /*
     Don't use SHA , Use bcrypt. http://codahale.com/how-to-safely-store-a-password/
   */
  public static String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  public static boolean verifyPassword(String candidate, String hashed) {
    return BCrypt.checkpw(candidate, hashed);
  }

  public static String hash(String message) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(message.getBytes("UTF-8"));
      byte[] digest = md.digest();
      BigInteger bigInt = new BigInteger(1, digest);
      return bigInt.toString(16);
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean verify(String message, String signature) {
    String hash = hash(message);
    return hash.equals(signature);
  }

  public static <T> Collection<T> verify(Capability<T> capability) {
    return functional(capability.getMessages())
        .filter(new Function1<SignedMessage<T>, Boolean>() {
          @Override
          public Boolean execute(SignedMessage signedMessage) {
            return verify(signedMessage.getMessage().toString(), signedMessage.getSignature());
          }
        })
        .map(new Function1<SignedMessage<T>, T>() {
          @Override
          public T execute(SignedMessage<T> tSignedMessage) {
            return tSignedMessage.getMessage();
          }
        })
        .asList();
  }

  public static <T> SignedMessage<T> sign(T message) {
    String signature = hash(message.toString());
    return SignedMessage.of(message, signature);
  }
}
