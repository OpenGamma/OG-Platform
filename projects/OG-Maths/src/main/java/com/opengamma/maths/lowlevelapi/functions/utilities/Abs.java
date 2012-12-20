/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import org.apache.commons.lang.Validate;

/**
 * Absolutes a vector V returning |V|. 
 */
public class Abs {
  // stateless
  public static int[] stateless(int[] v) {
    Validate.notNull(v);
    final int n = v.length;
    int[] tmp = new int[n];
    for (int i = 0; i < n; i++) {
      tmp[i] = Math.abs(v[i]);
    }
    return tmp;
  }  

  public static long[] stateless(long[] v) {
    Validate.notNull(v);
    final int n = v.length;
    long[] tmp = new long[n];
    for (int i = 0; i < n; i++) {
      tmp[i] = Math.abs(v[i]);
    }
    return tmp;
  }  
  
  public static float[] stateless(float[] v) {
    Validate.notNull(v);
    final int n = v.length;
    float[] tmp = new float[n];
    for (int i = 0; i < n; i++) {
      tmp[i] = Math.abs(v[i]);
    }
    return tmp;
  }  
    
  public static double[] stateless(double[] v) {
    Validate.notNull(v);
    final int n = v.length;
    double[] tmp = new double[n];
    for (int i = 0; i < n; i++) {
      tmp[i] = Math.abs(v[i]);
    }
    return tmp;
  }

  // inplace, these mangle memory
  public static void inPlace(int[] v) {
    Validate.notNull(v);
    final int n = v.length;
    for (int i = 0; i < n; i++) {
      v[i] = Math.abs(v[i]);
    }
  }  
  

  public static void inPlace(long[] v) {
    Validate.notNull(v);
    final int n = v.length;
    for (int i = 0; i < n; i++) {
      v[i] = Math.abs(v[i]);
    }
  }  
  
  public static void inPlace(float[] v) {
    Validate.notNull(v);
    final int n = v.length;
    for (int i = 0; i < n; i++) {
      v[i] = Math.abs(v[i]);
    }
  }  
  
  public static void inPlace(double[] v) {
    Validate.notNull(v);
    final int n = v.length;
    for (int i = 0; i < n; i++) {
      v[i] = Math.abs(v[i]);
    }
  }    
  
}
