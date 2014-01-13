/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.regression;

import java.io.File;

import org.testng.annotations.Test;

import com.opengamma.integration.regression.AbstractRegressionTest;

/**
 * Example regression test.
 */
@Test
public class FuturesViewTest extends AbstractRegressionTest {
  
  public FuturesViewTest() {
    super(new File("regression/multiview_example"), "classpath:regression/regression-toolcontext.properties", "classpath:regression/regression-testdb.properties");
  }
  
  @Test
  public void testFuturesView() {
    runTestForView("Futures View", "Futures Snapshot");
  }

  @Override
  protected double getAcceptableDelta() {
    return 0.0001;
  }
  
  
  
  
}
