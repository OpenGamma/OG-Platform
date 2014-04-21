/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MarketDataShiftTest {

  @Test
  public void absolute() {
    MarketDataShift shift = new MarketDataShift(ScenarioShiftType.ABSOLUTE, 1);
    assertEquals(shift.execute(3d, null, null), 4d);
  }

  @Test
  public void relative() {
    MarketDataShift shift = new MarketDataShift(ScenarioShiftType.RELATIVE, 0.5);
    assertEquals(shift.execute(3d, null, null), 4.5d);
  }
}
