/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Factory class for various types of matrix algebra calculators.
 */
public final class MatrixAlgebraFactory {
  /**
   * Label for Colt matrix algebra
   */
  public static final String COLT = "Colt";
  /**
   * Label for Commons matrix algebra
   */
  public static final String COMMONS = "Commons";
  /**
   * Label for OpenGamma matrix algebra
   */
  public static final String OG = "OG";
  /**
   * Instance of Colt matrix algebra
   */
  public static final ColtMatrixAlgebra COLT_ALGEBRA = new ColtMatrixAlgebra();
  /**
   * Instance of Commons matrix algebra
   */
  public static final CommonsMatrixAlgebra COMMONS_ALGEBRA = new CommonsMatrixAlgebra();
  /**
   * Instance of OpenGamma matrix algebra
   */
  public static final OGMatrixAlgebra OG_ALGEBRA = new OGMatrixAlgebra();
  private static final Map<String, MatrixAlgebra> s_staticInstances;
  private static final Map<Class<?>, String> s_instanceNames;

  static {
    s_staticInstances = new HashMap<String, MatrixAlgebra>();
    s_instanceNames = new HashMap<Class<?>, String>();
    s_staticInstances.put(COLT, COLT_ALGEBRA);
    s_instanceNames.put(ColtMatrixAlgebra.class, COLT);
    s_staticInstances.put(COMMONS, COMMONS_ALGEBRA);
    s_instanceNames.put(CommonsMatrixAlgebra.class, COMMONS);
    s_staticInstances.put(OG, OG_ALGEBRA);
    s_instanceNames.put(OGMatrixAlgebra.class, OG);
  }

  private MatrixAlgebraFactory() {
  }

  public static MatrixAlgebra getMatrixAlgebra(final String algebraName) {
    if (s_staticInstances.containsKey(algebraName)) {
      return s_staticInstances.get(algebraName);
    }
    throw new IllegalArgumentException("Matrix algebra " + algebraName + " not found");
  }

  public static String getMatrixAlgebraName(final MatrixAlgebra algebra) {
    if (algebra == null) {
      return null;
    }
    return s_instanceNames.get(algebra.getClass());
  }
}
