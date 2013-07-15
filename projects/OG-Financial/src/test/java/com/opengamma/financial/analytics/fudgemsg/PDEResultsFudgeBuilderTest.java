/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.analytics.financial.greeks.PDEResultCollection;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.util.test.TestGroup;


/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class PDEResultsFudgeBuilderTest extends AnalyticsTestBase {

  private static final PDEGrid1D GRID = new PDEGrid1D(3, 4, 400, 0, 10);
  private static final double[][] DATA = new double[][] {new double[] {1, 2, 3, 4}, new double[] {5, 6, 7, 8}, new double[] {9, 10, 11, 12}};
  private static final PDEFullResults1D RESULTS = new PDEFullResults1D(GRID, DATA);
  private static final double[] STRIKES = new double[] {1, 2, 3, 4};
  private static final double[] BD = new double[] {0.5, 0.6, 0.7, 0.8};
  private static final double[] BDD = new double[] {0.51, 0.61, 0.71, 0.81};
  private static final double[] BG = new double[] {0.52, 0.62, 0.72, 0.82};
  private static final double[] BDG = new double[] {0.53, 0.63, 0.73, 0.83};
  private static final double[] BVE = new double[] {0.54, 0.64, 0.74, 0.84};
  private static final double[] BVA = new double[] {0.55, 0.65, 0.75, 0.85};
  private static final double[] BVO = new double[] {0.56, 0.66, 0.76, 0.86};
  private static final double[] D = new double[] {0.15, 0.16, 0.17, 0.8};
  private static final double[] DD = new double[] {0.151, 0.161, 0.171, 0.181};
  private static final double[] G = new double[] {0.152, 0.162, 0.172, 0.182};
  private static final double[] DG = new double[] {0.153, 0.163, 0.173, 0.183};
  private static final double[] VE = new double[] {0.154, 0.164, 0.174, 0.184};
  private static final double[] VA = new double[] {0.155, 0.165, 0.175, 0.185};
  private static final double[] VO = new double[] {0.156, 0.166, 0.176, 0.186};
  private static final PDEResultCollection PDE_GREEKS = new PDEResultCollection(STRIKES);
  private static final double[] EXPIRIES = new double[] {1, 5, 10};
  private static final double[][] STRIKE_GRID = new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}};
  private static final double[][] BUCKETED_VEGA = new double[][] {new double[] {11, 12, 13}, new double[] {14, 15, 16}, new double[] {17, 18, 19}};
  private static final BucketedGreekResultCollection BUCKETED_GREEKS = new BucketedGreekResultCollection(EXPIRIES, STRIKE_GRID);

  static {
    PDE_GREEKS.put(PDEResultCollection.GRID_BLACK_DELTA, BD);
    PDE_GREEKS.put(PDEResultCollection.GRID_BLACK_DUAL_DELTA, BDD);
    PDE_GREEKS.put(PDEResultCollection.GRID_BLACK_GAMMA, BG);
    PDE_GREEKS.put(PDEResultCollection.GRID_BLACK_DUAL_GAMMA, BDG);
    PDE_GREEKS.put(PDEResultCollection.GRID_BLACK_VEGA, BVE);
    PDE_GREEKS.put(PDEResultCollection.GRID_BLACK_VANNA, BVA);
    PDE_GREEKS.put(PDEResultCollection.GRID_BLACK_VOMMA, BVO);
    PDE_GREEKS.put(PDEResultCollection.GRID_DELTA, D);
    PDE_GREEKS.put(PDEResultCollection.GRID_DUAL_DELTA, DD);
    PDE_GREEKS.put(PDEResultCollection.GRID_GAMMA, G);
    PDE_GREEKS.put(PDEResultCollection.GRID_DUAL_GAMMA, DG);
    PDE_GREEKS.put(PDEResultCollection.GRID_VEGA, VE);
    PDE_GREEKS.put(PDEResultCollection.GRID_VANNA, VA);
    PDE_GREEKS.put(PDEResultCollection.GRID_VOMMA, VO);
    BUCKETED_GREEKS.put(BucketedGreekResultCollection.BUCKETED_VEGA, BUCKETED_VEGA);
  }

  @Test
  public void testGrid() {
    final PDEGrid1D grid = cycleObject(PDEGrid1D.class, GRID);
    assertEquals(grid.getNumSpaceNodes(), GRID.getNumSpaceNodes());
    assertEquals(grid.getNumTimeNodes(), GRID.getNumTimeNodes());
    assertArrayEquals(grid.getTimeNodes(), GRID.getTimeNodes(), 1e-9);
    assertArrayEquals(grid.getSpaceNodes(), GRID.getSpaceNodes(), 1e-9);
  }

  @Test
  public void testResults() {
    final PDEFullResults1D results = cycleObject(PDEFullResults1D.class, RESULTS);
    assertEquals(results.getGrid().getNumSpaceNodes(), GRID.getNumSpaceNodes());
    assertEquals(results.getGrid().getNumTimeNodes(), GRID.getNumTimeNodes());
    assertArrayEquals(results.getGrid().getTimeNodes(), GRID.getTimeNodes(), 1e-9);
    assertArrayEquals(results.getGrid().getSpaceNodes(), GRID.getSpaceNodes(), 1e-9);
    for (int i = 0; i < DATA.length; i++) {
      assertArrayEquals(results.getF()[i], DATA[i], 1e-9);
    }
  }

  @Test
  public void testPDEGreeks() {
    final PDEResultCollection pdeGreeks = cycleObject(PDEResultCollection.class, PDE_GREEKS);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_BLACK_DELTA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_BLACK_DELTA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_BLACK_DUAL_DELTA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_BLACK_DUAL_DELTA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_BLACK_GAMMA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_BLACK_GAMMA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_BLACK_DUAL_GAMMA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_BLACK_DUAL_GAMMA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_BLACK_VEGA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_BLACK_VEGA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_BLACK_VANNA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_BLACK_VANNA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_BLACK_VOMMA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_BLACK_VOMMA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_DELTA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_DELTA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_DUAL_DELTA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_DUAL_DELTA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_GAMMA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_GAMMA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_DUAL_GAMMA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_DUAL_GAMMA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_VEGA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_VEGA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_VANNA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_VANNA), 1e-9);
    assertArrayEquals(PDE_GREEKS.getGridGreeks(PDEResultCollection.GRID_VOMMA), pdeGreeks.getGridGreeks(PDEResultCollection.GRID_VOMMA), 1e-9);
  }

  @Test
  public void testBucketedGreeks() {
    final BucketedGreekResultCollection bucketedGreeks = cycleObject(BucketedGreekResultCollection.class, BUCKETED_GREEKS);
    assertArrayEquals(BUCKETED_GREEKS.getExpiries(), bucketedGreeks.getExpiries(), 1e-9);
    for (int i = 0; i < BUCKETED_VEGA.length; i++) {
      assertArrayEquals(bucketedGreeks.getStrikes()[i], bucketedGreeks.getStrikes()[i], 1e-9);
      assertArrayEquals(BUCKETED_VEGA[i], bucketedGreeks.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA)[i], 1e-9);
    }
  }
}
