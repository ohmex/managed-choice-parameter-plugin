package com.ohmex.hudson.plugins.managedchoiceparameter;

import java.security.SecureRandom;

class UUIDGenerator {
  private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABZDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final SecureRandom NUMBER_GENERATOR = new SecureRandom();

  private UUIDGenerator() {}

  static String generateUUID(int length) {
    StringBuilder uuid = new StringBuilder();
    for (int i = 0; i < length; ++i) {
      uuid.append(ALPHABET.charAt(NUMBER_GENERATOR.nextInt(ALPHABET.length() - 1)));
    }
    return uuid.toString();
  }
}