/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * 
 */
public class PrimeNumbers {
  private static final TreeSet<Integer> PRIME_SET;
  private static final List<Integer> PRIME_LIST;
  static {
    final List<Integer> list = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151,
        157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373,
        379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541);
    PRIME_LIST = Collections.unmodifiableList(list);
    PRIME_SET = new TreeSet<Integer>(list);
  }

  public static List<Integer> getPrimes() {
    return PRIME_LIST;
  }

  public static Integer getNthPrime(final int n) {
    if (n > PRIME_LIST.size()) {
      throw new IllegalArgumentException("List of primes only contains " + PRIME_LIST.size() + " elements");
    }
    return PRIME_LIST.get(n - 1);
  }

  public static Integer getNextPrime(final double d) {
    if (Math.abs(d - (int) d) < 1e-15 && PRIME_SET.contains((int) d)) {
      return (int) d;
    }
    return PRIME_SET.higher((int) Math.ceil(d));
  }

  public static Integer getPreviousPrime(final double d) {
    return PRIME_SET.lower((int) Math.floor(d));
  }
}
