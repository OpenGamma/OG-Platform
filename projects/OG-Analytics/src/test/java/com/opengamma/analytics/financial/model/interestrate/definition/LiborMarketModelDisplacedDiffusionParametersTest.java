package com.opengamma.analytics.financial.model.interestrate.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LiborMarketModelDisplacedDiffusionParametersTest {

  // LMM
  private static final int NB_PERIOD = 20; // %Y quarterly
  private static final double[] TIME_IBOR = new double[NB_PERIOD + 1];
  private static final double[] DELTA = new double[NB_PERIOD];
  private static final double[][] VOL = new double[NB_PERIOD][2];
  private static final double[] DISPLACEMENT = new double[NB_PERIOD];
  static {
    for (int loopperiod = 0; loopperiod <= NB_PERIOD; loopperiod++) {
      TIME_IBOR[loopperiod] = 2.0 / 365.0 + loopperiod * 0.25;
    }
    for (int loopperiod = 0; loopperiod < NB_PERIOD; loopperiod++) {
      DELTA[loopperiod] = 0.25;
      DISPLACEMENT[loopperiod] = 0.10;
      VOL[loopperiod][0] = 0.02;
      VOL[loopperiod][1] = -0.02 + loopperiod * 0.002;
    }
  }
  private static final double MEAN_REVERSION = 0.01;
  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETER_LMM = new LiborMarketModelDisplacedDiffusionParameters(TIME_IBOR, DELTA, DISPLACEMENT, VOL, MEAN_REVERSION);

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals("LMM getter", MEAN_REVERSION, PARAMETER_LMM.getMeanReversion());
    ArrayAsserts.assertArrayEquals("LMM getter", DISPLACEMENT, PARAMETER_LMM.getDisplacement(), 1.0E-10);
    ArrayAsserts.assertArrayEquals("LMM getter", DELTA, PARAMETER_LMM.getAccrualFactor(), 1.0E-10);
    ArrayAsserts.assertArrayEquals("LMM getter", TIME_IBOR, PARAMETER_LMM.getIborTime(), 1.0E-10);
    for (int loopperiod = 0; loopperiod < NB_PERIOD; loopperiod++) {
      ArrayAsserts.assertArrayEquals("LMM getter", VOL[loopperiod], PARAMETER_LMM.getVolatility()[loopperiod], 1.0E-10);
    }
  }

  @Test
  /**
   * Tests the class setters.
   */
  public void setter() {
    double[][] volReplaced = new double[][] { {0.01, 0.01}, {0.011, 0.009}, {0.012, 0.008}};
    int indexStart = 10;
    double[][] vol2 = new double[NB_PERIOD][2];
    for (int loopperiod = 0; loopperiod < NB_PERIOD; loopperiod++) {
      vol2[loopperiod][0] = 0.02;
      vol2[loopperiod][1] = -0.02 + loopperiod * 0.002;
    }
    LiborMarketModelDisplacedDiffusionParameters parameterLmm = new LiborMarketModelDisplacedDiffusionParameters(TIME_IBOR, DELTA, DISPLACEMENT, vol2, MEAN_REVERSION);
    parameterLmm.setVolatility(volReplaced, indexStart);

    for (int loopperiod = 0; loopperiod < NB_PERIOD; loopperiod++) {
      if (loopperiod >= indexStart && loopperiod < indexStart + volReplaced.length) { // Replaced vol
        ArrayAsserts.assertArrayEquals("LMM getter", volReplaced[loopperiod - indexStart], parameterLmm.getVolatility()[loopperiod], 1.0E-10);
      } else {// Original vol
        ArrayAsserts.assertArrayEquals("LMM getter", VOL[loopperiod], parameterLmm.getVolatility()[loopperiod], 1.0E-10);
      }
    }
  }

}
