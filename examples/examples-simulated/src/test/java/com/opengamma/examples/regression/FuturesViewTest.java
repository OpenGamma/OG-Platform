/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.regression;

import java.io.File;

import org.testng.annotations.Test;

import com.opengamma.integration.regression.AbstractRegressionTest;
import com.opengamma.util.test.TestGroup;

/**
 * Example regression test.
 */
//not strictly a unit test, but tagged as such due to limitations
//of current build infrastructure
@Test(groups = TestGroup.UNIT) 
public class FuturesViewTest extends AbstractRegressionTest {
  
  
  public FuturesViewTest() {
    super(new File("src/test/resources/FuturesView_example"), "classpath:regression/regression-testdb.properties");
  }
  
  
  @Test(enabled=false) //PLAT-6127
  public void testFuturesView() {
    runTestForView("Futures View", "Futures Snapshot");
  }

  @Override
  protected double getAcceptableDelta() {
    return 0.0001;
  }
  
  
  
  
}
