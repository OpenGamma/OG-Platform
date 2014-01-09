/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;

import org.testng.annotations.Test;

/**
 * Example test
 */
@Test
public class FuturesViewTest extends AbstractRegressionTest {
  
  public FuturesViewTest() {
    super(new File("regression/multiview"), new File("regression/multiview/dbdump/dumpzip.zip"));
  }
  
  @Test
  public void testFuturesView() {
    runTestForView("Futures View", "Futures Snapshot");
  }
  
  @Test
  public void testEurSwapDeskView() {
    runTestForView("EUR Swap Desk View", "EUR Swap Desk Snapshot");
  }

  @Override
  protected double getAcceptableDelta() {
    return 1;
  }
  
  
  
  
}
