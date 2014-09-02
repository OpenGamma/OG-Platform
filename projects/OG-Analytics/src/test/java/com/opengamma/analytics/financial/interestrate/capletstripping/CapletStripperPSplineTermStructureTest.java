package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.surface.BasisSplineVolatilityTermStructureProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class CapletStripperPSplineTermStructureTest extends CapletStrippingSetup {

  /**
   * Here we expect to recover close to the market (less than 1bps error in cap vol). We perform regression tests
   * against the expected chi-squares and do not explicitly check the fit parameters.
   */
  @Test
  public void singleStrikeTest() {
    final double oneBP = 1e-4;
    BasisSplineVolatilityTermStructureProvider vtsp = new BasisSplineVolatilityTermStructureProvider(0, 10.0, 10, 2);
    int size = vtsp.getNumModelParameters();
    DoubleMatrix1D guess = new DoubleMatrix1D(size, 0.5);
    double lambda = 1000;

    double[] expChi2 = new double[] {0.277676669976045, 0.0022011319414088, 0.518347393499247, 0.00007981972507487,
      1.54586760016073, 0.000527042501277108, 0.0522325414771529, 0.6992013245803, 0.000432311638224547,
      0.34538288663326, 0.184403889621066, 0.0985505473418442, 0.00346706710567801, 0.00710721343644138,
      0.00349404965587751, 0.00379692419890176, 0.00911582614672745, 0.00894420412698515 };

    int n = getNumberOfStrikes();
    for (int i = 0; i < n; i++) {
      MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());

      CapletStripper stripper = new CapletStripperPSplineTermStructure(pricer, vtsp, lambda);

      double[] vols = getCapVols(i);
      double[] prices = pricer.price(vols);
      double[] vega = pricer.vega(vols);
      int nVols = vols.length;
      double[] errors = new double[nVols];
      Arrays.fill(errors, oneBP); // 1bps
      // scale vega
      for (int j = 0; j < nVols; j++) {
        vega[j] *= oneBP;
      }

      CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, errors, guess);
      assertEquals(expChi2[i], res.getChiSqr(), 1e-13);

      // use default guess - won't get exactly the same solution due to stopping criteria and implied vol tolerance
      res = stripper.solve(vols, MarketDataType.VOL, errors);
      assertEquals(expChi2[i], res.getChiSqr(), 1e-5);

      // solve for price (weighted by vega), starting from vol solution - this should give a very similar chiSq
      res = stripper.solve(prices, MarketDataType.PRICE, vega, res.getFitParameters());
      assertEquals(expChi2[i], res.getChiSqr(), 1e-4);

      // this is effectively setting errors to 1 - must also scale lambda.
      // Since we have scaled the problem, but not the stopping criteria, there is a poor match with the (scaled chi2)
      stripper = new CapletStripperPSplineTermStructure(pricer, vtsp, oneBP * oneBP * lambda);
      res = stripper.solve(vols, MarketDataType.VOL, guess);
      assertEquals(expChi2[i], res.getChiSqr() / oneBP / oneBP, 1e-4);

      // use default guess and errors = 1.0
      res = stripper.solve(vols, MarketDataType.VOL);
      assertEquals(expChi2[i], res.getChiSqr() / oneBP / oneBP, 1e-4);
    }
  }
}
