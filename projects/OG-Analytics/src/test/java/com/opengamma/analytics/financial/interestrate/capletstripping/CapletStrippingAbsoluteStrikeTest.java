/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public abstract class CapletStrippingAbsoluteStrikeTest extends CapletStrippingSetup {

  public abstract CapletStrippingAbsoluteStrike getStripper(final List<CapFloor> caps);

  public void testVolStripping(final boolean print) {
    testVolStripping(1e-7, print); // Tolerance of 0.001bps - only really applicable of root finding methods
  }

  public void testVolStripping(final double volTol, final boolean print) {

    if (print) {
      System.out.println("CapletStrippingAbsoluteStrikeTest " + this.getClass());
    }

    final int n = getNumberOfStrikes();

    final int samples = 101;
    final double[][] mVols = new double[n][samples];

    for (int i = 0; i < n; i++) {
      final List<CapFloor> caps = getCaps(i);
      final double k = getStrikes()[i];
      final CapletStrippingAbsoluteStrike stripper = getStripper(caps);

      final double[] vols = getCapVols(i);
      final CapletStrippingSingleStrikeResult res = stripper.solveForVol(vols);

      if (print) {
        System.out.println(i + " chiSqr: " + res.getChiSq() + " fit parameters: " + res.getFitParameters());
      }

      final double[] fitVols = res.getModelValues().getData();
      final VolatilitySurface volCurve = res.getVolatilitySurface();

      final int m = vols.length;
      assertEquals(fitVols.length, m);
      for (int j = 0; j < m; j++) {
        assertEquals(vols[j], fitVols[j], volTol);
      }

      if (print) {
        for (int j = 0; j < samples; j++) {
          final double t = j * 10.0 / (samples - 1);
          mVols[i][j] = volCurve.getVolatility(t, k);
        }
      }
    }

    if (print) {
      System.out.print("\n");
      for (int j = 0; j < samples; j++) {
        final double t = j * 10.0 / (samples - 1);
        System.out.print(t);
        for (int i = 0; i < n; i++) {
          System.out.print("\t" + mVols[i][j]);
        }
        System.out.print("\n");
      }
    }
  }

  protected void timingTest(final int warmup, final int beanchmarkCycles) {
    final int n = getNumberOfStrikes();

    final CapletStrippingAbsoluteStrike[] strippers = new CapletStrippingAbsoluteStrike[n];
    // setup the strippers
    for (int i = 0; i < n; i++) {
      final List<CapFloor> caps = getCaps(i);
      strippers[i] = getStripper(caps);
    }

    for (int runs = 0; runs < warmup; runs++) {
      for (int i = 0; i < n; i++) {
        final CapletStrippingSingleStrikeResult res = strippers[i].solveForVol(getCapVols(i));
        // check fit
        assertTrue(res.getChiSq() < 1.0);
      }
    }
    if (beanchmarkCycles > 0) {
      final long start = System.nanoTime();

      for (int runs = 0; runs < beanchmarkCycles; runs++) {
        for (int i = 0; i < n; i++) {
          final CapletStrippingSingleStrikeResult res = strippers[i].solveForVol(getCapVols(i));
          // check fit
          assertTrue(res.getChiSq() < 1.0);
        }
      }
      final double time = (System.nanoTime() - start) / (beanchmarkCycles * 1e6);
      System.out.println("Time per fit set for " + this.getClass() + " is: " + time + "ms");
    }

  }

}
