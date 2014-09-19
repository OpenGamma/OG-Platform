/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSDGBP;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the Swap discounting method with standard data for cross-currency instruments.
 * Demo test - worked-out example on how to use OG-Analytics library for compute standard measure to simple instruments. 
 * The data is hard-coded. It is also available in some integration unit test and in snapshots.
 */
@Test(groups = TestGroup.UNIT)
public class SwapCrossCurrencyE2ETest {  

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 1, 22);

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR = 
      new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());

  /** Curve providers */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FFCOL_PAIR = 
      StandardDataSetsMulticurveUSDGBP.getCurvesUSDOisL1L3L6(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_FFCOL = MULTICURVE_FFCOL_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_FFCOL = MULTICURVE_FFCOL_PAIR.getSecond();
  
  /** Calculators */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;
  
  /** USD Fixed v USDLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2016, 7, 18);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2021, 7, 18);
  private static final double FIXED_RATE_1 = 0.02655;
  private static final boolean PAYER_1 = false;
  private static final double NOTIONAL_1 = 1000000; // 1m
  private static final SwapCouponFixedCouponDefinition IRS_USD_1_DEFINITION =
      irsUsd(EFFECTIVE_DATE_1, MATURITY_DATE_1, FIXED_RATE_1, PAYER_1, NOTIONAL_1);
  private static final SwapFixedCoupon<Coupon> IRS_USD_1 = 
      IRS_USD_1_DEFINITION.toDerivative(VALUATION_DATE);

  @Test
  public void presentValueUsd() {
    MultipleCurrencyAmount pvIrsUsd1Expected = MultipleCurrencyAmount.of(USD, -10920.1548);
    MultipleCurrencyAmount pvIrsUsd1 = IRS_USD_1.accept(PVDC, MULTICURVE_FFCOL);
    assertEquals("SwapCrossCurrencyE2E", pvIrsUsd1Expected.getAmount(USD), pvIrsUsd1.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void parRateUsd() {
    double prIrsUsd1Expected = 0.0289290855;
    double prIrsUsd1 = IRS_USD_1.accept(PRDC, MULTICURVE_FFCOL);
    assertEquals("SwapCrossCurrencyE2ETest", prIrsUsd1Expected, prIrsUsd1, TOLERANCE_RATE);
    SwapCouponFixedCouponDefinition irsAtmDefinition = irsUsd(EFFECTIVE_DATE_1, MATURITY_DATE_1, prIrsUsd1, PAYER_1, NOTIONAL_1);
    SwapFixedCoupon<Coupon> irsAtm = irsAtmDefinition.toDerivative(VALUATION_DATE);
    MultipleCurrencyAmount pvIrsUsd1Atm = irsAtm.accept(PVDC, MULTICURVE_FFCOL);
    assertEquals("SwapCrossCurrencyE2E", 0.0, pvIrsUsd1Atm.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void parSpreaMarketQuoteUsd() {
    double psIrsUsd1Expected = 0.0023790855;
    double psIrsUsd1 = IRS_USD_1.accept(PSMQDC, MULTICURVE_FFCOL);
    assertEquals("SwapCrossCurrencyE2ETest", psIrsUsd1Expected, psIrsUsd1, TOLERANCE_RATE);
    SwapCouponFixedCouponDefinition irsAtmDefinition = irsUsd(EFFECTIVE_DATE_1, MATURITY_DATE_1,
        FIXED_RATE_1 + psIrsUsd1, PAYER_1, NOTIONAL_1);
    SwapFixedCoupon<Coupon> irsAtm = irsAtmDefinition.toDerivative(VALUATION_DATE);
    MultipleCurrencyAmount pvIrsUsd1Atm = irsAtm.accept(PVDC, MULTICURVE_FFCOL);
    assertEquals("SwapCrossCurrencyE2E", 0.0, pvIrsUsd1Atm.getAmount(USD), TOLERANCE_PV);
  }

  private static SwapCouponFixedCouponDefinition irsUsd(final LocalDate effectiveDate, final LocalDate maturityDate, 
      final double fixedRate, boolean payer, final double notional) {
    NotionalProvider notionalProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notional;
      }
    };
    // Fixed leg
    AnnuityDefinition<?> legFixedGenDefinition = new FixedAnnuityDefinitionBuilder().
        payer(payer).currency(USD6MLIBOR3M.getCurrency()).notional(notionalProvider).startDate(effectiveDate).
        endDate(maturityDate).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
        accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(fixedRate).
        accrualPeriodParameters(ADJUSTED_DATE_LIBOR).build();
    AnnuityCouponFixedDefinition legFixedDefinition =
        new AnnuityCouponFixedDefinition((CouponFixedDefinition[]) legFixedGenDefinition.getPayments(), NYC);
    AnnuityDefinition<? extends CouponDefinition> legIborDefinition = 
        (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(!payer).
            notional(notionalProvider).startDate(effectiveDate).endDate(maturityDate).index(USDLIBOR3M).
            accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
            resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
            dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).
            currency(USDLIBOR3M.getCurrency()).build();
    return new SwapCouponFixedCouponDefinition(legFixedDefinition, legIborDefinition);
  }
  
}
