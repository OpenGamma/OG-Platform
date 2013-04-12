/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;

/**
 * 
 */
public class CapletStrippingBootstrapTest extends CapletStrippingSetup {

  private static final double[] CAP_EXPIRIES = new double[] {1, 2, 3, 4, 5, 7, 10};
  // private static final double[] VOL_1PC = new double[] {0.9943, 0.7309, 0.7523, 0.7056, 0.661, 0.5933, 0.5313};
  private static final double[] VOL_1PC = new double[] {0.7145, 0.7561, 0.724, 0.73, 0.693, 0.6103, 0.5626};
  private static final double STRIKE = 0.01;

  private static List<CapFloor> CAPS_1PC;

  static {
    final int n = CAP_EXPIRIES.length;
    CAPS_1PC = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      CapFloor cap = SimpleCapFloorMaker.makeCap(CUR, INDEX, 1, (int) (FREQUENCY * CAP_EXPIRIES[i]), "funding", "3m Libor", STRIKE, true);
      CAPS_1PC.add(cap);
    }
  }

  @Test
  public void test() {

    final boolean print = false;
    if (print) {
      System.out.println("CapletStrippingBootstrapTest");
    }

    final CapletStrippingBootstrap bootstrap = new CapletStrippingBootstrap(CAPS_1PC, YIELD_CURVES);
    final double[] capletVols = bootstrap.capletVolsFromCapVols(VOL_1PC);

    if (print) {
      final int n = capletVols.length;
      System.out.println("caplet vols");
      for (int i = 0; i < n; i++) {
        System.out.println(capletVols[i]);
      }
      System.out.println();
    }

    final int n = CAP_EXPIRIES.length;

    VolatilityModel1D piecewise = new VolatilityModel1D() {

      @Override
      public Double getVolatility(double[] fwdKT) {
        return getVolatility(fwdKT[0], fwdKT[1], fwdKT[2]);
      }

      @Override
      public double getVolatility(SimpleOptionData option) {
        return getVolatility(0, 0, option.getTimeToExpiry());
      }

      @Override
      public double getVolatility(double forward, double strike, double timeToExpiry) {
        int index = Arrays.binarySearch(CAP_EXPIRIES, timeToExpiry);
        if (index >= 0) {
          if (index >= (n - 1)) {
            return capletVols[n - 1];
          }
          return capletVols[index + 1];
        } else if (index == -(n + 1)) {
          return capletVols[n - 1];
        } else {
          return capletVols[-index - 1];
        }
      }
    };

    // print the curve
    if (print) {
      System.out.println("caplet vol curve");
      for (int i = 0; i < 101; i++) {
        double t = i * 10. / 100;
        double sig = piecewise.getVolatility(0, 0, t);
        System.out.println(t + "\t" + sig);
      }
      System.out.println();
    }

    Iterator<CapFloor> iter = CAPS_1PC.iterator();
    int ii = 0;
    while (iter.hasNext()) {
      CapFloor cap = iter.next();
      CapFloorPricer pricer = new CapFloorPricer(cap, YIELD_CURVES);
      double vol = pricer.impliedVol(piecewise);
      if (print) {
        System.out.println(vol + "\t" + VOL_1PC[ii]);
      }
      assertEquals(vol, VOL_1PC[ii++], 1e-9);
    }

  }
}
