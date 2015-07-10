/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.financial.convention.businessday.BusinessDayDateUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Demo test for equity options
 */
@Test(groups = TestGroup.UNIT)
public class EquityOptionE2ETest extends EquityE2ETestMaster {
  // trade
  private static final ZoneId ZID = ZoneId.of("EST");
  private static final ZonedDateTime TRADE_DATE = ZonedDateTime.of(2014, 3, 26, 13, 46, 0, 0, ZID);
  private static final Currency USD = Currency.USD;
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final double NOTIONAL = 325;

  // market data
  private static final double SPOT = 56.0;
  private static final ZonedDateTime[] DIVIDEND_DATES = new ZonedDateTime[] {
      ZonedDateTime.of(2014, 4, 2, 0, 0, 0, 0, ZID), ZonedDateTime.of(2014, 7, 1, 0, 0, 0, 0, ZID) };
  private static final double[] ALPHA = new double[] {0.38, 0.4 };
  private static final double[] BETA = new double[] {0.0, 0.0 };
  private static final double POINT_VALUE = 100.0;
  private static final ZonedDateTime[] EXPIRY_DATES = new ZonedDateTime[] {
      ZonedDateTime.of(2014, 3, 28, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 4, 11, 15, 0, 0, 0, ZID),
      ZonedDateTime.of(2014, 4, 25, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 5, 9, 15, 0, 0, 0, ZID),
      ZonedDateTime.of(2014, 5, 23, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 6, 20, 15, 0, 0, 0, ZID) };
  private static final double[][] STRIKES = new double[][] {
      {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
      {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
      {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
      {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
      {52.5, 55, 57.5, 60 }, {52.5, 55, 57.5, 60 } };
  private static final double[][] CALL_PRICES = new double[][] {
      {5.1, 4.1, 3.08, 2.07, 1.13, 0.41, 0.105, 0.05, 0.025, 0.02 },
      {5.275, 4.275, 3.34, 2.51, 1.702, 1.021, 0.64, 0.435, 0.345, 0.31 },
      {5.575, 4.61, 3.645, 2.805, 2.085, 1.505, 1.125, 0.825, 0.655, 0.535 },
      {5.65, 4.78, 3.925, 3.15, 2.475, 1.87, 1.455, 1.12, 0.89, 0.725 },
      {4.55, 2.81, 1.615, 0.92 }, {5.05, 3.425, 2.085, 1.215 } };
  private static final double[][] PUT_PRICES = new double[][] {
      {0.025, 0.03, 0.03, 0.025, 0.04, 0.2, 1.03, 2.01, 3.02, 4.075 },
      {0.295, 0.315, 0.355, 0.48, 0.65, 0.88, 1.63, 2.57, 3.675, 4.895 },
      {0.36, 0.475, 0.64, 0.855, 1.035, 1.205, 1.915, 2.925, 4.05, 5.15 },
      {0.49, 0.64, 0.79, 1, 1.34, 1.615, 2.21, 3.285, 4.325, 5.3 },
      {0.795, 1.4, 2.92, 5.315 }, {1.315, 2.055, 3.46, 5.41 } };

  /**
   * Vol surface is built by using call options 
   */
  @Test
  public void dividendCallOnlyTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = CALL_PRICES[i].length;
      isCalls[i] = new boolean[nOptions];
      Arrays.fill(isCalls[i], true);
    }
    double[] timeToExpiries = toDateToDouble(TRADE_DATE, EXPIRY_DATES);
    double[] tau = toDateToDouble(TRADE_DATE, DIVIDEND_DATES);
    AffineDividends dividend = new AffineDividends(tau, ALPHA, BETA);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, CALL_PRICES, isCalls,
        forwardCurve, exerciseType);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // Option 1
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // ITM 
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(true, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    double[] expected1 = new double[] {291.56750963236584, 94759.4406305189, 0.7433580421991999, 0.09387569587899836,
        -0.028143893730161934, 0.011158414048770745, 0.03669291093468003, 24159.136371473996, 3050.960116067447,
        -914.6765462302628, 362.6484565850492, 1192.519605377101 };
    assertOptionResult(targetOption1, NOTIONAL, data, expected1);

    // Option 2
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[2]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 57.5; // OTM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(true, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    double[] expected2 = new double[] {188.76756455195363, 61349.45847938493, 0.41601610970443836,
        0.058690007627269086, -0.01619733480905359, 0.0386020497360748, 0.09652116218314116, 13520.523565394247,
        1907.4252478862454, -526.4133812942416, 1254.566616422431, 3136.9377709520877 };
    assertOptionResult(targetOption2, NOTIONAL, data, expected2);
 }

  /**
   * Vol surface is constructed by using put options
   */
  @Test
  public void dividendPutOnlyTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = PUT_PRICES[i].length;
      isCalls[i] = new boolean[nOptions];
      Arrays.fill(isCalls[i], false);
    }
    double[] timeToExpiries = toDateToDouble(TRADE_DATE, EXPIRY_DATES);
    double[] tau = toDateToDouble(TRADE_DATE, DIVIDEND_DATES);
    AffineDividends dividend = new AffineDividends(tau, ALPHA, BETA);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, PUT_PRICES, isCalls,
        forwardCurve, exerciseType);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // Option 1
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // OTM
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(false, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    double[] expected1 = new double[] {40.86299068016362, 13280.471971053177, -0.22632187721042948,
        0.09894526790792406, -0.0337728538179612, -0.005734862479160847, 0.03517593490085753, -7355.461009338958,
        3215.721207007532, -1097.617749083739, -186.38303057272753, 1143.2178842778699 };
    assertOptionResult(targetOption1, NOTIONAL, data, expected1);

    // Option 2
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[2]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 58.5; // ITM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    double[] expected2 = new double[] {389.5706476110104, 126610.46047357837, -0.680130005903282, 0.06622597074777498,
        -0.01656306237658076, -0.08281575479676825, 0.08778111393062614, -22104.225191856665, 2152.344049302687,
        -538.2995272388747, -2691.512030894968, 2852.8862027453497 };
    assertOptionResult(targetOption2, NOTIONAL, data, expected2);
  }

  /**
   * Vol surface is constructed by using OTM call and put options
   */
  @Test
  public void dividendCallPutOTMTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    double[][] optionPrices = new double[nExpiries][];
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = STRIKES[i].length;
      optionPrices[i] = new double[nOptions];
      isCalls[i] = new boolean[nOptions];
      for (int j = 0; j < nOptions; ++j) {
        if (STRIKES[i][j] < SPOT) {
          optionPrices[i][j] = PUT_PRICES[i][j];
          isCalls[i][j] = false;
        } else {
          optionPrices[i][j] = CALL_PRICES[i][j];
          isCalls[i][j] = true;
        }
      }
    }
    double[] timeToExpiries = toDateToDouble(TRADE_DATE, EXPIRY_DATES);
    double[] tau = toDateToDouble(TRADE_DATE, DIVIDEND_DATES);
    AffineDividends dividend = new AffineDividends(tau, ALPHA, BETA);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, optionPrices, isCalls,
        forwardCurve, exerciseType);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // ITM call option
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // ITM 
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(true, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    double[] expected1 = new double[] {263.49044651502817, 85634.39511738415, 0.8249842622485184, 0.12405698831215517,
        -0.013847882501060185, 0.009035557820894862, 0.028178878648973716, 26811.98852307685, 4031.852120145043,
        -450.056181284456, 293.655629179083, 915.8135560916459 };
    assertOptionResult(targetOption1, NOTIONAL, data, expected1);

    // ITM put option
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[2]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 58.5; // ITM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    double[] expected2 = new double[] {449.8314803750468, 146195.23112189022, -0.6292338338039938,
        0.053545033644796146, -0.02136148161220006, -0.07838217763747224, 0.09297744638761118, -20450.099598629797,
        1740.2135934558748, -694.248152396502, -2547.420773217848, 3021.7670075973633 };
    assertOptionResult(targetOption2, NOTIONAL, data, expected2);
  }

  /**
   * European call and put options 
   */
  @Test
  public void europeanTest() {
    ExerciseDecisionType exerciseType = ExerciseDecisionType.EUROPEAN;
    double[][] euCallPrices = new double[][] {
        {5.1, 4.1, 3.08, 2.07, 1.13, 0.31, 0.105, 0.05, 0.025, 0.02 },
        {5.175, 4.175, 3.34, 2.51, 1.702, 1.021, 0.64, 0.435, 0.345, 0.31 },
        {5.575, 4.61, 3.645, 2.805, 2.085, 1.505, 1.125, 0.825, 0.655, 0.535 },
        {5.65, 4.78, 3.925, 3.15, 2.475, 1.87, 1.455, 1.12, 0.89, 0.725 },
        {4.55, 2.81, 1.615, 0.92 }, {5.05, 3.425, 2.085, 1.215 } };
    double[][] euPutPrices = new double[][] {
        {0.025, 0.03, 0.03, 0.025, 0.04, 0.305, 1.03, 2.01, 3.02, 4.075 },
        {0.525, 0.545, 0.715, 0.88, 1.08, 1.39, 2.02, 2.81, 3.715, 4.695 },
        {0.95, 0.985, 1.02, 1.175, 1.455, 1.875, 2.495, 3.195, 4.03, 4.91 },
        {1.03, 1.15, 1.3, 1.52, 1.84, 2.245, 2.83, 3.485, 4.255, 5.1 },
        {1.425, 2.19, 3.49, 5.295 }, {1.915, 2.785, 3.95, 5.58 } };
    double[] timeToExpiries = toDateToDouble(TRADE_DATE, EXPIRY_DATES);
    double[] tau = toDateToDouble(TRADE_DATE, DIVIDEND_DATES);
    AffineDividends dividend = new AffineDividends(tau, ALPHA, BETA);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    int nExpiries = EXPIRY_DATES.length;
    double[][] optionPrices = new double[nExpiries][];
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = STRIKES[i].length;
      optionPrices[i] = new double[nOptions];
      isCalls[i] = new boolean[nOptions];
      for (int j = 0; j < nOptions; ++j) {
        if (STRIKES[i][j] < SPOT) {
          optionPrices[i][j] = euPutPrices[i][j];
          isCalls[i][j] = false;
        } else {
          optionPrices[i][j] = euCallPrices[i][j];
          isCalls[i][j] = true;
        }
      }
    }
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, optionPrices, isCalls,
        forwardCurve, exerciseType);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // ITM call option
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // ITM 
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(true, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    double[] expected1 = new double[] {291.9372446579417, 94879.60451383104, 0.7049649450883742, 0.08035708404397142,
        -0.028315856794856677, 1.602571593572363, 3.9874876101033134, 22911.36071537216, 2611.605231429071,
        -920.2653458328421, 52083.5767911018, 129593.34732835769 };
    assertOptionResult(targetOption1, NOTIONAL, data, expected1);


    // ITM put option
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[1]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 58.5; // ITM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    double[] expected2 = new double[] {438.3525829195858, 142464.5894488654, -0.6371842871074586, 0.05559556867013955,
        -0.02047344552497136, 7.903399740601026, 9.227915017241902, -20708.4893309924, 1806.8559817795356,
        -665.3869795615693, 256860.49156953333, 299907.2380603618 };
    assertOptionResult(targetOption2, NOTIONAL, data, expected2);

    // OTM call option
    ZonedDateTime expiryDate3 = EXPIRY_DATES[2];
    LocalDate settlementDate3 = BusinessDayDateUtils.addWorkDays(expiryDate3.toLocalDate(), 3, NYC);
    double targetStrike3 = 57.5; // OTM
    EquityOptionDefinition targetOption3Dfn = new EquityOptionDefinition(true, targetStrike3, USD,
        exerciseType, expiryDate3, settlementDate3, POINT_VALUE, SettlementType.PHYSICAL);
    EquityOption targetOption3 = targetOption3Dfn.toDerivative(TRADE_DATE);
    double[] expected3 = new double[] {96.1168240349344, 31237.96781135368, 0.3454892853212746, 0.08494846834611697,
        -0.022095497012336332, 1.5111971291212627, 5.892958177166839, 11228.401772941424, 2760.8252212488014,
        -718.1036529009308, 49113.90669644104, 191521.14075792223 };
    assertOptionResult(targetOption3, NOTIONAL, data, expected3);

    // OTM put option
    ZonedDateTime expiryDate4 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID);
    LocalDate settlementDate4 = BusinessDayDateUtils.addWorkDays(expiryDate4.toLocalDate(), 3, NYC);
    double targetStrike4 = 51.5; // OTM
    EquityOptionDefinition targetOption4Dfn = new EquityOptionDefinition(false, targetStrike4, USD,
        exerciseType, expiryDate4, settlementDate4, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption4 = targetOption4Dfn.toDerivative(TRADE_DATE);
    double[] expected4 = new double[] {139.18639622061397, 45235.57877169954, -0.26708010101144775,
        0.04138149377665815, -0.019400255824207735, 3.224879924813697, 8.15126668810231, -8680.103282872053,
        1344.89854774139, -630.5083142867514, 104808.59755644515, 264916.1673633251 };
    assertOptionResult(targetOption4, NOTIONAL, data, expected4);
  }

  /**
   * No dividend payment. 
   */
  @Test
  public void noDividendTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = PUT_PRICES[i].length;
      isCalls[i] = new boolean[nOptions];
      Arrays.fill(isCalls[i], false);
    }
    double[] timeToExpiries = toDateToDouble(TRADE_DATE, EXPIRY_DATES);
    double[] tau = new double[] {};
    double[] alpha = new double[] {};
    double[] beta = new double[] {};
    AffineDividends dividend = new AffineDividends(tau, alpha, beta);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    //  Equivalent construction of forward curve
    //       YieldAndDiscountCurve constantCurve = YieldCurve.from(ConstantDoublesCurve.from(0.0));
    //    ForwardCurveYieldImplied forwardCurve = new ForwardCurveYieldImplied(SPOT, SINGLE_CURVE, constantCurve);
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, PUT_PRICES, isCalls,
        forwardCurve, exerciseType);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // Option 1
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 53.5; // OTM
    EquityOptionDefinition targetOption1Dfn = new EquityOptionDefinition(false, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    double[] expected1 = new double[] {40.85549009446838, 13278.034280702223, -0.211236426949203, 0.0872436729523134,
        -0.02991197901689341, -0.005364512793468524, 0.03390731790197594, -6865.183875849098, 2835.4193709501856,
        -972.1393180490358, -174.346665787727, 1101.987831814218 };
    assertOptionResult(targetOption1, NOTIONAL, data, expected1);

    // Option 2
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2014, 6, 6, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[1]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 58.5; // ITM
    EquityOptionDefinition targetOption2Dfn = new EquityOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    double[] expected2 = new double[] {388.8222088041128, 126367.21786133666, -0.6378366779223801, 0.0625115852644212,
        -0.015518137566435322, -0.07812902673210956, 0.09324125158922084, -20729.692032477353, 2031.626521093689,
        -504.33947090914796, -2539.1933687935607, 3030.340676649677 };
    assertOptionResult(targetOption2, NOTIONAL, data, expected2);
  }
}
