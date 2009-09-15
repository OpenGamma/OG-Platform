/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.securities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class CurrencyTest {
  
  @Test
  public void testLookups() {
    Currency c1 = Currency.getInstance("USD");
    assertNotNull(c1);
    assertEquals("USD", c1.getISOCode());
    assertSame(c1, Currency.getInstance("USD"));
  }

}
