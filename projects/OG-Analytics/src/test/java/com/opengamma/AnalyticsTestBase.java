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

/**
 * 
 */
public abstract class AnalyticsTestBase {

  /**
   * @param clazz The class to test
   * @param variables The variables
   * @param variableClasses The variable classes
   * @param notNull Indicates which variables can be null
   * @throws Exception If the object cannot be constructed successfully
   */
  public AnalyticsTestBase(final Class<?> clazz, final Object[] variables, final Class<?>[] variableClasses, final boolean[] notNull) throws Exception {
    testNullInputs(clazz, variables, variableClasses, notNull);
  }

  /**
   * Tests attempted construction with null values for non-nullable parameters fails.
   * @param clazz The class to test
   * @param variables The variables
   * @param variableClasses The variable classes
   * @param notNull Indicates whether a variable can be nullable
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
