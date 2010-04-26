/**
 * Copyright (C) 2010 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArgumentCheckerTest {

  @Test(expected=NullPointerException.class)
  public void test_checkNotNull_null() {
    try {
      ArgumentChecker.notNull(null, "name");
    } catch (NullPointerException ex) {
      assertTrue(ex.getMessage().contains("'name'"));
      assertFalse(ex.getMessage().contains("Injected"));
      throw ex;
    }
  }

  @Test
  public void test_checkNotNull_nonNull() {
    ArgumentChecker.notNull("Kirk", "name");
  }

  //-------------------------------------------------------------------------
  @Test(expected=NullPointerException.class)
  public void test_checkNotNullInjected_null() {
    try {
      ArgumentChecker.notNullInjected(null, "name");
    } catch (NullPointerException ex) {
      assertTrue(ex.getMessage().contains("'name'"));
      assertTrue(ex.getMessage().contains("Injected"));
      throw ex;
    }
  }

  @Test
  public void test_checkNotNullInjected_nonNull() {
    ArgumentChecker.notNullInjected("Kirk", "name");
  }

}
