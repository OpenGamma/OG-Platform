/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

/**
 * 
 */
public class EquityIndexDividendFutureTest {

  public static final double PRICE = 95.0;
  
  @Test
  public void test() {
    final double settlement = 1.45;
    final double fixing = 1.44;
    final EquityIndexDividendFuture theFuture = new EquityIndexDividendFuture(fixing, settlement, PRICE, 10., "DIVIDX","USD");
    
    assertEquals(theFuture.getDeliveryDate(),settlement,0);
    assertFalse(theFuture.getFixingDate()==settlement);
  }

}
