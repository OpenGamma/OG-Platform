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
import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackScholesRhoCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackScholesThetaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotDeltaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotGammaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackVegaCalculator;
import com.opengamma.analytics.financial.equity.EqyOptBjerksundStenslandGreekCalculator;
import com.opengamma.analytics.financial.equity.EqyOptBjerksundStenslandPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayDateUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Demo test for equity index options
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexOptionE2ETest extends EquityE2ETestMaster {
  // Calculators for European options
  private static final EquityOptionBlackPresentValueCalculator PV_EUROPEAN = EquityOptionBlackPresentValueCalculator
      .getInstance();
  private static final EquityOptionBlackScholesRhoCalculator RHO_EUROPEAN = EquityOptionBlackScholesRhoCalculator
      .getInstance();
  private static final EquityOptionBlackSpotDeltaCalculator DELTA_EUROPEAN = EquityOptionBlackSpotDeltaCalculator
      .getInstance();
  private static final EquityOptionBlackSpotGammaCalculator GAMMA_EUROPEAN = EquityOptionBlackSpotGammaCalculator
      .getInstance();
  private static final EquityOptionBlackScholesThetaCalculator THETA_EUROPEAN = EquityOptionBlackScholesThetaCalculator
      .getInstance();
  private static final EquityOptionBlackVegaCalculator VEGA_EUROPEAN = EquityOptionBlackVegaCalculator.getInstance();

  // Calculators for American options
  private static final EqyOptBjerksundStenslandPresentValueCalculator PV_AMERICAN = EqyOptBjerksundStenslandPresentValueCalculator
      .getInstance();
  private static final EqyOptBjerksundStenslandGreekCalculator GREEKS_AMERICAN = EqyOptBjerksundStenslandGreekCalculator
      .getInstance();

  // trade
  private static final ZoneId ZID = ZoneId.of("EST");
  private static final ZonedDateTime TRADE_DATE = ZonedDateTime.of(2014, 10, 23, 13, 46, 0, 0, ZID);
  private static final Currency USD = Currency.USD;
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final double NOTIONAL = 50;

  // forward curve interpolator
  private static final Interpolator1D FORWARD_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  // market data
  private static final double SPOT = 3949.6;
  private static final ZonedDateTime[] FUTURE_MATURITY_DATES = new ZonedDateTime[] {
      ZonedDateTime.of(2014, 12, 19, 15, 0, 0, 0, ZID), ZonedDateTime.of(2015, 3, 20, 15, 0, 0, 0, ZID),
      ZonedDateTime.of(2015, 6, 19, 15, 0, 0, 0, ZID), ZonedDateTime.of(2015, 9, 18, 15, 0, 0, 0, ZID) };
  private static final double[] FUTURE_PRICES = new double[] {3949.25, 3942.0, 3930.0, 3915.75 };
  private static final double POINT_VALUE = 100.0;
  private static final ZonedDateTime[] EXPIRY_DATES = new ZonedDateTime[] {
      ZonedDateTime.of(2014, 11, 21, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 12, 19, 15, 0, 0, 0, ZID),
      ZonedDateTime.of(2015, 1, 16, 15, 0, 0, 0, ZID), ZonedDateTime.of(2015, 3, 20, 15, 0, 0, 0, ZID),
      ZonedDateTime.of(2015, 6, 19, 15, 0, 0, 0, ZID) };
  private static final double[][] STRIKES = new double[][] {
      {3600.0, 3625.0, 3650.0, 3675.0, 3700.0, 3710.0, 3720.0, 3730.0, 3740.0, 3750.0, 3760.0, 3770.0, 3780.0, 3790.0,
          3800.0, 3810.0, 3820.0, 3830.0, 3840.0, 3850.0, 3860.0, 3870.0, 3880.0, 3890.0, 3900.0, 3910.0, 3920.0,
          3930.0, 3940.0, 3950.0, 3960.0, 3970.0, 3980.0, 3990.0, 4000.0, 4010.0, 4020.0, 4030.0, 4040.0, 4050.0,
          4060.0, 4070.0, 4080.0, 4090.0, 4100.0, 4110.0, 4120.0, 4130.0, 4140.0, 4150.0, 4160.0, 4170.0, 4180.0,
          4190.0, 4200.0, 4210.0, 4220.0, 4230.0, 4240.0, 4250.0, 4260.0, 4270.0, 4280.0, 4290.0, 4300.0 },
      {3600.0, 3625.0, 3650.0, 3675.0, 3700.0, 3710.0, 3720.0, 3730.0, 3740.0, 3750.0, 3760.0, 3770.0, 3780.0, 3790.0,
          3800.0, 3810.0, 3820.0, 3830.0, 3840.0, 3850.0, 3860.0, 3870.0, 3880.0, 3890.0, 3900.0, 3910.0, 3920.0,
          3930.0, 3940.0, 3950.0, 3960.0, 3970.0, 3980.0, 3990.0, 4000.0, 4010.0, 4020.0, 4030.0, 4040.0, 4050.0,
          4060.0, 4070.0, 4080.0, 4090.0, 4100.0, 4110.0, 4120.0, 4130.0, 4140.0, 4150.0, 4160.0, 4170.0, 4180.0,
          4190.0, 4200.0, 4210.0, 4220.0, 4230.0, 4240.0, 4250.0, 4260.0, 4270.0, 4280.0, 4290.0, 4300.0 },
      {3600.0, 3625.0, 3650.0, 3675.0, 3700.0, 3725.0, 3750.0, 3775.0, 3800.0, 3825.0, 3850.0, 3875.0, 3900.0, 3925.0,
          3950.0, 3975.0, 4000.0, 4025.0, 4050.0, 4075.0, 4100.0, 4125.0, 4150.0, 4175.0, 4200.0, 4225.0, 4250.0,
          4275.0, 4300.0 },
      {3600.0, 3625.0, 3650.0, 3675.0, 3700.0, 3725.0, 3750.0, 3775.0, 3800.0, 3825.0, 3850.0, 3875.0, 3900.0, 3925.0,
          3950.0, 3975.0, 4000.0, 4025.0, 4050.0, 4075.0, 4100.0, 4125.0, 4150.0, 4175.0, 4200.0, 4225.0, 4250.0,
          4275.0, 4300.0 },
      {3600.0, 3625.0, 3650.0, 3675.0, 3700.0, 3725.0, 3750.0, 3775.0, 3800.0, 3825.0, 3850.0, 3875.0, 3900.0, 3925.0,
          3950.0, 3975.0, 4000.0, 4025.0, 4050.0, 4075.0, 4100.0, 4125.0, 4150.0, 4175.0, 4200.0, 4225.0, 4250.0,
          4275.0, 4300.0 } };
  private static final double[][] CALL_PRICES = new double[][] {
      {363.05, 339.822413969573, 316.8, 293.95, 271.65, 262.85, 254.05, 245.45, 236.8, 228.15, 219.65, 211.15, 202.85,
          194.65, 186.5, 178.95, 170.9, 163.15, 155.5, 147.75, 140.35, 133.05, 125.95, 119.0, 112.2, 105.7, 99.2,
          92.85, 86.9, 80.9, 75.25, 69.85, 64.55, 59.8, 54.7, 49.8, 45.5, 41.35, 37.7, 34.05, 30.65, 27.71, 24.45,
          21.75, 19.15, 16.9, 14.85, 13.1, 11.3, 9.8, 8.5, 7.5, 6.45, 5.55, 4.7, 4.2, 3.35, 2.85, 2.65, 2.3, 1.95, 1.7,
          1.5, 1.4, 1.3 },
      {377.85, 355.95, 334.15, 312.7, 292.05, 283.455, 275.45, 267.3, 259.25, 251.25, 243.4, 235.5, 227.8, 220.35,
          212.8, 205.35, 197.9, 190.7, 183.5, 176.5, 169.5, 162.4, 155.9, 149.0, 142.55, 136.2, 129.85, 123.75, 117.75,
          111.85, 106.15, 100.55, 95.25, 90.0, 84.8, 79.85, 75.0, 70.2, 65.95, 61.65, 57.45, 53.6, 49.7, 46.1, 42.65,
          39.35, 36.15, 33.3, 30.7, 28.15, 25.4, 23.4, 21.05, 19.05, 17.25, 15.55, 14.25, 12.8, 11.25, 10.05, 9.1,
          7.75, 7.3, 6.25, 5.6 },
      {391.7, 370.9, 350.1, 329.75, 309.35, 290.0, 270.7, 252.15, 233.8, 215.85, 198.25, 181.65, 165.45, 150.0,
          135.2, 121.55, 108.154, 95.25, 83.75, 72.758, 62.85, 53.6, 45.45, 38.1, 31.65, 26.0, 21.4, 16.9, 12.85 },
      {423.05, 403.3, 384.0, 364.95, 346.0, 327.45, 309.6, 292.0, 274.55, 257.95, 241.5, 225.45, 209.95, 195.15,
          181.05, 166.45, 153.1, 140.25, 128.15, 116.4, 105.4, 95.05, 85.3, 76.6, 68.25, 59.95, 53.4, 47.05, 40.7 },
      {456.55, 438.45, 419.5, 402.4, 383.85, 366.5, 349.45, 333.3, 316.8, 301.1, 285.1, 270.0, 255.15, 240.55, 227.25,
          213.45, 199.85, 187.5, 175.4, 163.25, 152.55, 140.45, 130.45, 119.8, 110.55, 101.7, 92.6, 84.4, 76.65 } };
  private static final double[][] PUT_PRICES = new double[][] {
      {12.35, 14.1, 16.1, 18.25, 20.9, 22.1, 23.3, 24.7, 26.05, 27.4, 28.9, 30.4, 32.1, 33.9, 35.75, 38.2, 40.15, 42.4,
          44.75, 47.0, 49.6, 52.3, 55.2, 58.25, 61.45, 64.95, 68.45, 72.1, 76.15, 80.15, 84.5, 89.1, 93.8, 99.05,
          103.95, 109.05, 114.75, 120.6, 126.95, 133.3, 139.9, 146.95, 153.7, 161.0, 168.4, 176.15, 184.1, 192.35,
          200.55, 209.05, 217.75, 226.75, 235.7, 244.8, 253.95, 263.45, 272.6, 282.1, 291.9, 301.5, 311.15, 320.9,
          330.7, 340.6, 350.5 },
      {28.65, 31.75, 34.95, 38.5, 42.85, 44.25, 46.25, 48.1, 50.05, 52.05, 54.15, 56.25, 58.55, 61.1, 63.55, 66.1,
          68.65, 71.45, 74.25, 77.25, 80.25, 83.15, 86.65, 89.75, 93.3, 96.95, 100.6, 104.5, 108.5, 112.6, 116.9,
          121.3, 126.0, 130.75, 135.55, 140.6, 145.75, 150.95, 156.7, 162.4, 168.2, 174.35, 180.45, 186.85, 193.4,
          200.1, 206.9, 214.05, 221.4, 228.85, 236.15, 244.1, 251.75, 259.75, 267.95, 276.25, 284.95, 293.5, 301.95,
          310.75, 319.8, 328.45, 338.0, 346.95, 356.3 },
      {44.25, 48.45, 52.65, 57.3, 61.9, 67.5, 73.2, 79.65, 86.3, 93.35, 100.75, 109.15, 117.95, 127.5, 137.7, 149.0,
          160.6, 172.7, 186.2, 200.2, 215.3, 231.05, 247.9, 265.55, 284.05, 303.4, 323.8, 344.3, 365.25 },
      {81.2, 86.45, 92.1, 98.05, 104.1, 110.55, 117.7, 125.05, 132.6, 141.0, 149.55, 158.5, 167.95, 178.15, 189.05,
          199.45, 211.1, 223.2, 236.1, 249.35, 263.35, 278.0, 293.2, 309.5, 326.15, 342.85, 361.25, 379.9, 398.55 },
      {126.8, 133.7, 139.7, 147.6, 154.05, 161.65, 169.6, 178.4, 186.9, 196.2, 205.15, 215.05, 225.15, 235.55, 247.25,
          258.4, 269.8, 282.45, 295.3, 308.15, 322.4, 335.3, 350.3, 364.6, 380.35, 396.45, 412.35, 429.15, 446.35 } };

  /**
   * American put options
   */
  @Test
  public void americanPutTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    double[][] optionPrices = new double[nExpiries][];
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = STRIKES[i].length;
      optionPrices[i] = new double[nOptions];
      isCalls[i] = new boolean[nOptions];
      Arrays.fill(isCalls[i], false);
      optionPrices[i] = Arrays.copyOf(PUT_PRICES[i], nOptions);
    }
    boolean isAmerican = exerciseType == ExerciseDecisionType.AMERICAN ? true : false;
    double[] timeToExpiries = toDateToDouble(TRADE_DATE, EXPIRY_DATES);
    double[] futureMaturities = toDateToDouble(TRADE_DATE, FUTURE_MATURITY_DATES);
    ForwardCurve forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(futureMaturities,
        FUTURE_PRICES, FORWARD_INTERPOLATOR));
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, optionPrices, isCalls,
        forwardCurve, isAmerican);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // OTM put option
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 3825.0; // OTM 
    EquityIndexOptionDefinition targetOption1Dfn = new EquityIndexOptionDefinition(false, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityIndexOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract1 = targetOption1.accept(PV_AMERICAN, data);
    Double pv1 = targetOption1.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks1 = targetOption1.accept(GREEKS_AMERICAN, data);
    double delta1 = greeks1.get(Greek.DELTA);
    double gamma1 = greeks1.get(Greek.GAMMA);
    double theta1 = greeks1.get(Greek.THETA);
    double rho1 = greeks1.get(Greek.RHO);
    double vega1 = greeks1.get(Greek.VEGA);
    double positionDelta1 = greeks1.get(Greek.DELTA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionGamma1 = greeks1.get(Greek.GAMMA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionTheta1 = greeks1.get(Greek.THETA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionRho1 = greeks1.get(Greek.RHO) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionVega1 = greeks1.get(Greek.VEGA) * targetOption1.getUnitAmount() * NOTIONAL;
    double[] res1 = new double[] {pvPerContract1, pv1, delta1, gamma1, theta1, rho1, vega1, positionDelta1,
        positionGamma1, positionTheta1, positionRho1, positionVega1 };
    double[] expected1 = new double[] {7003.405080211951, 350170.25401059753, -0.32794324602390557,
        0.0011583327329192671, -1.0045632814954177, -2.1334636391931023, 5.638990237262086, -1639.716230119528,
        5.791663664596336, -5022.816407477088, -10667.31819596551, 28194.95118631043 };
    assertEqualsArray(COMPUTE_VALUES, expected1, res1, TOL);

    // ITM put option
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2015, 1, 5, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[2]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 4025.0; // ITM
    EquityIndexOptionDefinition targetOption2Dfn = new EquityIndexOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityIndexOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract2 = targetOption2.accept(PV_AMERICAN, data);
    Double pv2 = targetOption2.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks2 = targetOption2.accept(GREEKS_AMERICAN, data);
    double delta2 = greeks2.get(Greek.DELTA);
    double gamma2 = greeks2.get(Greek.GAMMA);
    double theta2 = greeks2.get(Greek.THETA);
    double rho2 = greeks2.get(Greek.RHO);
    double vega2 = greeks2.get(Greek.VEGA);
    double positionDelta2 = greeks2.get(Greek.DELTA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionGamma2 = greeks2.get(Greek.GAMMA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionTheta2 = greeks2.get(Greek.THETA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionRho2 = greeks2.get(Greek.RHO) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionVega2 = greeks2.get(Greek.VEGA) * targetOption2.getUnitAmount() * NOTIONAL;
    double[] res2 = new double[] {pvPerContract2, pv2, delta2, gamma2, theta2, rho2, vega2, positionDelta2,
        positionGamma2, positionTheta2, positionRho2, positionVega2 };
    double[] expected2 = new double[] {16354.409183292037, 817720.4591646019, -0.5840436960476042,
        0.0012909508047592303, -0.8253537515916972, -5.011446402186543, 6.930396434699613, -2920.218480238021,
        6.454754023796151, -4126.768757958486, -25057.232010932716, 34651.982173498065 };
    assertEqualsArray(COMPUTE_VALUES, expected2, res2, TOL);
  }

  /**
   * American call options
   */
  @Test
  public void americanCallTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.AMERICAN;
    int nExpiries = EXPIRY_DATES.length;
    double[][] optionPrices = new double[nExpiries][];
    boolean[][] isCalls = new boolean[nExpiries][];
    for (int i = 0; i < nExpiries; ++i) {
      int nOptions = STRIKES[i].length;
      optionPrices[i] = new double[nOptions];
      isCalls[i] = new boolean[nOptions];
      Arrays.fill(isCalls[i], true);
      optionPrices[i] = Arrays.copyOf(CALL_PRICES[i], nOptions);
    }
    boolean isAmerican = exerciseType == ExerciseDecisionType.AMERICAN ? true : false;
    double[] timeToExpiries = toDateToDouble(TRADE_DATE, EXPIRY_DATES);
    double[] futureMaturities = toDateToDouble(TRADE_DATE, FUTURE_MATURITY_DATES);
    ForwardCurve forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(futureMaturities,
        FUTURE_PRICES, FORWARD_INTERPOLATOR));
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, optionPrices, isCalls,
        forwardCurve, isAmerican);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // ITM call option
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 3825.0; // ITM 
    EquityIndexOptionDefinition targetOption1Dfn = new EquityIndexOptionDefinition(true, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityIndexOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract1 = targetOption1.accept(PV_AMERICAN, data);
    Double pv1 = targetOption1.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks1 = targetOption1.accept(GREEKS_AMERICAN, data);
    double delta1 = greeks1.get(Greek.DELTA);
    double gamma1 = greeks1.get(Greek.GAMMA);
    double theta1 = greeks1.get(Greek.THETA);
    double rho1 = greeks1.get(Greek.RHO);
    double vega1 = greeks1.get(Greek.VEGA);
    double positionDelta1 = greeks1.get(Greek.DELTA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionGamma1 = greeks1.get(Greek.GAMMA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionTheta1 = greeks1.get(Greek.THETA) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionRho1 = greeks1.get(Greek.RHO) * targetOption1.getUnitAmount() * NOTIONAL;
    double positionVega1 = greeks1.get(Greek.VEGA) * targetOption1.getUnitAmount() * NOTIONAL;
    double[] res1 = new double[] {pvPerContract1, pv1, delta1, gamma1, theta1, rho1, vega1, positionDelta1,
        positionGamma1, positionTheta1, positionRho1, positionVega1 };
    double[] expected1 = new double[] {19448.32698966393, 972416.3494831964, 0.6722227856448935, 0.0011611972654525307,
        -0.9540269304132232, 3.339565206778478, 5.62847715661257, 3361.113928224467, 5.805986327262653,
        -4770.134652066116, 16697.82603389239, 28142.385783062848 };
    assertEqualsArray(COMPUTE_VALUES, expected1, res1, TOL);

    // OTM call option
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2015, 1, 5, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[2]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 4125.0; // OTM
    EquityIndexOptionDefinition targetOption2Dfn = new EquityIndexOptionDefinition(true, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityIndexOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract2 = targetOption2.accept(PV_AMERICAN, data);
    Double pv2 = targetOption2.accept(PV_AMERICAN, data) * NOTIONAL;
    GreekResultCollection greeks2 = targetOption2.accept(GREEKS_AMERICAN, data);
    double delta2 = greeks2.get(Greek.DELTA);
    double gamma2 = greeks2.get(Greek.GAMMA);
    double theta2 = greeks2.get(Greek.THETA);
    double rho2 = greeks2.get(Greek.RHO);
    double vega2 = greeks2.get(Greek.VEGA);
    double positionDelta2 = greeks2.get(Greek.DELTA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionGamma2 = greeks2.get(Greek.GAMMA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionTheta2 = greeks2.get(Greek.THETA) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionRho2 = greeks2.get(Greek.RHO) * targetOption2.getUnitAmount() * NOTIONAL;
    double positionVega2 = greeks2.get(Greek.VEGA) * targetOption2.getUnitAmount() * NOTIONAL;
    double[] res2 = new double[] {pvPerContract2, pv2, delta2, gamma2, theta2, rho2, vega2, positionDelta2,
        positionGamma2, positionTheta2, positionRho2, positionVega2 };
    double[] expected2 = new double[] {4637.1269429228505, 231856.34714614254, 0.2789490853773786,
        0.0012070495167825143, -0.6173922626019448, 2.0763853801397287, 5.975975135977502, 1394.745426886893,
        6.035247583912572, -3086.961313009724, 10381.926900698643, 29879.87567988751 };
    assertEqualsArray(COMPUTE_VALUES, expected2, res2, TOL);
  }

  /**
   * European options, vol surface is constructed by using OTM options
   */
  @Test
  public void europeanTest() {
    // construct data bundle
    ExerciseDecisionType exerciseType = ExerciseDecisionType.EUROPEAN;
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
    boolean isAmerican = exerciseType == ExerciseDecisionType.AMERICAN ? true : false;
    double[] timeToExpiries = toDateToDouble(TRADE_DATE, EXPIRY_DATES);
    double[] futureMaturities = toDateToDouble(TRADE_DATE, FUTURE_MATURITY_DATES);
    ForwardCurve forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(futureMaturities,
        FUTURE_PRICES, FORWARD_INTERPOLATOR));
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, STRIKES, optionPrices, isCalls,
        forwardCurve, isAmerican);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    // ITM put option
    ZonedDateTime expiryDate1 = EXPIRY_DATES[1];
    LocalDate settlementDate1 = BusinessDayDateUtils.addWorkDays(expiryDate1.toLocalDate(), 3, NYC);
    double targetStrike1 = 3825.0; // ITM 
    EquityIndexOptionDefinition targetOption1Dfn = new EquityIndexOptionDefinition(false, targetStrike1, USD,
        exerciseType, expiryDate1, settlementDate1, POINT_VALUE, SettlementType.PHYSICAL);
    EquityIndexOption targetOption1 = targetOption1Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract1 = targetOption1.accept(PV_EUROPEAN, data);
    Double pv1 = targetOption1.accept(PV_EUROPEAN, data) * NOTIONAL;
    Double delta1 = targetOption1.accept(DELTA_EUROPEAN, data);
    Double gamma1 = targetOption1.accept(GAMMA_EUROPEAN, data);
    Double theta1 = targetOption1.accept(THETA_EUROPEAN, data);
    Double rho1 = targetOption1.accept(RHO_EUROPEAN, data);
    Double vega1 = targetOption1.accept(VEGA_EUROPEAN, data);
    double positionDelta1 = delta1 * targetOption1.getUnitAmount() * NOTIONAL;
    double positionGamma1 = gamma1 * targetOption1.getUnitAmount() * NOTIONAL;
    double positionTheta1 = theta1 * targetOption1.getUnitAmount() * NOTIONAL;
    double positionRho1 = rho1 * targetOption1.getUnitAmount() * NOTIONAL;
    double positionVega1 = vega1 * targetOption1.getUnitAmount() * NOTIONAL;
    double[] res1 = new double[] {pvPerContract1, pv1, delta1, gamma1, theta1, rho1, vega1, positionDelta1,
        positionGamma1, positionTheta1, positionRho1, positionVega1 };
    double[] expected1 = new double[] {7003.3169380080935, 350165.84690040466, -0.3279391186544159,
        0.0011583181545821923, -1.0045632814954233, 213.34367882294566, 563.8919267066684, -1639.6955932720793,
        5.791590772910961, -5022.816407477117, 1066718.3941147283, 2819459.633533342 };
    assertEqualsArray(COMPUTE_VALUES, expected1, res1, TOL);

    // ITM call option
    ZonedDateTime expiryDate2 = ZonedDateTime.of(2015, 1, 5, 15, 0, 0, 0, ZID); // between expiryDates[1] and expiryDates[2]
    LocalDate settlementDate2 = BusinessDayDateUtils.addWorkDays(expiryDate2.toLocalDate(), 3, NYC);
    double targetStrike2 = 4125.0; // ITM
    EquityIndexOptionDefinition targetOption2Dfn = new EquityIndexOptionDefinition(false, targetStrike2, USD,
        exerciseType, expiryDate2, settlementDate2, 100.0, SettlementType.PHYSICAL);
    EquityIndexOption targetOption2 = targetOption2Dfn.toDerivative(TRADE_DATE);
    Double pvPerContract2 = targetOption2.accept(PV_EUROPEAN, data);
    Double pv2 = targetOption2.accept(PV_EUROPEAN, data) * NOTIONAL;
    Double delta2 = targetOption2.accept(DELTA_EUROPEAN, data);
    Double gamma2 = targetOption2.accept(GAMMA_EUROPEAN, data);
    Double theta2 = targetOption2.accept(THETA_EUROPEAN, data);
    Double rho2 = targetOption2.accept(RHO_EUROPEAN, data);
    Double vega2 = targetOption2.accept(VEGA_EUROPEAN, data);
    double positionDelta2 = delta2 * targetOption2.getUnitAmount() * NOTIONAL;
    double positionGamma2 = gamma2 * targetOption2.getUnitAmount() * NOTIONAL;
    double positionTheta2 = theta2 * targetOption2.getUnitAmount() * NOTIONAL;
    double positionRho2 = rho2 * targetOption2.getUnitAmount() * NOTIONAL;
    double positionVega2 = vega2 * targetOption2.getUnitAmount() * NOTIONAL;
    double[] res2 = new double[] {pvPerContract2, pv2, delta2, gamma2, theta2, rho2, vega2, positionDelta2,
        positionGamma2, positionTheta2, positionRho2, positionVega2 };
    double[] expected2 = new double[] {22305.75749980942, 1115287.874990471, -0.7201304521915648,
        0.0012043819567103293, -0.6708394805504454, 622.255200471624, 597.281853757696, -3600.652260957824,
        6.021909783551646, -3354.1974027522274, 3111276.00235812, 2986409.26878848 };
    assertEqualsArray(COMPUTE_VALUES, expected2, res2, TOL);
  }
}
