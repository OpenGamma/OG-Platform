/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSortedMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.isda.credit.CdsQuote;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.ParSpreadQuote;
import com.opengamma.sesame.credit.CreditPricingSampleData;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test credit curve parallel shifts
 */
@Test(groups = TestGroup.UNIT)
public class CreditCurveParallelShiftTest {

  @Test
  public void absolute() {
    double shiftAmount = 0.0001;
    CreditCurveParallelShift absolute = CreditCurveParallelShift.absolute(shiftAmount);
    CreditCurveData shifted = absolute.apply(CreditPricingSampleData.createSingleNameCreditCurveData(),
                                             StandardMatchDetails.MATCH);
    ImmutableSortedMap<Tenor, CdsQuote> cdsQuotes = shifted.getCdsQuotes();
    for (Map.Entry<Tenor, CdsQuote> entry : cdsQuotes.entrySet()) {
      ParSpreadQuote quote = (ParSpreadQuote) entry.getValue();
      assertEquals(0.0029, quote.getParSpread());
    }
  }

  @Test
  public void relative() {
    double shiftAmount = 0.01; //shift 1%
    CreditCurveParallelShift relative = CreditCurveParallelShift.relative(shiftAmount);
    CreditCurveData shifted = relative.apply(CreditPricingSampleData.createSingleNameCreditCurveData(),
                                             StandardMatchDetails.MATCH);
    ImmutableSortedMap<Tenor, CdsQuote> cdsQuotes = shifted.getCdsQuotes();
    for (Map.Entry<Tenor, CdsQuote> entry : cdsQuotes.entrySet()) {
      ParSpreadQuote quote = (ParSpreadQuote) entry.getValue();
      assertEquals(0.002828, quote.getParSpread());
    }
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void pointsUpFrontQuote() {
    CreditCurveParallelShift relative = CreditCurveParallelShift.relative(0.01);
    relative.apply(CreditPricingSampleData.createPUFSingleNameCreditCurveData(), StandardMatchDetails.MATCH);
  }


}
