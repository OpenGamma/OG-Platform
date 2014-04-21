/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HestonFFTPricerTest {
  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final FFTPricer PRICER = new FFTPricer();

  @Test
  public void testLowVolOfVol() {
    final double sigma = 0.36;

    final double kappa = 1.0; // mean reversion speed
    final double theta = sigma * sigma; // reversion level
    final double vol0 = theta; // start level
    final double omega = 0.001; // vol-of-vol
    final double rho = -0.3; // correlation

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);

    final int n = 21;
    final double deltaMoneyness = 0.1;
    final double alpha = -0.5;
    final double tol = 1e-9;

    final double[][] strikeNprice = PRICER.price(FORWARD, DF, T, true, heston, n, deltaMoneyness, sigma, alpha, tol);

    for (int i = 0; i < n; i++) {
      final double k = strikeNprice[i][0];
      final double price = strikeNprice[i][1];

      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(FORWARD, DF, 0.0), new EuropeanVanillaOption(k, T, true), price);
      //System.out.println(k + "\t" + impVol);
      assertEquals(sigma, impVol, 1e-3);
    }
  }
  
  
  @Test
  public void testHestonModelGreeks() {

    FFTModelGreeks modelGreekFFT = new FFTModelGreeks();
    FourierModelGreeks modelGreekFourier = new FourierModelGreeks();

    final double alpha = -0.5;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final double forward = 1.0;
    final double t = 1 / 12.0;

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final BlackFunctionData data = new BlackFunctionData(forward, 1, 0.2);

    boolean isCall = true;
    
   //this contains strike, price then the derivatives of price wrt the parameters 
    double[][] res = modelGreekFFT.getGreeks(forward, 1.0, t, isCall,heston,0.3,2.0,50,0.2,alpha,1e-16);
    
    DoubleQuadraticInterpolator1D interpolator = new DoubleQuadraticInterpolator1D();
    int size = res.length-2;
    Interpolator1DDoubleQuadraticDataBundle[] db = new Interpolator1DDoubleQuadraticDataBundle[size];
    for(int i=0;i<size;i++) {
      db[i] = interpolator.getDataBundle(res[0], res[i+2]);
    }
    

    for (int i = 0; i < 11; i++) {
      final double k = 0.7 + 0.6 * i / 10.0;
      isCall = k >= forward;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, isCall);
      double[] senseFourier = modelGreekFourier.getGreeks(data, option, heston, alpha, 1e-12);

  //    System.out.print(k);
      for(int j=0;j<size;j++) {
        double senseFFT = interpolator.interpolate(db[j], k);
    //    System.out.print("\t"+senseFourier[j]+"\t"+senseFFT);
        assertEquals(senseFourier[j],senseFFT,1e-5);
      }
    //  System.out.print("\n");
          
    }
    
  }
}
