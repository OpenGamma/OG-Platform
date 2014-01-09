package com.opengamma.integration.regression;

import org.testng.annotations.Test;

@Test
public class FuturesViewTest extends AbstractRegressionTest {

  @Test
  public void doTest() {
    runTestForView("Futures View", "Futures Snapshot");
  }
  
  
}
