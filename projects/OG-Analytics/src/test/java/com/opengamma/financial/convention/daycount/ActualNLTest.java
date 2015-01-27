/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ActualNLTest {
  private static final DayCount ACTUAL_NL = DayCountFactory.of("Actual/NL");
  private static final double EPS = 1e-12;

  @DataProvider(name = "nl365")
  Object[][] data_nl365() {
    return new Object[][] {
        // start and end both in standard year, 
        {LocalDate.of(2011, 1, 31), LocalDate.of(2011, 2, 15), 15d / 365d},
        {LocalDate.of(2011, 1, 31), LocalDate.of(2011, 3, 15), 43d / 365d},
        {LocalDate.of(2011, 2, 28), LocalDate.of(2011, 3, 1), 1d / 365d},
        {LocalDate.of(2011, 2, 28), LocalDate.of(2011, 3, 15), 15d / 365d},
        {LocalDate.of(2011, 3, 31), LocalDate.of(2011, 5, 15), 45d / 365d},
        
        // start and end both in leap year, 
        {LocalDate.of(2012, 1, 31), LocalDate.of(2012, 2, 15), 15d / 365d},
        {LocalDate.of(2012, 1, 31), LocalDate.of(2012, 3, 15), 43d / 365d},
        {LocalDate.of(2012, 2, 28), LocalDate.of(2012, 2, 29), 0d},
        {LocalDate.of(2012, 2, 28), LocalDate.of(2012, 3, 1), 1d / 365d},
        {LocalDate.of(2012, 2, 28), LocalDate.of(2012, 3, 15), 15d / 365d},
        {LocalDate.of(2012, 3, 31), LocalDate.of(2012, 5, 15), 45d / 365d},
        
        // different year, no leap days
        {LocalDate.of(2010, 1, 31), LocalDate.of(2011, 1, 31), 1d},
        {LocalDate.of(2010, 1, 31), LocalDate.of(2012, 1, 31), 2d},
        
        // different year, no leap days
        {LocalDate.of(2012, 1, 31), LocalDate.of(2013, 1, 31), 1d},
        {LocalDate.of(2012, 1, 31), LocalDate.of(2016, 1, 31), 4d},
        {LocalDate.of(2012, 1, 31), LocalDate.of(2017, 1, 31), 5d},
        {LocalDate.of(2012, 7, 31), LocalDate.of(2013, 7, 31), 1d},
        {LocalDate.of(2012, 7, 31), LocalDate.of(2016, 7, 31), 4d},
        {LocalDate.of(2012, 7, 31), LocalDate.of(2017, 7, 31), 5d},
        
        // different year, from standard to leap
        {LocalDate.of(2012, 1, 1), LocalDate.of(2013, 1, 1), 1d},
        {LocalDate.of(2012, 1, 2), LocalDate.of(2013, 1, 1), 364d / 365d},
        {LocalDate.of(2012, 1, 3), LocalDate.of(2013, 1, 1), 363d / 365d},
        
        {LocalDate.of(2011, 12, 1), LocalDate.of(2012, 12, 1), 1d},
        {LocalDate.of(2011, 12, 1), LocalDate.of(2013, 12, 1), 2d},
        {LocalDate.of(2011, 12, 1), LocalDate.of(2014, 12, 1), 3d},
        
        {LocalDate.of(2011, 12, 1), LocalDate.of(2012, 12, 2), 1d + 1d / 365d},
        {LocalDate.of(2011, 12, 1), LocalDate.of(2012, 12, 3), 1d + 2d / 365d},
        
        {LocalDate.of(2011, 12, 3), LocalDate.of(2012, 12, 1), 363d / 365d},
        {LocalDate.of(2011, 12, 3), LocalDate.of(2012, 12, 2), 364d / 365d},
        {LocalDate.of(2011, 12, 3), LocalDate.of(2012, 12, 3), 1d},
    };
  }

  @Test(dataProvider = "nl365")
  public void testDifferentYearLeapDays2(LocalDate start, LocalDate end, double expected) {
    assertEquals(ACTUAL_NL.getDayCountFraction(start, end), expected, EPS);
  }

}
