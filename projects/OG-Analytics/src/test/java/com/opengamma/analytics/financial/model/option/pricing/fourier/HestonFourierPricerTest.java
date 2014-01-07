/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static com.opengamma.analytics.math.ComplexMathUtils.exp;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HestonFourierPricerTest {
  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  @Test
  public void testLowVolOfVol() {
    final double sigma = 0.36;

    final double kappa = 1.0; // mean reversion speed
    final double theta = sigma * sigma; // reversion level
    final double vol0 = theta; // start level
    final double omega = 0.001; // vol-of-vol
    final double rho = -0.3; // correlation

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final FourierPricer pricer = new FourierPricer();

    for (int i = 0; i < 21; i++) {
      final double k = 0.2 + 3.0 * i / 20.0;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k * FORWARD, T, true);
      final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
      final double price = pricer.price(data, option, heston, -0.5, 1e-6);
      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      assertEquals(sigma, impVol, 1e-3);
    }
  }

  @Test(enabled = false)
  public void testHestonCE() {

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    for (int j = 0; j < 101; j++) {
      final double y = -3. + j * 6. / 100.0;
      System.out.print("\t" + y);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      final double x = -3. + i * 6. / 100.0;
      System.out.print(x + "\t");
      for (int j = 0; j < 101; j++) {
        final double y = -3. + j * 6. / 100.0;
        final ComplexNumber res = heston.getValue(new ComplexNumber(x, y), 0.25);
        System.out.print(res.getReal() + "\t");
      }
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void testIntegrandHeston() {

    final double alpha = 0.75;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation
    final double t = 1.0;// / 52.0;
    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final EuropeanPriceIntegrand integrand = new EuropeanPriceIntegrand(heston, alpha, true);
    final BlackFunctionData data = new BlackFunctionData(1, 1, 0.5);
    for (int i = 0; i < 201; i++) {
      final double x = -0. + i * 80. / 200.0;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(2, t, true);
      final Double res = integrand.getFunction(data, option).evaluate(x);
      System.out.println(x + "\t" + res);
    }

  }

  @Test(enabled = false)
  public void testEuropeanCallFTHeston() {

    final double alpha = -0.4;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation
    final double t = 1 / 52.0;
    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final EuropeanCallFourierTransform integrand = new EuropeanCallFourierTransform(heston);
    final Function1D<ComplexNumber, ComplexNumber> func = integrand.getFunction(t);

    for (int i = 0; i < 201; i++) {
      final double x = -0. + i * 20. / 200.0;
      final ComplexNumber res = func.evaluate((new ComplexNumber(x, alpha)));
      System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    }
  }

  @Test
  public void testHeston() {
    final double alpha = 0.75;

    // parameters from the paper Not-so-complex logarithms in the Heston model
    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final double t = 1 / 12.0;

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final FourierPricer pricer = new FourierPricer();
    final BlackFunctionData data = new BlackFunctionData(1, 1, 0);
    for (int i = 0; i < 11; i++) {
      final double k = 0.5 + 1.0 * i / 10.0;

      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
      final double price = pricer.price(data, option, heston, alpha, 1e-8);
      BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      //  System.out.println(k + "\t" + impVol);
    }
  }

  @Test
  public void testHestonModelGreeks() {

    final FourierModelGreeks modelGreek = new FourierModelGreeks();

    final double alpha = -0.5;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final double forward = 1.0;
    final double t = 1 / 12.0;

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final FourierPricer pricer = new FourierPricer();
    final BlackFunctionData data = new BlackFunctionData(forward, 1, 0);

    boolean isCall;
    for (int i = 0; i < 11; i++) {
      final double k = 0.7 + 0.6 * i / 10.0;
      isCall = k >= forward;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, isCall);
      final double[] sense = modelGreek.getGreeks(data, option, heston, alpha, 1e-12);

      final double price = pricer.price(data, option, heston, alpha, 1e-8);
      final double[] fdSense = finiteDifferenceModelGreeks((HestonCharacteristicExponent) heston, pricer, data, option);
//      System.out.println(k + "\t" + price + "\t" + fdSense[0] / price + "\t" + fdSense[1] / price + "\t" + fdSense[2] / price + "\t" + fdSense[3] / price + "\t" + fdSense[4] / price
//          + "\t" + sense[0] / price + "\t" + sense[1] / price + "\t" + sense[2] / price + "\t" + sense[3] / price + "\t" + sense[4] / price);

      for(int index =0;index<5;index++) {
        assertEquals( fdSense[index],sense[index],1e-3*price);
      }
    }
  }

  @Test(enabled=false)
  public void testHestonModelGreeksIntegrand() {

    final FourierModelGreeks modelGreek = new FourierModelGreeks();

    final double alpha = -0.75;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final double forward = 1.0;
    final double strike = 1.4;
    final double t = 1 / 12.0;

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final double k = Math.log(strike / forward);

    final List<Function1D<Double, Double>> funcs = new ArrayList<>(5);
    for (int index = 0; index < 5; index++) {
      funcs.add(modelGreek.getIntegrandFunction(heston.getAdjointFunction(t), alpha, k, index));
    }
    for (int i = 0; i < 201; i++) {
      final double x = 0.0 + 25.0 * i / 200;
      System.out.print(x);

      for (int index = 0; index < 5; index++) {
        final double value = funcs.get(index).evaluate(x);
        System.out.print("\t" + value);
      }
      System.out.print("\n");
    }

  }

  @Test(enabled=false)
  public void testHestonModelGreeksCE() {

    final double alpha = -0.75;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final double t = 1 / 12.0;

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);

    for (int i = 0; i < 201; i++) {
      final double x = 0.0 + 250.0 * i / 200;
      final ComplexNumber z = new ComplexNumber(x, -(1 + alpha));
      final ComplexNumber[] res = heston.getCharacteristicExponentAdjoint(z, t);
      System.out.print(x);
      for (final ComplexNumber re : res) {
        final double value = exp(re).getReal();
        System.out.print("\t" + value);
      }
      System.out.print("\n");
    }

  }

  public static double[] finiteDifferenceModelGreeks(final HestonCharacteristicExponent ce, final FourierPricer pricer, final BlackFunctionData data, final EuropeanVanillaOption option) {
    final double eps = 1e-5;
    final double tol = 1e-13;
    final double alpha = -0.5;
    final double[] res = new double[5];
    //kappa
    HestonCharacteristicExponent ceTemp = new HestonCharacteristicExponent(ce.getKappa() + eps, ce.getTheta(), ce.getVol0(), ce.getOmega(), ce.getRho());
    double up = pricer.price(data, option, ceTemp, alpha,tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa() - eps, ce.getTheta(), ce.getVol0(), ce.getOmega(), ce.getRho());
    double down = pricer.price(data, option, ceTemp, alpha, tol);
    res[0] = (up - down) / 2 / eps;
    //theta
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta() + eps, ce.getVol0(), ce.getOmega(), ce.getRho());
    up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta() - eps, ce.getVol0(), ce.getOmega(), ce.getRho());
    down = pricer.price(data, option, ceTemp, alpha, tol);
    res[1] = (up - down) / 2 / eps;
    //vol0
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0() + eps, ce.getOmega(), ce.getRho());
    up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0() - eps, ce.getOmega(), ce.getRho());
    down = pricer.price(data, option, ceTemp, alpha, tol);
    res[2] = (up - down) / 2 / eps;
    //omega
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0(), ce.getOmega() + eps, ce.getRho());
    up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0(), ce.getOmega() - eps, ce.getRho());
    down = pricer.price(data, option, ceTemp, alpha, tol);
    res[3] = (up - down) / 2 / eps;
    //rho
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0(), ce.getOmega(), ce.getRho() + eps);
    up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0(), ce.getOmega(), ce.getRho() - eps);
    down = pricer.price(data, option, ceTemp, alpha, tol);
    res[4] = (up - down) / 2 / eps;

    return res;
  }

}
