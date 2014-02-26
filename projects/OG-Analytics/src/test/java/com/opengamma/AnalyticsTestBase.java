/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.testng.annotations.Test;

/**
 * 
 */
@Test
public abstract class AnalyticsTestBase {

  public AnalyticsTestBase(final Class<?> clazz, final Object[] variables, final Class<?>[] variableClasses, final boolean[] notNull) throws Exception {
    testNullInputs(clazz, variables, variableClasses, notNull);
  }

  /**
   * Tests attempted construction with non-null parameters fails.
   * @throws NoSuchMethodException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  public void testNullInputs(final Class<?> clazz, final Object[] variables, final Class<?>[] variableClasses, final boolean[] notNull)
      throws NoSuchMethodException, IllegalAccessException, InstantiationException {
    final Constructor<?> constructor = clazz.getConstructor(variableClasses);
    final int length = variables.length;
    for (int i = 0; i < length; i++) {
      if (notNull[i]) {
        final Object[] variablesWithNull = new Object[length];
        System.arraycopy(variables, 0, variablesWithNull, 0, length);
        variablesWithNull[i] = null;
        try {
          constructor.newInstance(variablesWithNull);
        } catch (final InvocationTargetException e) {
          assertTrue(e.getCause() instanceof IllegalArgumentException);
          continue;
        }
        fail();
      }
    }
  }
}
