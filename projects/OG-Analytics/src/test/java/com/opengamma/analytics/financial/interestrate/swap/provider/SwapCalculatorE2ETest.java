package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
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
 * Test related to swap using standardized market data.
 */
@Test(groups = TestGroup.UNIT)
public class SwapCalculatorE2ETest {

  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL1L3L6();
  private static final IborIndex USDLIBOR1M = INDEX_IBOR_LIST[0];
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_LIST[1];
  private static final Calendar CALENDAR = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final Currency USD = USDLIBOR3M.getCurrency();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  // Curve Data
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 22);

  // Standard conventions
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_SWAP_FIXED_ONCMP_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR1M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR1M", CALENDAR);
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR);
  private static final GeneratorSwapFixedON USD1YFEDFUND = GENERATOR_SWAP_FIXED_ONCMP_MASTER.getGenerator("USD1YFEDFUND", CALENDAR);

  private static final double NOTIONAL = 100000000; //100m
  // Instrument description: Swap Fixed vs ON Cmp
  private static final ZonedDateTime START_DATE_ON = DateUtils.getUTCDate(2014, 2, 3);
  private static final Period TENOR_SWAP_ON = Period.ofMonths(2);
  private static final double FIXED_RATE_ON = 0.00123;
  private static final GeneratorAttributeIR ATTRIBUTE_ON = new GeneratorAttributeIR(TENOR_SWAP_ON);
  private static final SwapDefinition SWAP_FIXED_ON_DEFINITION = USD1YFEDFUND.generateInstrument(START_DATE_ON, FIXED_RATE_ON, NOTIONAL, ATTRIBUTE_ON);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_ON = SWAP_FIXED_ON_DEFINITION.toDerivative(REFERENCE_DATE);
  // Instrument description: Swap Fixed vs Libor3M
  private static final ZonedDateTime START_DATE_3M = DateUtils.getUTCDate(2014, 9, 10);
  private static final Period TENOR_SWAP_3M = Period.ofYears(7);
  private static final double FIXED_RATE_3M = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M = new GeneratorAttributeIR(TENOR_SWAP_3M);
  private static final SwapDefinition SWAP_FIXED_3M_DEFINITION = USD6MLIBOR3M.generateInstrument(START_DATE_3M, FIXED_RATE_3M, NOTIONAL, ATTRIBUTE_3M);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_3M = SWAP_FIXED_3M_DEFINITION.toDerivative(REFERENCE_DATE);
  // Instrument description: Swap Fixed vs Libor1M
  private static final ZonedDateTime START_DATE_1M = DateUtils.getUTCDate(2014, 9, 10);
  private static final Period TENOR_SWAP_1M = Period.ofYears(2);
  private static final double FIXED_RATE_1M = 0.0125;
  private static final GeneratorAttributeIR ATTRIBUTE_1M = new GeneratorAttributeIR(TENOR_SWAP_1M);
  private static final SwapDefinition SWAP_FIXED_1M_DEFINITION = USD6MLIBOR1M.generateInstrument(START_DATE_1M, FIXED_RATE_1M, NOTIONAL, ATTRIBUTE_1M);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_1M = SWAP_FIXED_1M_DEFINITION.toDerivative(REFERENCE_DATE);
  // Instrument description: Swap Fixed vs Libor3M Already started (with fixing)
  private static final ZonedDateTime START_DATE_3M_S = DateUtils.getUTCDate(2013, 9, 10);
  private static final Period TENOR_SWAP_3M_S = Period.ofYears(7);
  private static final double FIXED_RATE_3M_S = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M_S = new GeneratorAttributeIR(TENOR_SWAP_3M_S);
  private static final SwapFixedIborDefinition SWAP_FIXED_3M_S_DEFINITION = USD6MLIBOR3M.generateInstrument(START_DATE_3M_S, FIXED_RATE_3M_S, NOTIONAL, ATTRIBUTE_3M_S);
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 10),
      DateUtils.getUTCDate(2013, 12, 12) }, new double[] {0.0024185, 0.0100 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M };
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_3M_S = SWAP_FIXED_3M_S_DEFINITION.toDerivative(REFERENCE_DATE, TS_FIXED_IBOR_USD3M);
  // Instrument description: Swap Libor3M+S vs Libor6M

  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-4;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double BP1 = 1.0E-4;

  /** FEDFUND products **/
  
  @Test
  /**
   * Test present value with a standard set of data against hard-coded standard values for a swap fixed vs ON compounded. 
   */
  public void presentValueONCmp() {
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_ON.accept(PVDC, MULTICURVE);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, -9723.264518929138);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Test forward rate with a standard set of data against hard-coded standard values for a swap fixed vs Fed Fund compounded.
   */
  public void parRateONCmp() {
    final double parRate = SWAP_FIXED_ON.accept(PRDC, MULTICURVE);
    final double parRateExpected = 6.560723881400023E-4;
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateExpected, parRate, TOLERANCE_RATE);
  }
  
  /** LIBOR3M products **/
  
  @Test
  /**
   * Test present value with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void presentValue3M() {
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_3M.accept(PVDC, MULTICURVE);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 7170391.798257509);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Test present value with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void presentValue3MWithFixing() {
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_3M_S.accept(PVDC, MULTICURVE);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 3588376.471608199);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Test forward rate with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR1M. Can be used for platform testing or regression testing.
   */
  public void parRate3M() {
    final double parRate = SWAP_FIXED_3M.accept(PRDC, MULTICURVE);
    final double parRateExpected = 0.025894715668195054;
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateExpected, parRate, TOLERANCE_RATE);
  }

  @Test
  /**
   * Test Bucketed PV01 with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void BucketedPV013M() {
    final double[] deltaDsc = {-2.0061282888005487, -2.0061296819291816, -8.67452075363044E-5, 0.0011745459201512494, 1.4847039752079148, -56.9491079838621, 
        1.1272953888594144, -86.07354102781184, -166.96224129263487, -242.22201138850485, -314.19406010048203, -385.9029177491706, -463.2762183477875, 
        -979.7315575792289, -243.35533439972858, 243.5314114568193, 139.99052652789604 };
    final double[] deltaFwd3 = {-2604.935862485693, -2632.099517240374, -1176.1264079094185, 27.132459446981603, -34.136228550265635, -8.299063015802915, 
        -10.516911338517652, 0.5088197130590212, 56648.04062948109, 15520.134985155655, 0.00, 0.00, 0.00, 0.00, 0.00 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SWAP_FIXED_3M, MULTICURVE, BLOCK).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed delts from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }
  
  /** LIBOR1M products **/

  @Test
  /**
   * Test present value with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR1M. Can be used for platform testing or regression testing.
   */
  public void presentValue1M() {
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_1M.accept(PVDC, MULTICURVE);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, -1003684.8402);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Test forward rate with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR1M. Can be used for platform testing or regression testing.
   */
  public void parRate1M() {
    final double parRate = SWAP_FIXED_1M.accept(PRDC, MULTICURVE);
    final double parRateExpected = 0.007452504182638092;
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateExpected, parRate, TOLERANCE_RATE);
  }

  @Test
  /**
   * Test Bucketed PV01 with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void BucketedPV011M() {
    final double[] deltaDsc =
      {0.3008,0.3008,0.0000,0.0002,1.5114,-13.5657,0.0846,45.9662,99.7453,104.8512,
      0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000 };
      final double[] deltaFwd1 =
      {-0.1666,2864.6265,3569.5692,33.0663,-9894.4160,-16771.9988,0.0000,0.0000,0.0000,0.0000,
      0.0000,0.0000,0.0000,0.0000,0.0000 };
      final double[] deltaFwd3 =
      {-2601.2036,-2629.5677,-1202.4749,-32.6058,9752.5245,16503.8143,0.0000,0.0000,0.0000,0.0000,
      0.0000,0.0000,0.0000,0.0000,0.0000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USDLIBOR1M), USD), new DoubleMatrix1D(deltaFwd1));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SWAP_FIXED_1M, MULTICURVE, BLOCK).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed delts from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

}
