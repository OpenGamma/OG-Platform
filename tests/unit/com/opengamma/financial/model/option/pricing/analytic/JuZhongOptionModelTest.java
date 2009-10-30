/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class JuZhongOptionModelTest extends AmericanAnalyticOptionModelTest {

  @Test
  public void test() {
    super.test(new JuZhongOptionModel(), 1e-1);
  }
}
