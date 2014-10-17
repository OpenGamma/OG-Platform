/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;

import java.util.ArrayList;

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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.businessday.BusinessDayDateUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class EquityOptionE2ETest {
  private static final Currency USD = Currency.USD;
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final ZoneId ZID = ZoneId.of("EST");
  private static final ZonedDateTime TRADE_DATE = ZonedDateTime.of(2014, 3, 26, 13, 46, 0, 0, ZID);

  private static final String INTERPOLATOR_NAME = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private static final String LEFT_EXTRAPOLATOR_NAME = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final String RIGHT_EXTRAPOLATOR_NAME = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final double[] SINGLE_CURVE_TIME = new double[] {0.002739726, 0.093150685, 0.257534247, 0.515068493,
      1.005479452, 2.009416873, 3.005479452, 4.005479452, 5.005479452, 6.006684632, 7.010958904, 8.008219178,
      9.005479452, 10.00668463, 12.00547945, 15.00547945, 20.00547945 };
  private static final double[] SINGLE_CURVE_RATE = new double[] {0.001774301, 0.000980829, 0.000940143, 0.001061566,
      0.001767578, 0.005373189, 0.009795971, 0.013499667, 0.016397755, 0.018647803, 0.020528999, 0.022002859,
      0.023322553, 0.024538027, 0.026482704, 0.028498622, 0.030369559 };
  private static final String SINGLE_CURVE_NAME = "Single Curve";
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      INTERPOLATOR_NAME, LEFT_EXTRAPOLATOR_NAME, RIGHT_EXTRAPOLATOR_NAME);
  private static final InterpolatedDoublesCurve INTERPOLATED_CURVE = InterpolatedDoublesCurve.from(SINGLE_CURVE_TIME,
      SINGLE_CURVE_RATE, INTERPOLATOR);
  private static final YieldAndDiscountCurve SINGLE_CURVE = new YieldCurve(SINGLE_CURVE_NAME, INTERPOLATED_CURVE);

  private static final BjerksundStenslandModel AMERICAN_MODEL = new BjerksundStenslandModel();
  private final static CombinedInterpolatorExtrapolator EXPIRY_INTERPOLATOR = getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final CombinedInterpolatorExtrapolator STRIKE_INTERPOLATOR = getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  private static final double SPOT = 56.0;

  @Test(enabled = false)
  public void dividendOnlyCallTest() {
    ZonedDateTime[] expiryDates = new ZonedDateTime[] {ZonedDateTime.of(2014, 3, 28, 15, 0, 0, 0, ZID),
        ZonedDateTime.of(2014, 4, 11, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 4, 25, 15, 0, 0, 0, ZID),
        ZonedDateTime.of(2014, 5, 9, 15, 0, 0, 0, ZID), ZonedDateTime.of(2014, 5, 23, 15, 0, 0, 0, ZID),
        ZonedDateTime.of(2014, 6, 20, 15, 0, 0, 0, ZID) };
    ZonedDateTime[] dividendDates = new ZonedDateTime[] {ZonedDateTime.of(2014, 4, 2, 0, 0, 0, 0, ZID),
        ZonedDateTime.of(2014, 7, 1, 0, 0, 0, 0, ZID) };

    double[][] strikes = new double[][] { {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
        {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
        {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
        {51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0 },
        {52.5, 55, 57.5, 60 }, {52.5, 55, 57.5, 60 } };
    double[][] marketPrices = new double[][] { {5.1, 4.1, 3.11, 2.05, 1.08, 0.45, 0.105, 0.05, 0.025, 0.02 },
        {5.125, 4.225, 3.35, 2.63, 1.855, 1.185, 0.7, 0.375, 0.19, 0.11 },
        {5.325, 4.45, 3.575, 2.965, 2.205, 1.535, 1.075, 0.625, 0.385, 0.245 },
        {5.65, 4.75, 3.925, 3.15, 2.475, 1.83, 1.3, 0.89, 0.585, 0.355 },
        {4.85, 3.02, 1.575, 0.67 }, {5.15, 3.425, 2.045, 1.065 } };
    boolean[][] isCalls = new boolean[][] { {true, true, true, true, true, true, true, true, true, true },
        {true, true, true, true, true, true, true, true, true, true },
        {true, true, true, true, true, true, true, true, true, true },
        {true, true, true, true, true, true, true, true, true, true },
        {true, true, true, true }, {true, true, true, true, } };

    double[] timeToExpiries = toDateToDouble(expiryDates);
    double[] tau = toDateToDouble(dividendDates);
    double[] alpha = new double[] {0.38, 0.4 };
    double[] beta = new double[] {0.0, 0.0 };
    AffineDividends dividend = new AffineDividends(tau, alpha, beta);
    ForwardCurveAffineDividends forwardCurve = new ForwardCurveAffineDividends(SPOT, SINGLE_CURVE, dividend);
    BlackVolatilitySurfaceStrike volSurface = createSurface(SPOT, timeToExpiries, strikes, marketPrices, isCalls,
        forwardCurve, false);
    StaticReplicationDataBundle data = new StaticReplicationDataBundle(volSurface, SINGLE_CURVE, forwardCurve);

    int nStrikes = 100;
    int nTimes = 100;
    double[] str = new double[nStrikes];
    double[] tm = new double[nTimes];
    double minStr = 50.0;
    double maxStr = 61.0;
    for (int i = 0; i < nStrikes; ++i) {
      str[i] = minStr + (maxStr - minStr) / (nStrikes - 1) * i;
    }
    double minTm = 0.001;
    double maxTm = 0.3;
    for (int j = 0; j < nTimes; ++j) {
      tm[j] = minTm + (maxTm - minTm) / (nTimes - 1) * j;
    }

    for (int j = 0; j < nTimes; ++j) {
      System.out.print("\t" + tm[j]);
    }
    System.out.println();
    for (int i = 0; i < nStrikes; ++i) {
      System.out.print(str[i]);
      for (int j = 0; j < nTimes; ++j) {
        System.out.print("\t" + volSurface.getVolatility(tm[j], str[i]));
      }
      System.out.println();
    }

    double targetExpiry = timeToExpiries[1];
    LocalDate settlementDate = BusinessDayDateUtils.addWorkDays(expiryDates[1].toLocalDate(), 3, NYC);
    double settlement = TimeCalculator.getTimeBetween(TRADE_DATE, settlementDate.atStartOfDay(ZID), DayCounts.ACT_365);
    double targetStrike = 52.5;
    EquityOption option = new EquityOption(targetExpiry, settlement, targetStrike, true, USD, 100.0,
        ExerciseDecisionType.AMERICAN, SettlementType.PHYSICAL);
 }

  private BlackVolatilitySurfaceStrike createSurface(double spot, double[] timeToExpiries, double[][] strikes,
      double[][] marketPrices, boolean[][] isCalls, ForwardCurveAffineDividends forwardCurve, boolean isAmerican) {
    int nExpiry = timeToExpiries.length;
    ArrayList<Double> expiryList = new ArrayList<>();
    ArrayList<Double> strikeList = new ArrayList<>();
    ArrayList<Double> impliedVolList = new ArrayList<>();
    for (int i = 0; i < nExpiry; ++i) {
      double expiry = timeToExpiries[i];
      int nStrikes = strikes[i].length;
      for (int j = 0; j < nStrikes; ++j) {
        if (!Double.isNaN(marketPrices[i][j])) {
          double strike = strikes[i][j];
          expiryList.add(expiry);
          strikeList.add(strike);
          double interestRate = SINGLE_CURVE.getInterestRate(expiry);
          double costOfCarry = Math.log(forwardCurve.getForward(expiry) / spot) / expiry;
          boolean isCall = isCalls[i][j];
          double impliedVol;
          if (isAmerican) {
            impliedVol = AMERICAN_MODEL.impliedVolatility(marketPrices[i][j], spot, strike, interestRate,
                costOfCarry, expiry, isCall);
          } else {
            double df = SINGLE_CURVE.getDiscountFactor(expiry);
            double fwdPrice = marketPrices[i][j] / df;
            double fwd = forwardCurve.getForward(expiry);
            impliedVol = BlackFormulaRepository.impliedVolatility(fwdPrice, fwd, strike, expiry, isCall);
          }
          impliedVolList.add(impliedVol);
        }
      }
    }

    InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(expiryList, strikeList, impliedVolList,
        new GridInterpolator2D(EXPIRY_INTERPOLATOR, STRIKE_INTERPOLATOR));
    return new BlackVolatilitySurfaceStrike(surface);
  }

  private double[] toDateToDouble(ZonedDateTime[] targetDates) {
    int nDates = targetDates.length;
    double[] res = new double[nDates];
    for (int i = 0; i < nDates; ++i) {
      res[i] = TimeCalculator.getTimeBetween(TRADE_DATE, targetDates[i], DayCounts.ACT_365);
    }
    return res;
  }

}
