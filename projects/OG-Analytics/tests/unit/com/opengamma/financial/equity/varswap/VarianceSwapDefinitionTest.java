/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.equity.varswap.definition.VarianceSwapDefinition;
import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.util.money.Currency;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * 
 */
public class VarianceSwapDefinitionTest {

  private final ZonedDateTime now = ZonedDateTime.now();
  private final ZonedDateTime tPlus2 = now.plusDays(2);
  private final ZonedDateTime plus1y = now.plusYears(1);
  private final ZonedDateTime plus5y = now.plusYears(5);
  private final PeriodFrequency obsFreq = PeriodFrequency.DAILY;
  private final int nObsExpected = 750; // TODO Case Calendar. Get nObsExpected from calendar
  private final Currency ccy = Currency.EUR;
  private final Calendar cal = null; // TODO Case Calendar.
  private final double obsPerYear = 250;
  private final double volStrike = 0.25;
  private final double volNotional = 1.0E6;

  @Test
  public void swapForwardStarting() {
    final VarianceSwapDefinition varSwapDefn = new VarianceSwapDefinition(tPlus2, plus5y, plus5y, obsFreq, nObsExpected, ccy, cal, obsPerYear, volStrike, volNotional);

    VarianceSwap forwardVar = varSwapDefn.toDerivative(now, null);
  }

}
