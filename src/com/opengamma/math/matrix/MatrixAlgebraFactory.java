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
  public static final String COMMONS = "Commons";
  public static final String OG = "OG";
  public static final ColtMatrixAlgebra COLT_ALGEBRA = new ColtMatrixAlgebra();
  public static final CommonsMatrixAlgebra COMMONS_ALGEBRA = new CommonsMatrixAlgebra();
  public static final OGMatrixAlgebra OG_ALGEBRA = new OGMatrixAlgebra();
  private static final Map<String, MatrixAlgebra> s_staticInstances;
  private static final Map<Class<?>, String> s_instanceNames;

  static {
    s_staticInstances = new HashMap<String, MatrixAlgebra>();
    s_instanceNames = new HashMap<Class<?>, String>();
    s_staticInstances.put(COLT, COLT_ALGEBRA);
    s_instanceNames.put(MatrixAlgebra.class, COLT);
    s_staticInstances.put(COMMONS, COMMONS_ALGEBRA);
    s_instanceNames.put(MatrixAlgebra.class, COMMONS);
    s_staticInstances.put(OG, OG_ALGEBRA);
    s_instanceNames.put(MatrixAlgebra.class, OG);
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
    if (s_instanceNames.containsKey(algebra.getClass())) {
      return s_instanceNames.get(algebra.getClass());
    }
    return null;
  }
}
