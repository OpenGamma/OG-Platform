/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * And no, this isn't in the Java API and isn't in Google Collections and isn't in Google Guava (well, at least the box methods aren't yet).
 *
 * @author jim
 */
public class Primitives {
  public static Long[] box(long[] input) {
    Long[] output = new Long[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = Long.valueOf(input[i]);
    }
    return output;
  }
  
  public static long[] unbox(Long[] input) {
    long[] output = new long[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }
  
  public static Double[] box(double[] input) {
    Double[] output = new Double[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = Double.valueOf(input[i]);
    }
    return output;
  }
  
  public static double[] unbox(Double[] input) {
    double[] output = new double[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }
  
  public static Integer[] box(int[] input) {
    Integer[] output = new Integer[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = Integer.valueOf(input[i]);
    }
    return output;
  }
  
  public static int[] unbox(Integer[] input) {
    int[] output = new int[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }
  
  public static Byte[] box(byte[] input) {
    Byte[] output = new Byte[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = Byte.valueOf(input[i]);
    }
    return output;
  }
  
  public static byte[] unbox(Byte[] input) {
    byte[] output = new byte[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }
  
  public static Boolean[] box(boolean[] input) {
    Boolean[] output = new Boolean[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = Boolean.valueOf(input[i]);
    }
    return output;
  }
  
  public static boolean[] unbox(Boolean[] input) {
    boolean[] output = new boolean[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }
  
  public static Short[] box(short[] input) {
    Short[] output = new Short[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = Short.valueOf(input[i]);
    }
    return output;
  }
  
  public static short[] unbox(Short[] input) {
    short[] output = new short[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }
  
  public static Character[] box(char[] input) {
    Character[] output = new Character[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = Character.valueOf(input[i]);
    }
    return output;
  }
  
  public static char[] unbox(Character[] input) {
    char[] output = new char[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }
  
  public static Float[] box(float[] input) {
    Float[] output = new Float[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = Float.valueOf(input[i]);
    }
    return output;
  }
  
  public static float[] unbox(Float[] input) {
    float[] output = new float[input.length];
    for (int i=0; i<input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }
  
  
}
