/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsInflationUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesInflationDataSets;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * End-to-end tests for inflation curve calibration and pricing of inflation zero-coupon swaps.
 */
public class SwapZeroCouponInflationUsdDiscountingE2ETest {
  

  private static final ZonedDateTime CAlIBRATION_DATE = DateUtils.getUTCDate(2014, 10, 9);
  private static final Currency USD = Currency.USD;

  /** Calculators **/
  private static final PresentValueDiscountingInflationCalculator PVDIC = 
      PresentValueDiscountingInflationCalculator.getInstance();
  
  /** Curves */
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisHicp(CAlIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL = MULTICURVE_INFL_PAIR.getFirst();
  private static final ZonedDateTimeDoubleTimeSeries HTS_CPI = 
      StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(CAlIBRATION_DATE);
  
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2014, 4, 2);
  private static final double NOTIONAL = 10_000_000;
  private static final GeneratorAttributeIR RPI_GBP_ATTR = new GeneratorAttributeIR(Period.ofYears(5));
  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_INFLATION_SWAP = 
      GeneratorSwapFixedInflationMaster.getInstance().getGenerator("USCPI");
  private static final double RATE_FIXED_LEG = 0.0250;
  private static final SwapFixedInflationZeroCouponDefinition SWAP_DEFINITION = 
      GENERATOR_INFLATION_SWAP.generateInstrument(ACCRUAL_START_DATE, RATE_FIXED_LEG, NOTIONAL, RPI_GBP_ATTR);
  private static final InstrumentDerivative SWAP = SWAP_DEFINITION.toDerivative(ACCRUAL_START_DATE, 
      new ZonedDateTimeDoubleTimeSeries[] {HTS_CPI, HTS_CPI});
  
  private static final double TOLERANCE_PV = 1.0E-2;
  
  @Test
  public void presentValue() {
    double pvExpectd = -75560.5403;
    MultipleCurrencyAmount pv = SWAP.accept(PVDIC, MULTICURVE_INFL);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest", pvExpectd, pv.getAmount(USD), TOLERANCE_PV);
  }
  
}
