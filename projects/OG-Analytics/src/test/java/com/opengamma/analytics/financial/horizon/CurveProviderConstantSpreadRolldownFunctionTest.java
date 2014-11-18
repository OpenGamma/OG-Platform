/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.datasets.SimpleDataSetsInflationIssuerUsd;
import com.opengamma.analytics.financial.interestrate.datasets.SimpleDataSetsInflationUsd;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/** 
 * Tests related to the fixed time shift roll down.
 */
@Test(groups = TestGroup.UNIT)
public class CurveProviderConstantSpreadRolldownFunctionTest {
  
  private static final ZonedDateTime CAlIBRATION_DATE = DateUtils.getUTCDate(2014, 11, 17);
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime NEXT_DAY = ScheduleCalculator.getAdjustedDate(CAlIBRATION_DATE, 1, NYC);
  private static final double TIME_SHIFT = TimeCalculator.getTimeBetween(CAlIBRATION_DATE, NEXT_DAY);
  /** Provider Inflation */
  private static final InflationProviderDiscount INFLATION = SimpleDataSetsInflationUsd.getProvider();
  private static final IndexPrice CPI = SimpleDataSetsInflationUsd.getPriceIndex();
  /** Provider Inflation/Issuer. */
  private static final InflationIssuerProviderDiscount INFLATION_ISSUER = SimpleDataSetsInflationIssuerUsd.getProvider();
  private static final String ISSUER_NAME = SimpleDataSetsInflationIssuerUsd.getIssuerName();
  private static final LegalEntity ISSUER_ENTITY = new LegalEntity(ISSUER_NAME, ISSUER_NAME, null, null, null);
  
  private static final CurveProviderConstantSpreadRolldownFunction ROLLDOWN_PROVIDER = 
      CurveProviderConstantSpreadRolldownFunction.getInstance();
  
  private static final double TOLERANCE_PRICE = 1.0E-6;
  private static final double TOLERANCE_DF = 1.0E-6;
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullData() {
    ROLLDOWN_PROVIDER.rollDown(null, TIME_SHIFT);
  }

  @Test
  public void inflationProvider() {
    InflationProviderDiscount inflationTimeShift =
        (InflationProviderDiscount) ROLLDOWN_PROVIDER.rollDown(INFLATION, TIME_SHIFT);
    double[] t = {0.0, 0.71, 2.00, 25.00 };
    int nbTime = t.length;
    for (int loopt = 0; loopt < nbTime; loopt++) {
      double dfComputed = inflationTimeShift.getDiscountFactor(USD, t[loopt]);
      double rExpected = INFLATION.getCurve(USD).getInterestRate(t[loopt] + TIME_SHIFT);
      double dfExpected = Math.exp(-rExpected * t[loopt]);
      assertEquals("CurveProviderConstantSpreadRolldownFunction: discount factor",
          dfComputed, dfExpected, TOLERANCE_DF);
      double priceComputed = inflationTimeShift.getPriceIndex(CPI, t[loopt]);
      double priceExpected = INFLATION.getPriceIndex(CPI, t[loopt] + TIME_SHIFT);
      assertEquals("CurveProviderConstantSpreadRolldownFunction: price index",
          priceExpected, priceComputed, TOLERANCE_PRICE);
    }
  }
  
  @Test
  public void inflationIssuerProvider() {
    InflationIssuerProviderDiscount inflationIssuerTimeShift =
        (InflationIssuerProviderDiscount) ROLLDOWN_PROVIDER.rollDown(INFLATION_ISSUER, TIME_SHIFT);
    double[] t = {0.0, 0.71, 2.00, 25.00 };
    int nbTime = t.length;
    for (int loopt = 0; loopt < nbTime; loopt++) {
      double dfComputed = inflationIssuerTimeShift.getDiscountFactor(USD, t[loopt]);
      double rExpected = INFLATION_ISSUER.getCurve(USD).getInterestRate(t[loopt] + TIME_SHIFT);
      double dfExpected = Math.exp(-rExpected * t[loopt]);
      assertEquals("CurveProviderConstantSpreadRolldownFunction: discount factor",
          dfExpected, dfComputed, TOLERANCE_DF);
      double priceComputed = inflationIssuerTimeShift.getPriceIndex(CPI, t[loopt]);
      double priceExpected = INFLATION_ISSUER.getPriceIndex(CPI, t[loopt] + TIME_SHIFT);
      assertEquals("CurveProviderConstantSpreadRolldownFunction: price index",
          priceExpected, priceComputed, TOLERANCE_PRICE);
      double dfIssuerComputed = inflationIssuerTimeShift.getDiscountFactor(ISSUER_ENTITY, t[loopt]);
      double rIssuerExpected = INFLATION_ISSUER.getCurve(ISSUER_ENTITY).getInterestRate(t[loopt] + TIME_SHIFT);
      double dfIssuerExpected = Math.exp(-rIssuerExpected * t[loopt]);
      assertEquals("CurveProviderConstantSpreadRolldownFunction: discount factor issuer",
          dfIssuerExpected, dfIssuerComputed, TOLERANCE_DF);
    }
  }
  
}
