/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link DataNotFoundException}. 
 */
@Test(groups = TestGroup.UNIT)
public class DataNotFoundExceptionTest {

  public void test_constructor_String() {
    DataNotFoundException test = new DataNotFoundException("Msg");
    assertEquals("Msg", test.getMessage());
    assertEquals(null, test.getCause());
  }

  public void test_constructor_String_Throwable() {
    Throwable th = new NullPointerException();
    DataNotFoundException test = new DataNotFoundException("Msg", th);
    assertEquals(true, test.getMessage().contains("Msg"));
    assertSame(th, test.getCause());
  }

}
