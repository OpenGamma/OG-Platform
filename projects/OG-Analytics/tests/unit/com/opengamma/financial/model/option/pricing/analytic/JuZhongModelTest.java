/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import org.junit.Test;

/**
 * 
 */
public class JuZhongModelTest extends AmericanAnalyticOptionModelTest {

  @Test
  public void test() {
    super.test(new JuZhongModel(), 10);
  }
}
