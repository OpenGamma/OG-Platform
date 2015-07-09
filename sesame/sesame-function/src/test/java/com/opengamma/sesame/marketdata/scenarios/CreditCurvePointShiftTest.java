/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.isda.credit.CdsQuote;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.ParSpreadQuote;
import com.opengamma.sesame.credit.CreditPricingSampleData;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test credit curve point shifts
 */
@Test(groups = TestGroup.UNIT)
public class CreditCurvePointShiftTest {
  public static final double DELTA = 1e-8;

  @Test
  public void absolute() {
    ImmutableMap<Tenor, Double> shifts = ImmutableMap.of(Tenor.ONE_YEAR, 0.0001,
                                                         Tenor.THREE_YEARS, 0.0002,
                                                         Tenor.TEN_YEARS, 0.0003);

    CreditCurvePointShift absolute = CreditCurvePointShift.absolute(shifts);
    CreditCurveData shifted = absolute.apply(CreditPricingSampleData.createSingleNameCreditCurveData(),
                                             StandardMatchDetails.MATCH);
    ImmutableSortedMap<Tenor, CdsQuote> cdsQuotes = shifted.getCdsQuotes();
    for (Map.Entry<Tenor, CdsQuote> entry : cdsQuotes.entrySet()) {
      ParSpreadQuote quote = (ParSpreadQuote) entry.getValue();
      double expected = 0.0028;
      Tenor tenor = entry.getKey();

      if (tenor== Tenor.ONE_YEAR) {
        expected = 0.0029;
      } else if (tenor == Tenor.THREE_YEARS) {
        expected = 0.0030;
      } else if (tenor == Tenor.TEN_YEARS) {
        expected = 0.0031;
      }

      assertEquals(expected, quote.getParSpread(), DELTA);
    }
  }

  @Test
  public void relative() {

    ImmutableMap<Tenor, Double> shifts = ImmutableMap.of(Tenor.ONE_YEAR, 0.01,    //1%
                                                         Tenor.THREE_YEARS, 0.02, //2%
                                                         Tenor.TEN_YEARS, 0.10);  //10%
    CreditCurvePointShift relative = CreditCurvePointShift.relative(shifts);
    CreditCurveData shifted = relative.apply(CreditPricingSampleData.createSingleNameCreditCurveData(),
                                             StandardMatchDetails.MATCH);
    ImmutableSortedMap<Tenor, CdsQuote> cdsQuotes = shifted.getCdsQuotes();
    for (Map.Entry<Tenor, CdsQuote> entry : cdsQuotes.entrySet()) {
      ParSpreadQuote quote = (ParSpreadQuote) entry.getValue();
      double expected = 0.0028;
      Tenor tenor = entry.getKey();

      if (tenor== Tenor.ONE_YEAR) {
        expected = 0.002828;
      } else if (tenor == Tenor.THREE_YEARS) {
        expected = 0.002856;
      } else if (tenor == Tenor.TEN_YEARS) {
        expected = 0.00308;
      }
        assertEquals(expected, quote.getParSpread(), DELTA);
    }
  }

  @Test
  public void flooredShift() {
    ImmutableMap<Tenor, Double> shifts = ImmutableMap.of(Tenor.ONE_YEAR, -0.0030,
                                                         Tenor.THREE_YEARS, -0.0050,
                                                         Tenor.TEN_YEARS, -0.0100);
    CreditCurvePointShift relative = CreditCurvePointShift.absolute(shifts);
    CreditCurveData shifted = relative.apply(CreditPricingSampleData.createSingleNameCreditCurveData(),
                                             StandardMatchDetails.MATCH);
    ImmutableSortedMap<Tenor, CdsQuote> cdsQuotes = shifted.getCdsQuotes();
    for (Map.Entry<Tenor, CdsQuote> entry : cdsQuotes.entrySet()) {
      ParSpreadQuote quote = (ParSpreadQuote) entry.getValue();
      double expected = 0.0028;
      Tenor tenor = entry.getKey();

      if (tenor== Tenor.ONE_YEAR) {
        expected = 0d;
      } else if (tenor == Tenor.THREE_YEARS) {
        expected = 0d;
      } else if (tenor == Tenor.TEN_YEARS) {
        expected = 0d;
      }
      assertEquals(expected, quote.getParSpread(), DELTA);
    }
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void pointsUpFrontQuote() {
    ImmutableMap<Tenor, Double> shifts = ImmutableMap.of(Tenor.FIVE_YEARS, 0.0001);
    CreditCurvePointShift relative = CreditCurvePointShift.relative(shifts);
    relative.apply(CreditPricingSampleData.createPUFSingleNameCreditCurveData(), StandardMatchDetails.MATCH);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void tenorMismatch1() {
    ImmutableMap<Tenor, Double> shifts = ImmutableMap.of(Tenor.ONE_DAY, 0.0001,       //missing
                                                         Tenor.ONE_YEAR, 0.001,       //included
                                                         Tenor.ofDays(1000), 0.0001); //missing
    CreditCurvePointShift relative = CreditCurvePointShift.relative(shifts);
    relative.apply(CreditPricingSampleData.createSingleNameCreditCurveData(), StandardMatchDetails.MATCH);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void tenorMismatch2() {
    ImmutableMap<Tenor, Double> shifts = ImmutableMap.of(Tenor.ONE_DAY, 0.0001);
    CreditCurvePointShift relative = CreditCurvePointShift.relative(shifts);
    relative.apply(CreditPricingSampleData.createSingleNameCreditCurveData(), StandardMatchDetails.MATCH);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void emptyShift() {
    ImmutableMap<Tenor, Double> shifts = ImmutableMap.of();
    CreditCurvePointShift relative = CreditCurvePointShift.relative(shifts);
    relative.apply(CreditPricingSampleData.createSingleNameCreditCurveData(), StandardMatchDetails.MATCH);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullShift() {
    CreditCurvePointShift relative = CreditCurvePointShift.relative(null);
    relative.apply(CreditPricingSampleData.createSingleNameCreditCurveData(), StandardMatchDetails.MATCH);
  }


}
