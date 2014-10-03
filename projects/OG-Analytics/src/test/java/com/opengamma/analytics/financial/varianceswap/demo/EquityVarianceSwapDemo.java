/**
 * /**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.varianceswap.demo;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.getWorkingDaysInclusive;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwapDefinition;
import com.opengamma.analytics.financial.equity.variance.pricing.RealizedVariance;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.instrument.varianceswap.VarianceSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayDateUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.money.Currency;

/**
 * This class is designed to demonstrate some of the basic coverage of (equity) variance swaps in the analytics.<p>
 * The first <i>test</i> builds an {@link EquityVarianceSwapDefinition} - essentially the representation of the swap as it 
 * would appear on a term sheet. This is turned into an <i>analytics</i> representation (an object containing just primitive
 * data types), which can be passed to <i>calculators</i>. We simulate some observations (the closing price) over the life
 * of the swap, and check that the payoff calculated by the {@link RealizedVariance} calculator is correct.<p>
 * 
 * The second test prices a variance swap both before the first observation and after some (but not all) observations have
 * been made, using a flat volatility surface. The calculation involves the integral of $C(k)/k^2$ from zero to infinity
 * (in practice some cut-off), where $C(K)$ is price of a vanilla option (derived from the volatility surface) - this is
 * <i>static replication</i> of a derivation with a pay-off proportional to the log of the terminal value of the underlying. 
 * Since the volatility is constant, the expected variance is just the square of this, so we can easily check our calculations.<p> 
 * 
 * The third test is similar to the second, except the volatility surface is no longer flat and instead comes from a
 * mixed log-normal model; again this gives us a simple analytic result to compare our calculation against.  <p>  
 * 
 * The fourth test is more realistic; we start with some parameters of the SABR model and from this get the (Black)
 * volatility at nine strikes around (and including) ATMF. This 'market data' is then our starting point. We use two 
 * different smile interpolation/extrapolation methods to reconstruct the smile from nine 'market prices' of vanilla
 * options. These smiles are used to calculate the expected variance (via static replication), and we compare the
 * these results with that derived from our knowledge of the SABR parameters. We do not recover exactly the 'true' answer,
 * and the different interpolation methods give different results - this is to be expected. <p>
 * 
 * Despite the name, these tests are not specific to <b>equity</b> variance swaps; they are equally valid of variance
 * swaps in other asset classes. Where equity will differ from say FX, is in discrete dividends. The implicit assumption
 * in these tests is that dividends are paid continuously (i.e. a dividend yield) and this information is contained in the
 * forward curve. We have a model to handle discrete dividends @see <a>href ="http://developers.opengamma.com/quantitative-research/Equity-Variance-Swaps-with-Dividends-OpenGamma.pdf">here</a> 
 * but do not give examples in this demo.
 */
public class EquityVarianceSwapDemo {
  private static final RealizedVariance REALIZED_VOL_CAL = new RealizedVariance();
  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication();
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1, RANDOM);
  private static final ZoneId UTC = ZoneId.of("UTC");

  private static final double s_Spot = 80;
  private static final double s_Drift = 0.05;
  private static final double s_Vol = 0.3;
  private static final ForwardCurve s_FwdCurve = new ForwardCurve(s_Spot, s_Drift);
  private static final YieldAndDiscountCurve s_DiscountCurve = new YieldCurve("Discount", ConstantDoublesCurve.from(s_Drift));
  private static final BlackVolatilitySurfaceStrike s_FlatVolSurf = new BlackVolatilitySurfaceStrike(ConstantDoublesSurface.from(s_Vol));

  private static final ZonedDateTime s_ObsStartTime = ZonedDateTime.of(2013, 12, 16, 12, 0, 0, 0, UTC);// ZonedDateTime.of(2013, 7, 27, 12, 0, 0, 0, UTC); // Saturday
  private static final ZonedDateTime s_ObsEndTime = ZonedDateTime.of(2015, 7, 30, 12, 0, 0, 0, UTC); // Thursday
  private static final ZonedDateTime s_SettlementTime = ZonedDateTime.of(2015, 8, 3, 12, 0, 0, 0, UTC);// Monday
  private static final Currency s_Ccy = Currency.EUR;
  private static final Calendar s_Calendar = new CalendarTarget("Eur");
  private static final double s_AnnualizationFactor = 252.0;
  private static final double s_VolStrike = 0.3;
  private static final double s_VolNotional = 1e6;

  /**
   * Demonstrate building an equity variance swap and adding time series of observations. Check that the realized variance
   * and present value of the swap are as expected when all the observations are known
   */
  @Test(description = "Demo")
  public void buildSwap() {
    EquityVarianceSwapDefinition def = new EquityVarianceSwapDefinition(s_ObsStartTime, s_ObsEndTime, s_SettlementTime, s_Ccy, s_Calendar, s_AnnualizationFactor, s_VolStrike, s_VolNotional, false);

    ZonedDateTime obsDate = ZonedDateTime.of(2014, 8, 11, 12, 0, 0, 0, UTC);
    EquityVarianceSwap varSwap = def.toDerivative(obsDate);
    System.out.println("time to observation start: " + varSwap.getTimeToObsStart());
    System.out.println("time to observation end: " + varSwap.getTimeToObsEnd());
    System.out.println("time to settlement: " + varSwap.getTimeToSettlement());
    System.out.println("Var Notional: " + varSwap.getVarNotional());
    System.out.println("Vol Notional: " + varSwap.getVolNotional());
    System.out.println("Annualization Factor: " + varSwap.getAnnualizationFactor());

    // we haven't added any observations, so all historical observations are treated as disrupted
    System.out.println("Observations disrupted: " + varSwap.getObsDisrupted());
    System.out.println("Observations expected: " + varSwap.getObsExpected());
    double[] obs = varSwap.getObservations();
    System.out.println("Observations: " + obs.length);

    // now add some randomly generated observations, and compute some values on the settlement date (i.e. all observations
    // are in the past)
    int observationDays = getWorkingDaysInclusive(s_ObsStartTime, s_ObsEndTime, s_Calendar);

    System.out.println("\nObsevations added: " + observationDays);
    LocalDate[] dates = new LocalDate[observationDays];
    double[] Prices = new double[observationDays];
    double[] logPrices = new double[observationDays];

    double dailyDrift = (s_Drift - 0.5 * s_Vol * s_Vol) / s_AnnualizationFactor;
    double dailySD = s_Vol / Math.sqrt(s_AnnualizationFactor);

    dates[0] = s_ObsStartTime.toLocalDate();
    Prices[0] = 100.0;
    logPrices[0] = Math.log(100.0);
    double sum2 = 0;
    for (int i = 1; i < observationDays; i++) {
      dates[i] = BusinessDayDateUtils.addWorkDays(dates[i - 1], 1, s_Calendar);
      logPrices[i] = logPrices[i - 1] + dailyDrift + dailySD * NORMAL.nextRandom();
      Prices[i] = Math.exp(logPrices[i]);
      double rtn = logPrices[i] - logPrices[i - 1];
      sum2 += rtn * rtn;
    }

    LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, Prices);
    varSwap = def.toDerivative(s_SettlementTime, ts);

    System.out.println("Observations disrupted: " + varSwap.getObsDisrupted());
    System.out.println("Observations expected: " + varSwap.getObsExpected());
    obs = varSwap.getObservations();
    System.out.println("Observations: " + obs.length);

    double relVar = REALIZED_VOL_CAL.evaluate(varSwap);
    // even with B-S dynamics, the realized variance will differ from the expected variance
    System.out.println("Expected variance: " + s_Vol * s_Vol + ", realized variance: " + relVar);
    double calRelVar = s_AnnualizationFactor / (observationDays - 1) * sum2;
    assertEquals(calRelVar, relVar, 1e-15); // check the calculation inside RealizedVariance is correct

    // check the price computed by VarianceSwapStaticReplication is as expected when all the observations are known
    double calPV = s_VolNotional / 2 / s_VolStrike * (calRelVar - s_VolStrike * s_VolStrike);
    StaticReplicationDataBundle market = new StaticReplicationDataBundle(s_FlatVolSurf, s_DiscountCurve, s_FwdCurve);
    double pv = PRICER.presentValue(varSwap, market);
    System.out.println("Variance swap value at settlement: " + pv);
    assertEquals(calPV, pv, 1e-9);
  }

  /**
   * The expected variance is computed by static replication - integration over vanilla option prices. These prices are
   * derived from a volatility surface which is flat at 30% - hence we should recover (up to some numerical tolerance)
   * 0.3^2 for the expected variance.
   */
  @Test(description = "Demo")
  public void flatVolPrice() {
    VarianceSwapDefinition def = new VarianceSwapDefinition(s_ObsStartTime, s_ObsEndTime, s_SettlementTime, s_Ccy, s_Calendar, s_AnnualizationFactor, s_VolStrike, s_VolNotional);
    ZonedDateTime valueDate = ZonedDateTime.of(2013, 7, 25, 12, 0, 0, 0, UTC); // before first observation
    VarianceSwap varSwap = def.toDerivative(valueDate);

    assertEquals(0.0, REALIZED_VOL_CAL.evaluate(varSwap)); // No observations yet made, so zero realized volatility

    StaticReplicationDataBundle market = new StaticReplicationDataBundle(s_FlatVolSurf, s_DiscountCurve, s_FwdCurve);
    assertEquals(s_Vol * s_Vol, PRICER.expectedVariance(varSwap, market), 1e-10);

    // now look at a seasoned swap
    valueDate = ZonedDateTime.of(2014, 1, 28, 12, 0, 0, 0, UTC); // Tue

    // Don't include the valueDate in the observations
    int observationDays = BusinessDayDateUtils.getDaysBetween(s_ObsStartTime, valueDate, s_Calendar);

    System.out.println("\nObsevations added: " + observationDays);
    LocalDate[] dates = new LocalDate[observationDays];
    double[] Prices = new double[observationDays];
    double[] logPrices = new double[observationDays];

    double dailyDrift = (s_Drift - 0.5 * s_Vol * s_Vol) / s_AnnualizationFactor;
    double dailySD = s_Vol / Math.sqrt(s_AnnualizationFactor);

    dates[0] = s_ObsStartTime.toLocalDate();
    Prices[0] = 100.0;
    logPrices[0] = Math.log(100.0);
    double sum2 = 0;
    for (int i = 1; i < observationDays; i++) {
      dates[i] = BusinessDayDateUtils.addWorkDays(dates[i - 1], 1, s_Calendar);
      logPrices[i] = logPrices[i - 1] + dailyDrift + dailySD * NORMAL.nextRandom();
      Prices[i] = Math.exp(logPrices[i]);
      double rtn = logPrices[i] - logPrices[i - 1];
      sum2 += rtn * rtn;
    }

    LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, Prices);
    varSwap = def.toDerivative(valueDate, ts);

    double relVar = (observationDays - 1) / s_AnnualizationFactor * REALIZED_VOL_CAL.evaluate(varSwap);
    assertEquals(relVar, sum2, 1e-14);

    // Compute the price using the observations we have and the knowledge that volatility surface is flat (at 30%), and
    // compare this with the result of the calculator (which integrates over the vanilla option prices)
    double df = market.getDiscountCurve().getDiscountFactor(varSwap.getTimeToSettlement());
    double expVar = (s_AnnualizationFactor * sum2 + s_Vol * s_Vol * (varSwap.getObsExpected() - observationDays)) / (varSwap.getObsExpected() - 1);
    double expPV = df * s_VolNotional / 2 / s_VolStrike * (expVar - s_VolStrike * s_VolStrike);
    double pv = PRICER.presentValue(varSwap, market);
    assertEquals(expPV, pv, 1e-5);
  }

  /**
   * A mixed log-normal model can give realistic looking smiles. It also allows a very simple analytic calculation of the
   * expected variance. This can be compared with the calculator that just 'sees' a volatility surface
   */
  @Test(description = "Demo")
  public void testMixedLogNormalVolSurface() {

    final double sigma1 = 0.2;
    final double sigma2 = 1.0;
    final double w = 0.9;

    Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... x) {
        double t = x[0];
        double k = x[1];
        @SuppressWarnings("synthetic-access")
        double fwd = s_FwdCurve.getForward(t);
        boolean isCall = k > fwd;
        double price = w * BlackFormulaRepository.price(fwd, k, t, sigma1, isCall) + (1 - w) * BlackFormulaRepository.price(fwd, k, t, sigma2, isCall);
        if (price < 1e-100) {
          return sigma2;
        }
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, t, isCall);
      }
    };

    // with a mixed log-normal, the expected variance is trivial
    double expected = w * sigma1 * sigma1 + (1 - w) * sigma2 * sigma2;

    VarianceSwapDefinition def = new VarianceSwapDefinition(s_ObsStartTime, s_ObsEndTime, s_SettlementTime, s_Ccy, s_Calendar, s_AnnualizationFactor, s_VolStrike, s_VolNotional);
    VarianceSwap varSwap = def.toDerivative(s_ObsStartTime);

    BlackVolatilitySurfaceStrike surfaceStrike = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));
    double strikeVal = PRICER.expectedVariance(varSwap, new StaticReplicationDataBundle(surfaceStrike, s_DiscountCurve, s_FwdCurve));
    assertEquals("strike", expected, strikeVal, 5e-12);

    // convert the vol surface to one parameterised by delta
    BlackVolatilitySurfaceDelta surfaceDelta = BlackVolatilitySurfaceConverter.toDeltaSurface(surfaceStrike, s_FwdCurve);
    double deltaVal = PRICER.expectedVariance(varSwap, new StaticReplicationDataBundle(surfaceDelta, s_DiscountCurve, s_FwdCurve));
    assertEquals("delta", expected, deltaVal, 5e-8);
    // comment - convection to the delta surface involves root-finding, so the delta surface is less accurate, not the method using
    // the delta surface
  }

  /**
   * So far we have assumed that a volatility surface (valid for strikes from zero to infinity) is known. In practice we
   * will have a finite set of vanilla option prices. Assume initially that the expiry of these options coincides with the
   * expiry of the variance swap.
   */
  @Test(description = "Demo")
  public void discreteOptionPricesTest() {

    VarianceSwapDefinition def = new VarianceSwapDefinition(s_ObsStartTime, s_ObsEndTime, s_SettlementTime, s_Ccy, s_Calendar, s_AnnualizationFactor, s_VolStrike, s_VolNotional);
    VarianceSwap varSwap = def.toDerivative(s_ObsStartTime);
    double expiry = varSwap.getTimeToObsEnd();
    double fwd = s_FwdCurve.getForward(expiry);
    double df = s_DiscountCurve.getDiscountFactor(varSwap.getTimeToSettlement());
    double[] strikes = new double[] {50.0, 60.0, 70.0, 80.0, fwd, 90.0, 100.0, 120.0, 150.0 };

    double alpha = 0.2;
    double beta = 1.0;
    double rho = -0.0;
    double nu = 0.5;
    SABRFormulaData sabr = new SABRFormulaData(alpha, beta, rho, nu);
    VolatilityFunctionProvider<SABRFormulaData> volFunPro = new SABRHaganVolatilityFunction();
    Function1D<SABRFormulaData, double[]> func = volFunPro.getVolatilityFunction(fwd, strikes, expiry);
    double[] blackVols = func.evaluate(sabr);

    GeneralSmileInterpolator smileInterpolator = new SmileInterpolatorSpline();
    Function1D<Double, Double> smileFunc = smileInterpolator.getVolatilityFunction(fwd, strikes, expiry, blackVols);
    BlackVolatilitySurface<?> volSurface = makeSurfaceFromSmile(smileFunc);
    StaticReplicationDataBundle market = new StaticReplicationDataBundle(volSurface, s_DiscountCurve, s_FwdCurve);

    //For the case of  beta = 1.0 & rho = 0.0, we know exactly the expected variance of a variance swap under SABR dynamics 
    double alpha2 = alpha*alpha;
    double nu2 = nu*nu;
    double analExpVar = alpha2*(Math.exp(nu2*expiry)-1)/nu2/expiry; 

    double expVar = PRICER.expectedVariance(varSwap, market);
    System.out.println("Expected variance - exact value: "+ analExpVar+", calculated value: "+expVar);

    //now use a different smile interpolator - get a slightly different answer 
    smileInterpolator = new SmileInterpolatorSABR();
    smileFunc = smileInterpolator.getVolatilityFunction(fwd, strikes, expiry, blackVols);
    volSurface = makeSurfaceFromSmile(smileFunc);
    market = new StaticReplicationDataBundle(volSurface, s_DiscountCurve, s_FwdCurve);
    expVar = PRICER.expectedVariance(varSwap, market);
    System.out.println("Expected variance - exact value: "+ analExpVar+", calculated value: "+expVar);
  }

  private  BlackVolatilitySurface<?> makeSurfaceFromSmile(final Function1D<Double, Double> smileFunc) {
    Function<Double, Double> volFunc = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(Double t, Double k) {
        return smileFunc.evaluate(k);
      }
    };
    return  new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(volFunc));
  }
}
