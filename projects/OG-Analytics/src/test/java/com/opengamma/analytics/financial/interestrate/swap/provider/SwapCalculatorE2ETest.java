package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwapCalculatorE2ETest {

  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsUSD.indexIborArrayUSDOisL1L3L6();
  private static final IborIndex USDLIBOR1M = INDEX_IBOR_LIST[0];
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_LIST[1];
  private static final Calendar CALENDAR = StandardDataSetsUSD.calendarArray()[0];
  private static final Currency USD = USDLIBOR3M.getCurrency();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  // Curve Data
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsUSD.getCurvesUSDOisL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 2, 18);

  // Standard conventions
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR1M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR1M", CALENDAR);
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR);

  private static final double NOTIONAL = 100000000; //100m
  // Instrument description: Swap Fixed vs Libor1M
  private static final ZonedDateTime START_DATE_1M = DateUtils.getUTCDate(2014, 9, 12);
  private static final Period TENOR_SWAP_1M = Period.ofYears(5);
  private static final double FIXED_RATE_1M = 0.0125;
  private static final GeneratorAttributeIR ATTRIBUTE_1M = new GeneratorAttributeIR(TENOR_SWAP_1M);
  private static final SwapDefinition SWAP_FIXED_1M_DEFINITION = USD6MLIBOR1M.generateInstrument(START_DATE_1M, FIXED_RATE_1M, NOTIONAL, ATTRIBUTE_1M);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_1M = SWAP_FIXED_1M_DEFINITION.toDerivative(REFERENCE_DATE);
  // Instrument description: Swap Fixed vs Libor3M
  private static final ZonedDateTime START_DATE_3M = DateUtils.getUTCDate(2014, 9, 12);
  private static final Period TENOR_SWAP_3M = Period.ofYears(10);
  private static final double FIXED_RATE_3M = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M = new GeneratorAttributeIR(TENOR_SWAP_3M);
  private static final SwapDefinition SWAP_FIXED_3M_DEFINITION = USD6MLIBOR3M.generateInstrument(START_DATE_3M, FIXED_RATE_3M, NOTIONAL, ATTRIBUTE_3M);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_3M = SWAP_FIXED_3M_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-4;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double BP1 = 1.0E-4;

  @Test
  /**
   * Test present value with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR1M. Can be used for platform testing or regression testing.
   */
  public void presentValue1M() {
    // Present Value
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_1M.accept(PVDC, MULTICURVE);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 3029889.0513);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Test present value with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void presentValue3M() {
    // Present Value
    final MultipleCurrencyAmount pvComputed = SWAP_FIXED_3M.accept(PVDC, MULTICURVE);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 13922262.1560159);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Test forward rate with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR1M. Can be used for platform testing or regression testing.
   */
  public void parRate1M() {
    final double parRate = SWAP_FIXED_1M.accept(PRDC, MULTICURVE);
    final double parRateExpected = 0.018750547210994777;
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateExpected, parRate, TOLERANCE_RATE);
  }

  @Test
  /**
   * Test forward rate with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR1M. Can be used for platform testing or regression testing.
   */
  public void parRate3M() {
    final double parRate = SWAP_FIXED_3M.accept(PRDC, MULTICURVE);
    final double parRateExpected = 0.030503957707234408;
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateExpected, parRate, TOLERANCE_RATE);
  }

  @Test
  /**
   * Test Bucketed PV01 with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void BucketedPV013M() {
    final double[] deltaDsc = {-109.21569905589685, -3.9005633897775693, 0.00, -2.9210928471750807E-4, 1.4446708798331307, -70.18678917594846, 4.063434475857444, -107.83773794596513,
      -247.24982349793797, -363.8039562740531, -485.1953983499274, -602.6668135729393, -732.6177103812603, -844.9397702333076, -972.1806298759524, -1091.9402994536438, -1600.9264938777508 };
    final double[] deltaFwd3 = {-2467.193492105822, -2554.159715884837, 0.015318107522794822, 0.0880794530928506, 32.72195110100366, -42.79452268211632, 1.6081072250661441, -17.264829202056397,
      -4.84423016897738, 71275.026591951, 23512.571668106564, 0.00, 0.00, 0.00, 0.00 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SWAP_FIXED_3M, MULTICURVE, BLOCK).multipliedBy(BP1);
    AssertSensivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed delts from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test Bucketed PV01 with a standard set of data against hard-coded standard values for a swap fixed vs LIBOR3M. Can be used for platform testing or regression testing.
   */
  public void BucketedPV011M() {
    final double[] deltaDsc = {-23.920268785383907, -0.8542962253122424, 3.169960129491918E-5, -8.591088119046564E-4, 1.4541692308631062, -42.8010937113937, 4.136280674336951, -45.07868671850383,
      -104.55226847938516, -148.29743474972304, -184.82689407910408, -386.2503518280091, 94.99491838839859, 132.3457876046568, 0.12791915267141205, 0.19443811990423096, 0.11188999147666708 };
    final double[] deltaFwd1 = {16.435621201080853, 5015.156994979652, -3.6558545954816344, 18.61448185713151, 37.2617769102718, -75.90349964510601, -4.530577036627824, -40704.27493679979,
      -13544.961874328397, 5.806784980289764E-9, 1.3817949850222894E-8, 1.6995597169101227E-8, 2.8762846588461285E-8, 2.6010948553526334E-8, 1.6778373679926674E-8 };
    final double[] deltaFwd3 = {-2471.5645535854824, -2529.7027760824426, 6.534531731522484, -35.31817854676756, -41.15629953153448, 83.17313044609304, 2.9911692774270664, 40013.24774906724,
      13264.99027809644, 12.374830276584856, -1.7240813202713925E-8, -2.85384179274041E-8, -4.494702702532258E-8, -3.419089184765406E-8, -2.8986038912780714E-8 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USDLIBOR1M), USD), new DoubleMatrix1D(deltaFwd1));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(SWAP_FIXED_1M, MULTICURVE, BLOCK).multipliedBy(BP1);
    AssertSensivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed delts from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

}
