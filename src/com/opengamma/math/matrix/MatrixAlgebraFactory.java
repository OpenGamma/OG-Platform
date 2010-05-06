/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.HashMap;
import java.util.Map;

public class MatrixAlgebraFactory {
  public static final String COLT = "Colt";
  public static final MatrixAlgebra COLT_ALGEBRA = new ColtMatrixAlgebra();
  private static final Map<String, MatrixAlgebra> s_staticInstances;
  private static final Map<Class<?>, String> s_instanceNames;

  static {
    s_staticInstances = new HashMap<String, MatrixAlgebra>();
    s_instanceNames = new HashMap<Class<?>, String>();
    s_staticInstances.put(COLT, COLT_ALGEBRA);
    s_instanceNames.put(MatrixAlgebra.class, COLT);
  }

  private MatrixAlgebraFactory() {
  }

  public static MatrixAlgebra getMatrixAlgebra(final String algebraName) {
    if (s_staticInstances.containsKey(algebraName))
      return s_staticInstances.get(algebraName);
    throw new IllegalArgumentException("Matrix algebra " + algebraName + " not found");
  }

  public static String getMatrixAlgebraName(final MatrixAlgebra algebra) {
    if (s_instanceNames.containsKey(algebra.getClass()))
      return s_instanceNames.get(algebra.getClass());
    return null;
  }
}
