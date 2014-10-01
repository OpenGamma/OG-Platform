package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
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
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the Swap discounting method with standard data.
 * Demo test - worked-out example on how to use OG-Analytics library for compute standard measure to simple instruments. 
 * The data is hard-coded. It is also available in some integration unit test and in snapshots.
 */
@Test(groups = TestGroup.UNIT)
public class SwapCalculatorE2ETest {

  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL1L3L6();
  private static final IborIndex USDLIBOR1M = INDEX_IBOR_LIST[0];
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_LIST[1];
  private static final IborIndex USDLIBOR6M = INDEX_IBOR_LIST[2];
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
  
  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-4;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double BP1 = 1.0E-4;
  
  /**
   * Test the present value versus a hard-coded number.
   * @param ins The instrument to test.
   * @param multicurve The multi-curve provider.
   * @param ccy The currency of the expected PV.
   * @param expectedPv The expected PV amount.
   * @param msg The assert message.
   */
  private void presentValueTest(InstrumentDerivative ins, MulticurveProviderDiscount multicurve, Currency ccy, 
      double expectedPv, String msg) {
    MultipleCurrencyAmount pvComputed = ins.accept(PVDC, multicurve);
    assertEquals(msg, expectedPv, pvComputed.getAmount(ccy), TOLERANCE_PV);
  }

  @Test
  /** Tests present value for a swap fixed vs Fed Fund compounded. */
  public void presentValueONCmp() {
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_ON, MULTICURVE_OIS, USD, -9723.264518929138,
        "Swap Fixed v ON compounded: present value from standard curves");
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_ON, MULTICURVE_FFS, USD, -5969.7908,
        "Swap Fixed v ON compounded: present value - Fed Fund swap based curves");
  }

  @Test
  /** Tests present value for a swap fixed vs Fed Fund compounded with fixing. */
  public void presentValueONCmpWithFixing() {
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_ON_S, MULTICURVE_OIS, USD, -7352.973875972721,
        "Swap Fixed v ON compounded: present value from standard curves");
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_ON_S, MULTICURVE_FFS, USD, -5569.499485016839,
        "Swap Fixed v ON compounded: present value from standard curves");
  }

  @Test
  /** Tests present value of ON Arithmetic Average (+ spread) vs Libor3M swaps. */
  public void presentValueONAA3M() {
    presentValueTest(SwapInstrumentsDataSet.SWAP_FF_3M_0, MULTICURVE_OIS, USD, -1617940.0428,
        "Swap ON Arithmetic Average: present value");
    presentValueTest(SwapInstrumentsDataSet.SWAP_FF_3M, MULTICURVE_OIS, USD, -160663.8362,
        "Swap ON Arithmetic Average: present value");
    presentValueTest(SwapInstrumentsDataSet.SWAP_FF_3M_0, MULTICURVE_FFS, USD, -1296763.1943,
        "Swap ON Arithmetic Average: present value");
    presentValueTest(SwapInstrumentsDataSet.SWAP_FF_3M, MULTICURVE_FFS, USD, 150128.4091,
        "Swap ON Arithmetic Average: present value");
  }

  @Test
  /** Test present value for a swap fixed vs LIBOR3M. Curves with OIS and curves with Fed Fund swaps.*/
  public void presentValue3M() {
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_3M, MULTICURVE_OIS, USD, 7170391.798257509,
        "IRS Fixed v LIBOR3M: present value - OIS based curves");
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_3M, MULTICURVE_FFS, USD, 6065111.8810,
        "IRS Fixed v LIBOR3M: present value - Fed Fund swap based curve");
  }

  @Test
  /** Test present value for a swap fixed vs LIBOR3M with fixing. */
  public void presentValue3MWithFixing() {
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_3M_S, MULTICURVE_OIS, USD, 3588376.471608199,
        "IRS Fixed v LIBOR3M: present value - OIS based curves");
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_3M_S, MULTICURVE_FFS, USD, 3193775.0940362737,
        "IRS Fixed v LIBOR3M: present value - Fed Fund swap based curve");
  }

  @Test
  /** Test present value for a swap LIBOR3M + Spread V LIBOR6M. */
  public void presentValue3M6M() {
    presentValueTest(SwapInstrumentsDataSet.BS_3M_S_6M, MULTICURVE_OIS, USD, -13844.3872,
        "Basis swap L3M v L6M: present value");
    presentValueTest(SwapInstrumentsDataSet.BS_3M_S_6M, MULTICURVE_FFS, USD, 72748.9893,
        "Basis swap L3M v L6M: present value");
  }

  @Test
  /** Test present value for a swap LIBOR1M Compounding V LIBOR3M. */
  public void presentValue1MCmp3M() {
    presentValueTest(SwapInstrumentsDataSet.BS_1MCMP_3M, MULTICURVE_OIS, USD, -340415.3431,
        "Basis swap L1MFlat v L3M: present value");
    presentValueTest(SwapInstrumentsDataSet.BS_1MCMP_3M, MULTICURVE_FFS, USD, -534937.1336,
        "Basis swap L1MFlat v L3M: present value");
  }

  @Test
  /** Test present value for a swap LIBOR1M Compounding FLAT + Spread V LIBOR3M. */
  public void presentValue1MSpreadFlat3M() {
    presentValueTest(SwapInstrumentsDataSet.BS_1MCMP_S_3M, MULTICURVE_OIS, USD, 152396.2410,
        "Basis swap L1MFlat v L3M: present value");
    presentValueTest(SwapInstrumentsDataSet.BS_1MCMP_S_3M, MULTICURVE_FFS, USD, -46130.4883,
        "Basis swap L1MFlat v L3M: present value");
  }

  @Test
  /**Test present value for a swap fixed vs LIBOR1M. */
  public void presentValue1M() {
    presentValueTest(SwapInstrumentsDataSet.SWAP_FIXED_1M, MULTICURVE_OIS, USD, -1003685.1791,
        "IRS Fixed v LIBOR1M: present value from standard curves");
  }
  
  @Test
  /**Tests present value for a ON Cmp + spread v ON AA. */
  public void presentValueONCmpONAA() {
    presentValueTest(SwapInstrumentsDataSet.BS_ONCMP_S_ONAA, MULTICURVE_FFS, USD, -507970.1126,
        "Basis swap ON Cmp + spread v ON AA: present value - FF swap based curves");
  }
  
  @Test
  /**Tests present value for an IRS with stub - fixed leg. */
  public void presentValueStub1() {
    presentValueTest(SwapInstrumentsDataSet.IRS_STUB1, MULTICURVE_FFS, USD, -180869.2122,
        "IRS with STUB: present value - FF swap based curves");
  }  
  
  @Test
  /**Tests present value for an IRS with stub - ibor leg / same index */
  public void presentValueStub2() {
    presentValueTest(SwapInstrumentsDataSet.IRS_STUB2, MULTICURVE_FFS, USD, -258994.3839,
        "IRS with STUB: present value - FF swap based curves");
  }  
  
  @Test
  /**Tests present value for an IRS with stub - ibor leg / interpolated index */
  public void presentValueStub3() {
    presentValueTest(SwapInstrumentsDataSet.IRS_STUB3, MULTICURVE_FFS, USD, -319533.7849,
        "IRS with STUB: present value - FF swap based curves");
  }
  
  @Test
  /**Tests present value for an IRS with stub - ibor leg / interpolated index */
  public void presentValueStub4() {
    presentValueTest(SwapInstrumentsDataSet.IRS_STUB4, MULTICURVE_FFS, USD, -405631.5512,
        "IRS with STUB: present value - FF swap based curves");
  }

  @Test(enabled=false) // TODO: reinstall the test when the stub problem [PLAT-6777]
  /**Tests present value for an IRS with stub - ibor leg / interpolated index  - short end*/
  public void presentValueStub5() {
    presentValueTest(SwapInstrumentsDataSet.IRS_STUB5, MULTICURVE_FFS, USD, 0.0,
        "IRS with STUB: present value - FF swap based curves");
  }
  
  @Test(enabled=false) // TODO: reinstall the test when the stub problem [PLAT-6777]
  /**Tests present value for an IRS with stub - ibor leg / interpolated index  - short end*/
  public void presentValueStub6() {
    presentValueTest(SwapInstrumentsDataSet.IRS_STUB6, MULTICURVE_FFS, USD, 0.0,
        "IRS with STUB: present value - FF swap based curves");
  }
  
  /**
   * Test the parrate versus a hard-coded number.
   * @param ins The instrument to test.
   * @param multicurve The multi-curve provider.
   * @param ccy The currency of the expected PV.
   * @param expectedPv The expected PV amount.
   * @param msg The assert message.
   */
  private void parRateTest(InstrumentDerivative ins, MulticurveProviderDiscount multicurve, double prExpected, 
      String msg) {
    double prComputed = ins.accept(PRDC, multicurve);
    assertEquals(msg, prExpected, prComputed, TOLERANCE_RATE);
  }

  @Test
  /** Tests forward rate for a swap fixed vs Fed Fund compounded. */
  public void parRateONCmp() {
    parRateTest(SwapInstrumentsDataSet.SWAP_FIXED_ON, MULTICURVE_OIS, 6.560723881400023E-4, 
        "Swap Fixed v ON compounded: par rate from standard curves");
  }

  @Test
  /** Test forward rate  for a swap fixed vs LIBOR3M. Curves with OIS and curves with Fed Fund swaps. */
  public void parRate3M() {
    parRateTest(SwapInstrumentsDataSet.SWAP_FIXED_3M, MULTICURVE_OIS, 0.025894715668195054, 
        "IRS Fixed v LIBOR3M: par rate");
    parRateTest(SwapInstrumentsDataSet.SWAP_FIXED_3M, MULTICURVE_FFS, 0.024262727477023297, 
        "IRS Fixed v LIBOR3M: par rate");
  }

  @Test
  /** Test forward rate for a swap fixed vs LIBOR1M. */
  public void parRate1M() {
    parRateTest(SwapInstrumentsDataSet.SWAP_FIXED_1M, MULTICURVE_OIS, 0.007452504182638092,
        "IRS Fixed v LIBOR1M: par rate from standard curves");
  }

  @Test
  /** Test forward rate for a swap fixed vs LIBOR3M - stub. */
  public void parRateStub1() {
    parRateTest(SwapInstrumentsDataSet.IRS_STUB1, MULTICURVE_FFS, 0.0110411215,
        "IRS Fixed v LIBOR3M - stub: par rate");
  }

  @Test
  /** Tests par spread for ON Arithmetic Average (+ spread) vs Libor3M swaps. */
  public void parSpreadMarketQuoteONAA3M() {
    final double parSpread = SwapInstrumentsDataSet.SWAP_FF_3M_0.accept(PSMQDC, MULTICURVE_OIS);
    final double parSpreadExpected = 0.0027756235; // 0.0027741318;
    assertEquals("Swap ON Arithmetic Average: par spread", parSpreadExpected, parSpread, TOLERANCE_RATE);
  }

  @Test
  /** Tests par spread for Fixed vs Libor3M swaps. */
  public void parSpreadMarketQuote3M() {
    final double parSpread = SwapInstrumentsDataSet.SWAP_FIXED_3M.accept(PSMQDC, MULTICURVE_OIS);
    final double parSpreadExpected = 0.01089471566819499;
    assertEquals("Fixed vs Libor3M swaps: par spread", parSpreadExpected, parSpread, TOLERANCE_RATE);
  }

  @Test
  /** Tests par spread for swap fixed vs Fed Fund compounded. */
  public void parSpreadMarketQuoteON() {
    final double parSpread = SwapInstrumentsDataSet.SWAP_FIXED_ON.accept(PSMQDC, MULTICURVE_OIS);
    final double parSpreadExpected = -5.739276118599975E-4;
    assertEquals("Fixed vs Libor3M swaps: par spread", parSpreadExpected, parSpread, TOLERANCE_RATE);
  }

  @Test
  /** Tests Bucketed PV01 of ON Arithmetic Average (+ spread) vs Libor3M swaps. */
  public void BucketedPV01ONAA3M() {
    final double[] deltaDsc = {-0.5362, -0.5362, 1.5056, -20.3864, 156.4589, 
      -3141.2267, -3355.0095, -13.7148, 44.0214, 62.8386, 
      45.9853, -102.8615, 23497.7198, 40079.2957, 0.0000, 
      0.0000, 0.0000};
    final double[] deltaFwd3 = {2605.0956, 2632.0962, 1176.1245, -27.1136 , 33.6303, 
      6.7954, 7.4652, -11436.3319, -52207.1283, 0.00000,
      0.00000, 0.00000, 0.00000, 0.00000, 0.00000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SwapInstrumentsDataSet.SWAP_FF_3M, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Swap ON Arithmetic Average: bucketed deltas - standard curves", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
    final double[] deltaDsc2 = 
      {-66.3101, 438.4449, -1360.4301, 1803.0197, -3862.0535, 
      -3801.7400, 424.2098, -97.2625, 448.7985, -1657.1109, 
      6315.9521, -26682.5922, -49580.1061, 8890.1666, -2332.2614, 
      504.8825, -49.6669, 7.2155, -1.0967, 0.2881, -0.0500};
    final double[] deltaFwd32 = {2582.0522, 4305.9415, -673.9309, 200.0968, -72.8718, 
      -17.0822, -23.7277, 7.9026, -3.0249, 0.4406, 
      -0.0690, 0.0182, -0.0032 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity2 = new LinkedHashMap<>();
    sensitivity2.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDsc2));
    sensitivity2.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd32));
    final MultipleCurrencyParameterSensitivity pvpsExpected2 = new MultipleCurrencyParameterSensitivity(sensitivity2);
    final MultipleCurrencyParameterSensitivity pvpsComputed2 = MQSBC.fromInstrument(SwapInstrumentsDataSet.SWAP_FF_3M, MULTICURVE_FFS, BLOCK_FFS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Swap ON Arithmetic Average: bucketed deltas - fed fund based curve", 
        pvpsExpected2, pvpsComputed2, TOLERANCE_PV_DELTA);
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
        MQSBC.fromInstrument(SwapInstrumentsDataSet.SWAP_FIXED_3M, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
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
        MQSBC.fromInstrument(SwapInstrumentsDataSet.SWAP_FIXED_3M, MULTICURVE_FFS, BLOCK_FFS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("IRS Fixed v LIBOR3M: bucketed deltas", 
        pvpsExpected2, pvpsComputed2, TOLERANCE_PV_DELTA);
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
    final MultipleCurrencyParameterSensitivity pvpsComputed = 
        MQSBC.fromInstrument(SwapInstrumentsDataSet.BS_3M_S_6M, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Basis swap L3M v L6M: bucketed deltas", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  /** Tests Bucketed PV01 for a swap fixed vs LIBOR1M. */
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
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SwapInstrumentsDataSet.SWAP_FIXED_1M, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("IRS Fixed v LIBOR1M: bucketed deltas", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }
  
  @Test
  /**Tests Bucketed PV01 for a ON Cmp + spread v ON AA. */
  public void bucketedPV01ONCmpONAA() {
    final double[] deltaDsc = 
      {0.4188, -0.9726, 3.0558, -4.0519, 7.8819, 
      -14.7705, 34.8685, -67.5583, -62.0034, -97.1414, 
      114.4679, 205.5575, -42.4933, 11.4320, -3.1404, 
      0.6875, -0.0408, 0.0016, 0.0005, -0.0002, 0.0001};
    final double[] deltaFwd3 = 
      {0.3084, -1.3555, 71.5146, 42.6154, 164.7760, 
      -276.6229, -68.7434, 12.8506, -4.5519, 0.6655, 
      -0.1020, 0.0269, -0.0047};
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = 
        MQSBC.fromInstrument(SwapInstrumentsDataSet.BS_ONCMP_S_ONAA, MULTICURVE_FFS, BLOCK_FFS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Basis swap ON Cmp + spread v ON AA: bucketed deltas", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

}
