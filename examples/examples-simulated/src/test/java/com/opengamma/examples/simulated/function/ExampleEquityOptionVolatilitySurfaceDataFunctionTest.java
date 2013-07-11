/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.function;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.examples.simulated.function.ExampleEquityOptionVolatilitySurfaceDataFunction;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExampleEquityOptionVolatilitySurfaceDataFunctionTest {
  // when written, this was the only unit test in examples-simulated
  // and was added to keep continuous integration happy

  @Test
  public void test_basics() {
    ExampleEquityOptionVolatilitySurfaceDataFunction test = new ExampleEquityOptionVolatilitySurfaceDataFunction("A", "B", "C");
    assertEquals("A", test.getDefinitionName());
    assertEquals("C", test.getSpecificationName());
  }

}
