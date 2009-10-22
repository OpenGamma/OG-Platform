/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class BivariateNormalDistributionTest {
  private static final ProbabilityDistribution<Double[]> DIST = new BivariateNormalDistribution();
  private static final double EPS = 1e-8;

  @Test
  public void testInputs() {
    try {
      DIST.getCDF(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DIST.getCDF(new Double[] { 2., 1. });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DIST.getCDF(new Double[] { 2., 1., 0.3, 4. });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DIST.getCDF(new Double[] { null, 1., 0.3 });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DIST.getCDF(new Double[] { 1., null, 0.4 });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DIST.getCDF(new Double[] { 1., 1., null });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DIST.getCDF(new Double[] { 1., 1., 3. });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DIST.getCDF(new Double[] { 1., 1., -3. });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    assertEquals(DIST.getCDF(new Double[] { 0.0, 0.0, 0.0 }), 0.25, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.0, 0.0, -0.5 }), 1. / 6, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.0, 0.0, 0.5 }), 1. / 3, EPS);

    assertEquals(DIST.getCDF(new Double[] { 0.0, -0.5, 0.0 }), 0.1542687694, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.0, -0.5, -0.5 }), 0.0816597607, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.0, -0.5, 0.5 }), 0.2268777781, EPS);

    assertEquals(DIST.getCDF(new Double[] { 0.0, 0.5, 0.0 }), 0.3457312306, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.0, 0.5, -0.5 }), 0.2731222219, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.0, 0.5, 0.5 }), 0.4183402393, EPS);

    assertEquals(DIST.getCDF(new Double[] { -0.5, 0.0, 0.0 }), 0.1542687694, EPS);
    assertEquals(DIST.getCDF(new Double[] { -0.5, 0.0, -0.5 }), 0.0816597607, EPS);
    assertEquals(DIST.getCDF(new Double[] { -0.5, 0.0, 0.5 }), 0.2268777781, EPS);

    assertEquals(DIST.getCDF(new Double[] { -0.5, -0.5, 0.0 }), 0.0951954128, EPS);
    assertEquals(DIST.getCDF(new Double[] { -0.5, -0.5, -0.5 }), 0.0362981865, EPS);
    assertEquals(DIST.getCDF(new Double[] { -0.5, -0.5, 0.5 }), 0.1633195213, EPS);

    assertEquals(DIST.getCDF(new Double[] { -0.5, 0.5, 0.0 }), 0.2133421259, EPS);
    assertEquals(DIST.getCDF(new Double[] { -0.5, 0.5, -0.5 }), 0.1452180174, EPS);
    assertEquals(DIST.getCDF(new Double[] { -0.5, 0.5, 0.5 }), 0.2722393522, EPS);

    assertEquals(DIST.getCDF(new Double[] { 0.5, 0.0, 0.0 }), 0.3457312306, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.5, 0.0, -0.5 }), 0.2731222219, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.5, 0.0, 0.5 }), 0.4183402393, EPS);

    assertEquals(DIST.getCDF(new Double[] { 0.5, -0.5, 0.0 }), 0.2133421259, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.5, -0.5, -0.5 }), 0.1452180174, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.5, -0.5, 0.5 }), 0.2722393522, EPS);

    assertEquals(DIST.getCDF(new Double[] { 0.5, 0.5, 0.0 }), 0.4781203354, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.5, 0.5, -0.5 }), 0.4192231090, EPS);
    assertEquals(DIST.getCDF(new Double[] { 0.0, -1.0, -1.0 }), 0.00000000, EPS);
  }
}
