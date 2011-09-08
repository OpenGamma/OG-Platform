/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ExceptionFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_UTC() {
    Exception object = new IllegalArgumentException("Testing");
    Exception cycled = cycleObject(Exception.class, object);
    assertEquals(IllegalArgumentException.class, cycled.getClass());
    assertEquals("Testing", cycled.getMessage());
  }

}
