package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.BasisSplineVolatilityTermStructureProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

public class CapletStripperPSplineTermStructureTest extends SingleStrikeSetup {

  @Test
  public void singleStrikeTest() {
    final BasisSplineVolatilityTermStructureProvider vtsp = new BasisSplineVolatilityTermStructureProvider(0, 10.0, 10, 2);
    final int size = vtsp.getNumModelParameters();
    final DoubleMatrix1D guess = new DoubleMatrix1D(size, 0.5);
    final double lambda = 1000;

    final double[] expChi2 = new double[] {0.277676669976045, 0.0022011319414088, 0.518347393499247, 0.00007981972507487, 1.54586760016073, 0.000527042501277108, 0.0522325414771529, 0.6992013245803,
      0.000432311638224547, 0.34538288663326, 0.184403889621066, 0.0985505473418442, 0.00346706710567801, 0.00710721343644138, 0.00349404965587751, 0.00379692419890176, 0.00911582614672745,
      0.00894420412698515 };

    final int n = getNumberOfStrikes();
    for (int i = 0; i < n; i++) {
      final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());

      final CapletStripper stripper = new CapletStripperPSplineTermStructure(pricer, vtsp, lambda);

      final double[] vols = getCapVols(i);
      final int nVols = vols.length;
      final double[] errors = new double[nVols];
      Arrays.fill(errors, 1e-4); //1bps
      final CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, errors, guess);
      // System.out.println(res);
      //System.out.println(res.getChiSq());
      assertEquals(expChi2[i], res.getChiSq(), 1e-13);
    }
  }
}
