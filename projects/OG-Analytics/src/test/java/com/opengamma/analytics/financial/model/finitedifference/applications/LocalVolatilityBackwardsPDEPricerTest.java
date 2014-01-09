/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.surface.MixedLogNormalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.PriceSurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LocalVolatilityBackwardsPDEPricerTest {

  private static final LocalVolatilityBackwardsPDEPricer PRICER = new LocalVolatilityBackwardsPDEPricer();
  private static final ForwardCurve FWD_CURVE;
  private static final YieldAndDiscountCurve DIS_CURVE;
  private static final Curve<Double, Double> RISK_FREE_CURVE;
  private static final LocalVolatilitySurfaceStrike LOCAL_VOL_SUR;
  private static final double S0 = 10.0;
  private static final double R = 0.07;
  private static final double T = 2.0;
  private static final double SIGMA_HAT;

  static {
    final Integrator1D<Double, Double> integrator = new RungeKuttaIntegrator1D();

    FWD_CURVE = new ForwardCurve(S0, R);
    RISK_FREE_CURVE = ConstantDoublesCurve.from(R);
    final Function1D<Double, Double> df = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        return Math.exp(-t * R);
      }
    };

    DIS_CURVE = new DiscountCurve("discount", FunctionalDoublesCurve.from(df));

    final Function1D<Double, Double> volTS = new Function1D<Double, Double>() {

      private final static double a = -0.1;
      private final static double b = 0.3;
      private final static double c = 0.4;
      private final static double d = 0.3;

      @Override
      public Double evaluate(final Double t) {
        final double tau = T - t;
        return (a + b * tau) * Math.exp(-c * tau) + d;
      }
    };

    final Function1D<Double, Double> vol2TS = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        final double vol = volTS.evaluate(t);
        return vol * vol;
      }
    };

    final Function<Double, Double> vol = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(final Double t, final Double s) {
        return volTS.evaluate(t);
      }
    };

    LOCAL_VOL_SUR = new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(vol));
    SIGMA_HAT = Math.sqrt(integrator.integrate(vol2TS, 0.0, T) / T);

  }

  /**
   * Here the vol surface is flat in the spot direction
   */
  @Test
  public void volTermStructureTest() {

    final double k = 14.0;
    final boolean isCall = true;

    final int tNodes = 150;
    int nu = 80;
    int xNodes = nu * tNodes;

    final double df = DIS_CURVE.getDiscountFactor(T);
    final double fwd = FWD_CURVE.getForward(T);
    final double bsPrice = df * BlackFormulaRepository.price(fwd, k, T, SIGMA_HAT, isCall);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, isCall);

    double pdePrice = PRICER.price(FWD_CURVE, RISK_FREE_CURVE, option, LOCAL_VOL_SUR, isCall, xNodes, tNodes);
    //    double relErr = Math.abs((pdePrice - bsPrice) / bsPrice);

    // System.out.println(bsPrice+"\t"+pdePrice+"\t"+relErr);
    assertEquals(bsPrice, pdePrice, 1e-5 * bsPrice);

    // with a better setup grid we can use one 20th of the number of nodes (i.e. computation is 20 times faster) for the same accuracy
    nu = 4;
    xNodes = nu * tNodes;

    pdePrice = PRICER.price(FWD_CURVE, RISK_FREE_CURVE, option, LOCAL_VOL_SUR, isCall, xNodes, tNodes, 0.1, 0.0, 5.0);
    //    relErr = Math.abs((pdePrice - bsPrice) / bsPrice);
    // System.out.println(bsPrice+"\t"+pdePrice+"\t"+relErr);
    assertEquals(bsPrice, pdePrice, 1e-5 * bsPrice);
  }

  /**
   * Here the vol surface is flat in the time direction
   * The dynamics of a CEV process are $df_t = \sigma_{\beta} f_t^{\beta} dW$ where $f$ is the forward ($f(t,T)$) for some expiry $T$.
   * For this test we'd like to work with spot rather that forward - for a deterministic rate (and zero yield), the forward and spot are related by
   * $f_t = R_t S_t$ where $R_t = \exp(\int_t^T r_s dt)$. The dynamics of spot are $\frac{dS_t}{S_t} = r_t dt + \sigma_{\beta} (R_t S_t)^{\beta-1} dW$.
   * This means we can treat the local volatility as $\sigma(t,S_t) = \sigma_{\beta} (R_t S_t)^{\beta-1}$
   */
  @Test
  public void cevTest() {
    final CEVPriceFunction cev = new CEVPriceFunction();
    final double k = 14.0;
    final boolean isCall = true;

    final int tNodes = 100;
    int nu = 80;
    int xNodes = nu * tNodes;

    final double df = DIS_CURVE.getDiscountFactor(T);
    final double fwd = FWD_CURVE.getForward(T);

    final double sigma = 1.0;
    final double beta = 0.5;
    final Function<Double, Double> vol = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(final Double t, final Double s) {
        final double tau = T - t;
        final double rt = Math.exp(tau * R);
        return sigma * Math.pow(rt * s, beta - 1.0);
      }
    };

    final LocalVolatilitySurfaceStrike volSurf = new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(vol));
    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, isCall);
    final CEVFunctionData data = new CEVFunctionData(fwd, df, sigma, beta);

    final Function1D<CEVFunctionData, Double> priceFunc = cev.getPriceFunction(option);
    final double cevPrice = priceFunc.evaluate(data);

    double pdePrice = PRICER.price(FWD_CURVE, RISK_FREE_CURVE, option, volSurf, isCall, xNodes, tNodes);
    //    double relErr = Math.abs((pdePrice - cevPrice) / cevPrice);

    // System.out.println(cevPrice + "\t" + pdePrice + "\t" + relErr);
    assertEquals(cevPrice, pdePrice, 1e-5 * cevPrice);

    // here only a 5 times speed up is possible
    nu = 15;
    xNodes = nu * tNodes;
    pdePrice = PRICER.price(FWD_CURVE, RISK_FREE_CURVE, option, volSurf, isCall, xNodes, tNodes, 0.1, 0.0, 4.0);
    //    relErr = Math.abs((pdePrice - cevPrice) / cevPrice);
    // System.out.println(cevPrice + "\t" + pdePrice + "\t" + relErr);
    assertEquals(cevPrice, pdePrice, 1e-5 * cevPrice);
  }

  @Test
  public void mixedLogNormalTest() {

    final double[] w = new double[] {0.7, 0.25, 0.05 };
    final double[] sigma = new double[] {0.3, 0.6, 1.0 };
    final double[] mu = new double[] {0.0, 0.3, -0.5 };
    //    double[] w = new double[] {0.99, 0.01, 0.0000};
    //    double[] sigma = new double[] {0.3, 0.5, 0.8};
    //  double[] mu = new double[] {0.0, 0.0, -0.0};
    final MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma, mu);
    final PriceSurface priceSurf = MixedLogNormalVolatilitySurface.getPriceSurface(FWD_CURVE, DIS_CURVE, data);
    final LocalVolatilitySurfaceStrike locVol = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(FWD_CURVE, data);

    final double k = 14.0;
    final boolean isCall = true;

    final int tNodes = 50;
    final int nu = 20;
    final int xNodes = nu * tNodes;

    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, isCall);
    final double pdePrice = PRICER.price(FWD_CURVE, RISK_FREE_CURVE, option, locVol, isCall, xNodes, tNodes, 0.05, 0.0, 8.0);
    final double mlnPrice = priceSurf.getPrice(T, k);

    //    double relErr = Math.abs((pdePrice - mlnPrice) / mlnPrice);
    //     System.out.println(mlnPrice + "\t" + pdePrice + "\t" + relErr);
    assertEquals(mlnPrice, pdePrice, 5e-3 * mlnPrice);
  }

}
