/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JuZhongModelTest extends AmericanAnalyticOptionModelTest {

  @Test
  public void test() {
    super.assertValid(new JuZhongModel(), 10);
  }
}
