/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsInflationGBP;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the zero coupon inflation discounting method.
 */
@Test(groups = TestGroup.UNIT)
public class SwapInflationZeroCouponDiscountingMethodE2ETest {

  private static final IndexPrice[] INDEX_IBOR_LIST = StandardDataSetsInflationGBP.indexONArray();
  private static final IndexPrice GBP_RPI_PRICE_INDEX = INDEX_IBOR_LIST[0];
  private static final Calendar CALENDAR = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final Currency CUR = GBP_RPI_PRICE_INDEX.getCurrency();

  private static final PresentValueDiscountingInflationCalculator PVDIC = PresentValueDiscountingInflationCalculator.getInstance();

  // Test with standard data - harcoded numbers
  private static final ZonedDateTime STD_REFERENCE_DATE = DateUtils.getUTCDate(2014, 4, 11);
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_STD = StandardDataSetsInflationGBP.getCurvesGBPRpiAndSonia();
  private static final InflationProviderDiscount INFLATION_MULTICURVE_STD = MULTICURVE_PAIR_STD.getFirst();
  // Instrument description
  private static final ZonedDateTime STD_ACCRUAL_START_DATE = DateUtils.getUTCDate(2014, 4, 2);
  private static final double STD_NOTIONAL = 10000000; //-10m
  private static final GeneratorAttributeIR RPI_GBP_ATTR = new GeneratorAttributeIR(Period.ofYears(5));
  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_INFLATION_SWAP = GeneratorSwapFixedInflationMaster.getInstance().getGenerator("UKRPI");
  private static final double RATE_FIXED_LEG = 0.02506;
  private static final SwapFixedInflationZeroCouponDefinition SWAP_DEFINITION = GENERATOR_INFLATION_SWAP.generateInstrument(STD_ACCRUAL_START_DATE, RATE_FIXED_LEG, STD_NOTIONAL, RPI_GBP_ATTR);
  private static final ZonedDateTimeDoubleTimeSeries TS_PRICE_INDEX_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 31),
    DateUtils.getUTCDate(2014, 1, 31), DateUtils.getUTCDate(2014, 2, 28) }, new double[] {253.4, 252.6, 254.2 });
  private static final InstrumentDerivative SWAP = convert(SWAP_DEFINITION);
  // Data

  private static final double STD_TOLERANCE_PV = 1.0e-2;

  private static InstrumentDerivative convert(final InstrumentDefinition<?> instrument) {
    InstrumentDerivative ird;
    if (instrument instanceof SwapFixedInflationZeroCouponDefinition) {
      final Annuity<? extends Payment> ird1 = ((SwapFixedInflationZeroCouponDefinition) instrument).getFirstLeg().toDerivative(STD_REFERENCE_DATE);
      final Annuity<? extends Payment> ird2 = ((SwapFixedInflationZeroCouponDefinition) instrument).getSecondLeg().toDerivative(STD_REFERENCE_DATE, TS_PRICE_INDEX_USD_WITH_TODAY);
      ird = new Swap<>(ird1, ird2);
    }
    else {
      ird = instrument.toDerivative(STD_REFERENCE_DATE);
    }
    return ird;
  }

  @Test
  /**
   * Test different results with a standard set of data against hardcoded values. Can be used for platform testing or regression testing.
   */
  public void presentValue() {
    // Present Value
    final MultipleCurrencyAmount pvComputed = SWAP.accept(PVDIC, INFLATION_MULTICURVE_STD);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.GBP, -21922.072817862267);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(CUR), pvComputed.getAmount(CUR), 5.00);
  }

}
