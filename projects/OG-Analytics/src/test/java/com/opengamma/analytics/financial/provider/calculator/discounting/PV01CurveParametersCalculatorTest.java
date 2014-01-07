/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurveUtils;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tests PV01 and gamma PV01 calculations.
 */
@Test(groups = TestGroup.UNIT)
public class PV01CurveParametersCalculatorTest {
  /** Provides discounting and forward Ibor curves */
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  /** Provides discounting and forward Ibor curves that have been shifted by 1bp */
  private static final MulticurveProviderDiscount SHIFTED;
  /** Gets the Ibor indices */
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  /** The 3m USD Libor index */
  private static final IborIndex USDLIBOR3M = INDEX_LIST[2];
  /** The 6m USD Libor index */
  private static final IborIndex USDLIBOR6M = INDEX_LIST[3];
  /** NYC calendar */
  private static final Calendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();
  /** Generates fixed / ibor swaps */
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  /** Generates standard USD swaps */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  /** The swap tenor */
  private static final Period SWAP_TENOR = Period.ofYears(10);
  /** The settlement date */
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 5, 17);
  /** The reference date */
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 17);
  /** The notional */
  private static final double NOTIONAL = 100000000;
  /** The fixed rate */
  private static final double FIXED_RATE = 0.025;
  /** A fixed / Libor swap */
  private static final Swap<?, ?> SWAP_FIXED_IBOR = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, FIXED_RATE, true).toDerivative(REFERENCE_DATE);
  /** A 3m Libor / 6m Libor swap */
  private static final Swap<?, ?> SWAP_IBORSPREAD_IBORSPREAD_DEFINITION = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL,
      USDLIBOR3M, 0.001, true, NYC), AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, 0.001, false, NYC)).toDerivative(REFERENCE_DATE);
  /** The PV calculator */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyAmount> PV = PresentValueDiscountingCalculator.getInstance();
  /** The PV01 calculator */
  private static final PV01CurveParametersCalculator<MulticurveProviderInterface> PV01 = new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance());
  /** The gamma PV01 calculator */
  private static final GammaPV01CurveParametersCalculator<MulticurveProviderInterface> GAMMA_PV01 = new GammaPV01CurveParametersCalculator<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance());
  /** One basis point */
  private static final double BP = 0.0001;
  /** Relative accuracy for calculations */
  private static final double EPS_FIRST = 1e-3;

  static {
    final Map<Currency, YieldAndDiscountCurve> discountCurves = new LinkedHashMap<>();
    for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : MULTICURVES.getDiscountingCurves().entrySet()) {
      discountCurves.put(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP, ShiftType.ABSOLUTE));
    }
    final Map<IborIndex, YieldAndDiscountCurve> iborCurves = new LinkedHashMap<>();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : MULTICURVES.getForwardIborCurves().entrySet()) {
      iborCurves.put(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP, ShiftType.ABSOLUTE));
    }
    final Map<IndexON, YieldAndDiscountCurve> overnightCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : MULTICURVES.getForwardONCurves().entrySet()) {
      overnightCurves.put(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP, ShiftType.ABSOLUTE));
    }
    SHIFTED = new MulticurveProviderDiscount(discountCurves, iborCurves, overnightCurves, MULTICURVES.getFxRates());
  }

  /**
   * Tests that the correct exception is thrown when the curve sensitivity calculator is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveSensitivityCalculator1() {
    new PV01CurveParametersCalculator<>(null);
  }

  /**
   * Tests that the correct exception is thrown when the curve sensitivity calculator is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveSensitivityCalculator2() {
    new GammaPV01CurveParametersCalculator<>(null);
  }

  /**
   * Tests that the correct exception is thrown when the instrument passed in is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument1() {
    PV01.visit(null, MULTICURVES);
  }

  /**
   * Tests that the correct exception is thrown when the instrument passed in is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument2() {
    GAMMA_PV01.visit(null, MULTICURVES);
  }

  /**
   * Tests that the correct exception is thrown when the curve data passed in is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    PV01.visit(SWAP_FIXED_IBOR, null);
  }

  /**
   * Tests that the correct exception is thrown when the curve data passed in is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    GAMMA_PV01.visit(SWAP_FIXED_IBOR, null);
  }

  /**
   * Tests that the correct exception is thrown with the wrong visit() method is used
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testVisit1() {
    PV01.visit(SWAP_FIXED_IBOR);
  }

  /**
   * Tests that the correct exception is thrown with the wrong visit() method is used
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testVisit2() {
    GAMMA_PV01.visit(SWAP_FIXED_IBOR);
  }

  /**
   * Tests that total PV01 (i.e. PV01 and gamma PV01) for swaps is a good approximation to the
   * change in PV when all curves to which the instruments are sensitive are shifted by +1bp
   */
  @Test
  public void testApproximation() {
    ReferenceAmount<Pair<String, Currency>> pv01s = SWAP_FIXED_IBOR.accept(PV01, MULTICURVES);
    double pv01 = 0;
    for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01s.getMap().entrySet()) {
      if (entry.getKey().getSecond().equals(Currency.USD)) {
        pv01 += entry.getValue();
      }
    }
    ReferenceAmount<Pair<String, Currency>> pv01Ups = SWAP_FIXED_IBOR.accept(PV01, SHIFTED);
    double pv01Up = 0;
    for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01Ups.getMap().entrySet()) {
      if (entry.getKey().getSecond().equals(Currency.USD)) {
        pv01Up += entry.getValue();
      }
    }
    double gammaPV01 = SWAP_FIXED_IBOR.accept(GAMMA_PV01, MULTICURVES);
    MultipleCurrencyAmount pv = SWAP_FIXED_IBOR.accept(PV, MULTICURVES);
    MultipleCurrencyAmount pvUp = SWAP_FIXED_IBOR.accept(PV, SHIFTED);
    double expectedPV01 = pvUp.getAmount(Currency.USD) - pv.getAmount(Currency.USD);
    assertEquals(0, (expectedPV01 - pv01) / expectedPV01, EPS_FIRST);
    double expectedGammaPV01 = (pv01Up - pv01) / BP;
    assertEquals(0, (expectedGammaPV01 - gammaPV01) / expectedGammaPV01, EPS_FIRST);

    pv01s = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.accept(PV01, MULTICURVES);
    pv01 = 0;
    for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01s.getMap().entrySet()) {
      if (entry.getKey().getSecond().equals(Currency.USD)) {
        pv01 += entry.getValue();
      }
    }
    pv01Ups = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.accept(PV01, SHIFTED);
    pv01Up = 0;
    for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01Ups.getMap().entrySet()) {
      if (entry.getKey().getSecond().equals(Currency.USD)) {
        pv01Up += entry.getValue();
      }
    }
    gammaPV01 = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.accept(GAMMA_PV01, MULTICURVES);
    pv = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.accept(PV, MULTICURVES);
    pvUp = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.accept(PV, SHIFTED);
    expectedPV01 = pvUp.getAmount(Currency.USD) - pv.getAmount(Currency.USD);
    assertEquals(0, (expectedPV01 - pv01) / expectedPV01, EPS_FIRST);
    expectedGammaPV01 = (pv01Up - pv01) / BP;
    assertEquals(0, (expectedGammaPV01 - gammaPV01) / expectedGammaPV01, EPS_FIRST);
  }
}
