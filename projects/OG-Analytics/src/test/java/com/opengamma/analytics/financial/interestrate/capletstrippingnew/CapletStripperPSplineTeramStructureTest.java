package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.BasisSplineVolatilityTermStructureProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

public class CapletStripperPSplineTeramStructureTest extends SingleStrikeSetup {

  
  @Test
  public void singleStrikeTest() {
    BasisSplineVolatilityTermStructureProvider vtsp = new BasisSplineVolatilityTermStructureProvider(0, 10.0, 10, 2);
   int  size = vtsp.getNumModelParameters();
   DoubleMatrix1D guess = new DoubleMatrix1D(size, 0.5);

    final int n = getNumberOfStrikes();
    for (int i = 0; i < n; i++) {
      final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());
     
      final CapletStripper stripper = new CapletStripperPSplineTermStructure(pricer,vtsp, 1000.0);
     
      
      double[] vols = getCapVols(i);
      final int nVols = vols.length;
      final double[] errors = new double[nVols];
      Arrays.fill(errors, 1e-4); //1bps
      CapletStrippingResult res = stripper.solve(vols,MarketDataType.VOL,errors,guess);
      System.out.println(res);
      // testSingleStrikeStripping(stripper, i, 0.0, null, 1e-15, true);
    }
  }
}
