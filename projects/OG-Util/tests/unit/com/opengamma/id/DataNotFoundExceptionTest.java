/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.opengamma.DataNotFoundException;

/**
 * Test DataNotFoundException. 
 */
public class DataNotFoundExceptionTest {

  @Test
  public void test_constructor_String() {
    DataNotFoundException test = new DataNotFoundException("Msg");
    assertEquals("Msg", test.getMessage());
    assertEquals(null, test.getCause());
  }

  @Test
  public void test_constructor_String_Throwable() {
    Throwable th = new NullPointerException();
    DataNotFoundException test = new DataNotFoundException("Msg", th);
    assertEquals(true, test.getMessage().contains("Msg"));
    assertSame(th, test.getCause());
  }

}
