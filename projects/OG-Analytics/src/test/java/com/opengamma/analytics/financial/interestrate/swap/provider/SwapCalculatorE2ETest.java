package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegOnAaMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapSingleCurrency;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the Swap discounting method with standard data.
 * Demo test - worked-out example on how to use OG-Analytics library for compute standard measure to simple instruments. 
 * The data is hard-coded. It is also available in some integration unit test and in snapshots.
 */
@Test(groups = TestGroup.UNIT)
public class SwapCalculatorE2ETest {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 1, 22);
  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL1L3L6();
  private static final IborIndex USDLIBOR1M = INDEX_IBOR_LIST[0];
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_LIST[1];
  private static final IborIndex USDLIBOR6M = INDEX_IBOR_LIST[2];
  private static final Calendar NYC = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final Currency USD = USDLIBOR3M.getCurrency();
  /** Calculators */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = 
      new MarketQuoteSensitivityBlockCalculator<>(PSC);
  /** Curve providers */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_OIS_PAIR = 
      StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE_OIS = MULTICURVE_OIS_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_OIS = MULTICURVE_OIS_PAIR.getSecond();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FF_PAIR = 
      StandardDataSetsMulticurveUSD.getCurvesUSDOisFFL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE_FFS = MULTICURVE_FF_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_FFS = MULTICURVE_FF_PAIR.getSecond();
  /** Standard market conventions */
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_SWAP_FIXED_ONCMP_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR1M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR1M", NYC);
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final GeneratorSwapFixedON USD1YFEDFUND = GENERATOR_SWAP_FIXED_ONCMP_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final GeneratorLegIbor LEG_USDLIBOR3M =  GeneratorLegIborMaster.getInstance().getGenerator("USDLIBOR3M", NYC);
  private static final GeneratorLegIbor LEG_USDLIBOR6M =  GeneratorLegIborMaster.getInstance().getGenerator("USDLIBOR6M", NYC);
  private static final GeneratorSwapSingleCurrency USDFFAA3MLIBOR3M = new GeneratorSwapSingleCurrency("USDFEDFUNDAA3MLIBOR3M",
      GeneratorLegOnAaMaster.getInstance().getGenerator("USDFEDFUNDAA3M", NYC),LEG_USDLIBOR3M);
  /** Fixing data */
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR3M = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 10), DateUtils.getUTCDate(2013, 12, 12) }, 
          new double[] {0.0024185, 0.0100 });
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR6M = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 10), DateUtils.getUTCDate(2013, 12, 12) }, 
          new double[] {0.0030, 0.0035 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M_USDLIBOR6M = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M, TS_USDLIBOR6M };
  /** Instruments descriptions */
  private static final double NOTIONAL = 100000000; //100 m
  // Instrument description: Swap Fixed vs ON Cmp
  private static final ZonedDateTime TRADE_DATE_ON = DateUtils.getUTCDate(2014, 2, 3);
  private static final Period TENOR_SWAP_ON = Period.ofMonths(2);
  private static final double FIXED_RATE_ON = 0.00123;
  private static final GeneratorAttributeIR ATTRIBUTE_ON = new GeneratorAttributeIR(TENOR_SWAP_ON);
  private static final SwapDefinition SWAP_FIXED_ON_DEFINITION = 
      USD1YFEDFUND.generateInstrument(TRADE_DATE_ON, FIXED_RATE_ON, NOTIONAL, ATTRIBUTE_ON);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_ON = SWAP_FIXED_ON_DEFINITION.toDerivative(VALUATION_DATE);
  // Instrument description: Swap ON AA vs Libor 3M
  private static final ZonedDateTime TRADE_DATE_FF = DateUtils.getUTCDate(2014, 9, 10);
  private static final Period TENOR_SWAP_FF = Period.ofYears(6);
  private static final double SPREAD_FF = 0.0025;
  private static final GeneratorAttributeIR ATTRIBUTE_FF = new GeneratorAttributeIR(TENOR_SWAP_FF);
  private static final SwapDefinition SWAP_FF_3M_0_DEFINITION = 
      USDFFAA3MLIBOR3M.generateInstrument(TRADE_DATE_FF, 0.0, NOTIONAL, ATTRIBUTE_FF);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FF_3M_0 = SWAP_FF_3M_0_DEFINITION.toDerivative(VALUATION_DATE);
  private static final SwapDefinition SWAP_FF_3M_DEFINITION = 
      USDFFAA3MLIBOR3M.generateInstrument(TRADE_DATE_FF, SPREAD_FF, NOTIONAL, ATTRIBUTE_FF);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FF_3M = SWAP_FF_3M_DEFINITION.toDerivative(VALUATION_DATE);
  // Instrument description: Swap Fixed vs Libor3M
  private static final ZonedDateTime TRADE_DATE_3M = DateUtils.getUTCDate(2014, 9, 10);
  private static final Period TENOR_SWAP_3M = Period.ofYears(7);
  private static final double FIXED_RATE_3M = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M = new GeneratorAttributeIR(TENOR_SWAP_3M);
  private static final SwapDefinition SWAP_FIXED_3M_DEFINITION = 
      USD6MLIBOR3M.generateInstrument(TRADE_DATE_3M, FIXED_RATE_3M, NOTIONAL, ATTRIBUTE_3M);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_3M = SWAP_FIXED_3M_DEFINITION.toDerivative(VALUATION_DATE);
  // Instrument description: Swap Fixed vs Libor1M
  private static final ZonedDateTime TRADE_DATE_1M = DateUtils.getUTCDate(2014, 9, 10);
  private static final Period TENOR_SWAP_1M = Period.ofYears(2);
  private static final double FIXED_RATE_1M = 0.0125;
  private static final GeneratorAttributeIR ATTRIBUTE_1M = new GeneratorAttributeIR(TENOR_SWAP_1M);
  private static final SwapDefinition SWAP_FIXED_1M_DEFINITION = 
      USD6MLIBOR1M.generateInstrument(TRADE_DATE_1M, FIXED_RATE_1M, NOTIONAL, ATTRIBUTE_1M);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_1M = SWAP_FIXED_1M_DEFINITION.toDerivative(VALUATION_DATE);
  // Instrument description: Swap Fixed vs Libor3M Already started (with fixing)
  private static final ZonedDateTime TRADE_DATE_3M_S = DateUtils.getUTCDate(2013, 9, 10);
  private static final Period TENOR_SWAP_3M_S = Period.ofYears(7);
  private static final double FIXED_RATE_3M_S = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M_S = new GeneratorAttributeIR(TENOR_SWAP_3M_S);
  private static final SwapFixedIborDefinition SWAP_FIXED_3M_S_DEFINITION = 
      USD6MLIBOR3M.generateInstrument(TRADE_DATE_3M_S, FIXED_RATE_3M_S, NOTIONAL, ATTRIBUTE_3M_S);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_3M_S = 
      SWAP_FIXED_3M_S_DEFINITION.toDerivative(VALUATION_DATE, TS_ARRAY_USDLIBOR3M);
  // Instrument description: Swap Libor3M+S vs Libor6M
  private static final ZonedDateTime TRADE_DATE_3M_6M_BS = DateUtils.getUTCDate(2014, 8, 27);
  private static final Period TENOR_SWAP_3M_6M_BS = Period.ofYears(10);
  private static final double SPREAD_3M_6M_BS = 0.0010;
  private static final GeneratorAttributeIR ATTRIBUTE_3M_6M_BS = new GeneratorAttributeIR(TENOR_SWAP_3M_6M_BS);
  private static final AnnuityDefinition<?> LEG_3M_S = 
      LEG_USDLIBOR3M.generateInstrument(TRADE_DATE_3M_6M_BS, SPREAD_3M_6M_BS, NOTIONAL, ATTRIBUTE_3M_6M_BS); // Receiver
  private static final AnnuityDefinition<?> LEG_6M = 
      LEG_USDLIBOR6M.generateInstrument(TRADE_DATE_3M_6M_BS, 0.0, -NOTIONAL, ATTRIBUTE_3M_6M_BS); // Payer
  private static final SwapDefinition BS_3M_S_6M_DEFINITION = new SwapDefinition(LEG_3M_S, LEG_6M);
  private static final Swap<? extends Payment, ? extends Payment> BS_3M_S_6M = 
      BS_3M_S_6M_DEFINITION.toDerivative(VALUATION_DATE, TS_ARRAY_USDLIBOR3M_USDLIBOR6M);
  
  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-4;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double BP1 = 1.0E-4;

  @Test
  /** Tests present value for a swap fixed vs ON compounded. */
  public void presentValueONCmp() {
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_ON.accept(PVDC, MULTICURVE_OIS);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, -9723.264518929138);
    assertEquals("Swap Fixed v ON compounded: present value from standard curves", 
        pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Tests forward rate for a swap fixed vs Fed Fund compounded. */
  public void parRateONCmp() {
    final double parRate = SWAP_FIXED_ON.accept(PRDC, MULTICURVE_OIS);
    final double parRateExpected = 6.560723881400023E-4;
    assertEquals("Swap Fixed v ON compounded: par rate from standard curves", parRateExpected, parRate, TOLERANCE_RATE);
  }

  @Test
  /** Tests present value of ON Arithmetic Average (+ spread) vs Libor3M swaps. */
  public void presentValueONAA3M() {
    final MultipleCurrencyAmount pv0Computed = SWAP_FF_3M_0.accept(PVDC, MULTICURVE_OIS); // Spread = 0
    final MultipleCurrencyAmount pv0Expected = MultipleCurrencyAmount.of(Currency.USD, -1617070.5290);
    assertEquals("Swap ON Arithmetic Average: present value", 
        pv0Expected.getAmount(USD), pv0Computed.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount pvComputed = SWAP_FF_3M.accept(PVDC, MULTICURVE_OIS); // Spread 
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, -159794.3224);
    assertEquals("Swap ON Arithmetic Average: present value", 
        pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Tests par spread for ON Arithmetic Average (+ spread) vs Libor3M swaps. */
  public void parSpreadMarketQuoteONAA3M() {
    final double parSpread = SWAP_FF_3M_0.accept(PSMQDC, MULTICURVE_OIS);
    final double parSpreadExpected = 0.0027741318;
    assertEquals("Swap ON Arithmetic Average: par spread", parSpreadExpected, parSpread, TOLERANCE_RATE);
  }

  @Test
  /** Tests Bucketed PV01 of ON Arithmetic Average (+ spread) vs Libor3M swaps. */
  public void BucketedPV01ONAA3M() {
    final double[] deltaDsc = {-0.53657, -0.53657, 1.50563, -20.38650, 156.45965, 
      -3141.24234, -3355.04638, -13.91364, 42.96362, 61.31768, 
      44.65782, -104.16588, 23501.58783, 40086.81647, 0.00000, 
      0.00000, 0.00000};
    final double[] deltaFwd3 = {2605.09563, 2632.09625, 1176.12448, -27.11358, 33.63026,
      6.79543, 7.46515, -11436.33187, -52207.12832, 0.00000,
      0.00000, 0.00000, 0.00000, 0.00000, 0.00000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SWAP_FF_3M, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Swap ON Arithmetic Average: bucketed delts from standard curves", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  /** Test present value for a swap fixed vs LIBOR3M. Curves with OIS and curves with Fed Fund swaps.*/
  public void presentValue3M() {
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_3M.accept(PVDC, MULTICURVE_OIS);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 7170391.798257509);
    assertEquals("IRS Fixed v LIBOR3M: present value - OIS based curves", 
        pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount pvComputed2 = SWAP_FIXED_3M.accept(PVDC, MULTICURVE_FFS);
    final MultipleCurrencyAmount pvExpected2 = MultipleCurrencyAmount.of(Currency.USD, 6065111.8810);
    assertEquals("IRS Fixed v LIBOR3M: present value - Fed Fund swap based curve", 
        pvExpected2.getAmount(USD), pvComputed2.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Test present value for a swap fixed vs LIBOR3M. */
  public void presentValue3MWithFixing() {
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_3M_S.accept(PVDC, MULTICURVE_OIS);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 3588376.471608199);
    assertEquals("IRS Fixed v LIBOR3M: present value", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Test forward rate  for a swap fixed vs LIBOR3M. Curves with OIS and curves with Fed Fund swaps. */
  public void parRate3M() {
    final double parRate = SWAP_FIXED_3M.accept(PRDC, MULTICURVE_OIS);
    final double parRateExpected = 0.025894715668195054;
    assertEquals("IRS Fixed v LIBOR3M: par rate", parRateExpected, parRate, TOLERANCE_RATE);
    final double parRate2 = SWAP_FIXED_3M.accept(PRDC, MULTICURVE_FFS);
    final double parRateExpected2 = 0.024262727477023297;
    assertEquals("IRS Fixed v LIBOR3M: par rate", parRateExpected2, parRate2, TOLERANCE_RATE);
  }

  @Test
  /** Test Bucketed PV01 for a swap fixed vs LIBOR3M. Curves with OIS and curves with Fed Fund swaps. */
  public void BucketedPV01IRS3M() {
    final double[] deltaDsc = 
      {-2.0061282888005487, -2.0061296819291816, -8.67452075363044E-5, 0.0011745459201512494, 1.4847039752079148, 
      -56.9491079838621, 1.1272953888594144, -86.07354102781184, -166.96224129263487, -242.22201138850485, 
      -314.19406010048203, -385.9029177491706, -463.2762183477875, -979.7315575792289, -243.35533439972858, 
      243.5314114568193, 139.99052652789604 };
    final double[] deltaFwd3 = 
      {-2604.935862485693, -2632.099517240374, -1176.1264079094185, 27.132459446981603, -34.136228550265635, 
      -8.299063015802915, -10.516911338517652, 0.5088197130590212, 56648.04062948109, 15520.134985155655, 
      0.00, 0.00, 0.00, 0.00, 0.00 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = 
        MQSBC.fromInstrument(SWAP_FIXED_3M, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("IRS Fixed v LIBOR3M: bucketed deltas", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
    final double[] deltaDsc2 = 
      {-3.24106, -0.17486, 0.08672, 0.76798, -49.17171, 
      2.42819, -70.15064, 140.71112, 183.35751, 319.67396, 
      360.95413, 157.18368, 844.78287, 226.54263, -234.85103, 
      57.18048, 28.57051, -9.64710, 2.37465, -0.76109, 
      0.14930 };
    final double[] deltaFwd32 = 
      {-2545.40838, -4467.09828, 1217.58588, -3009.98103, 9615.49437, 
      -20168.36507, 70744.04947, 17070.26570, -5800.63056, 848.03566, 
      -129.99027, 34.28334, -5.96436 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity2 = new LinkedHashMap<>();
    sensitivity2.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDsc2));
    sensitivity2.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd32));
    final MultipleCurrencyParameterSensitivity pvpsExpected2 = new MultipleCurrencyParameterSensitivity(sensitivity2);
    final MultipleCurrencyParameterSensitivity pvpsComputed2 = 
        MQSBC.fromInstrument(SWAP_FIXED_3M, MULTICURVE_FFS, BLOCK_FFS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("IRS Fixed v LIBOR3M: bucketed deltas", 
        pvpsExpected2, pvpsComputed2, TOLERANCE_PV_DELTA);
  }

  @Test
  /** Test present value for a swap LIBOR3M + Spread V LIBOR6M. */
  public void presentValue3M6M() {
    final MultipleCurrencyAmount pvComputed = BS_3M_S_6M.accept(PVDC, MULTICURVE_OIS);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, -13844.3872);
    assertEquals("Basis swap L3M v L6M: present value", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Test Bucketed PV01 for a swap LIBOR3M + Spread V LIBOR6M. */
  public void BucketedPV01BS3M6M() {
    final double[] deltaDsc = 
      {0.0052, 0.0052, -0.0001, 0.0016, -0.0149,
      0.7604, -5.5533, -2.9892, -22.2481, -37.7274, 
      -41.8728, -24.0496, -43.6660, 12.2553, -31.6188, -49.6446, 598.0190 };
    final double[] deltaFwd3 = 
      {-2591.4459, -2619.3357, -812.9288, 1.3646, 3.5382, 
      36.6063, 54.6148, -33.8631, 185.7332, -429.3245, 
      -186.3951, -26.0288, 0.0000, 0.0000, 0.0000 }; // Swap 6M curve as spread to 3M.
    final double[] deltaFwd6 = 
      {4442.0847, 1584.5644, -101.6016, 80.0868, -1.6158, 
      3.4711, 0.4096, 52.3150, -68443.8927, -28842.1969, 
      0.0000, 0.0000, 0.0000, 0.0000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR6M), USD), new DoubleMatrix1D(deltaFwd6));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(BS_3M_S_6M, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Basis swap L3M v L6M: bucketed deltas", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  /**Test present value for a swap fixed vs LIBOR1M. */
  public void presentValue1M() {
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_1M.accept(PVDC, MULTICURVE_OIS);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, -1003685.179128858);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", 
        pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Test forward rate for a swap fixed vs LIBOR1M. */
  public void parRate1M() {
    final double parRate = SWAP_FIXED_1M.accept(PRDC, MULTICURVE_OIS);
    final double parRateExpected = 0.007452504182638092;
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateExpected, parRate, TOLERANCE_RATE);
  }

  @Test
  /** Test Bucketed PV01 for a swap fixed vs LIBOR1M. */
  public void BucketedPV011M() {
    final double[] deltaDsc = 
      {0.30079551275104416, 0.30079572164276736, -9.585961874740465E-6, 1.2979495579621574E-4, 1.5085871713580485,
      -13.566109046684943, 0.09026843918435334, 45.96990975622252, 99.74522348776304, 104.85108270307225, 
      -9.33773534893459E-11, -4.285397912505579E-12, 0.00, 0.00, 0.00, 0.00, 0.00 };
    final double[] deltaFwd1 = 
      {-0.20863786628281816, 2887.648010427227, 3524.8181060609513, 54.75432367092116, -9894.416325570519, 
      -16771.99913018682, -3.0220503938933227E-10, 3.729948495906336E-10, 1.6330782253589604E-10, -8.986191325519167E-11,
      0.00, 0.00, 0.00, 0.00, 0.00 };
    final double[] deltaFwd3 = 
      {-2597.896012855518, -2626.224124335432, -1187.3995581915851, -53.9916796422252, 9752.524496704595, 
      16503.81428148996, 4.871798063348056E-10, -6.672030279745711E-10, -1.7934130597452707E-10, 1.7682040814394901E-10,
      0.00, 0.00, 0.00, 0.00, 0.00 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR1M), USD), new DoubleMatrix1D(deltaFwd1));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SWAP_FIXED_1M, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed delts from standard curves", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

}
