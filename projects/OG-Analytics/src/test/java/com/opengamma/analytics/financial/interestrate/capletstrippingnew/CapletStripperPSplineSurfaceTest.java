package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.BasisSplineVolatilitySurfaceProvider;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;

public class CapletStripperPSplineSurfaceTest extends SingleStrikeSetup {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  @Test
  public void test() {

    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());

    final double lambda = 1;
    final CapletStripper stripper = new CapletStripperPSplineSurface(pricer, 0.0, 10.0, 10, 2, 0.0, 0.12, 10, 2, lambda);

    final int size = 11 * 11;
    final DoubleMatrix1D guess = new DoubleMatrix1D(size, 0.5);

    final double[] vols = getAllCapVols();
    final int nVols = vols.length;
    final double[] errors = new double[nVols];
    Arrays.fill(errors, 1e-4); //1bps
    final CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, errors, guess);
    System.out.println(res);
    //System.out.println(res.getChiSq());
    // assertEquals(expChi2[i], res.getChiSq(), 1e-13);
    res.printSurface(System.out, 101, 101);
  }

  @Test
  public void debugTest() {
    final int tSize = 10;
    final int kSize = 10;
    final int tDegree = 2;
    final int kDegree = 2;
    final double tLambda = 1;
    final double kLambda = 100;

    final VolatilitySurfaceProvider vsp = new BasisSplineVolatilitySurfaceProvider(0.0, 0.12, kSize, kDegree, 0.0, 10.0, tSize, tDegree);
    final int n = vsp.getNumModelParameters();
    final DoubleMatrix1D x = new DoubleMatrix1D(n);
    Arrays.fill(x.getData(), 0, 11, 1.0);
    Arrays.fill(x.getData(), 11, 22, 0.5);
    //  x.getData()[80] = 1.0;
    final VolatilitySurface vs = vsp.getVolSurface(x);
    final int nStrikePoints = 101;
    final int nExpPoints = 101;
    final double[] strikes = new double[nStrikePoints];
    final double[] times = new double[nExpPoints];

    final double arse = vs.getVolatility(7.8, 0.05);

    for (int i = 0; i < nStrikePoints; i++) {
      strikes[i] = 0.0 + 0.12 * i / (nStrikePoints - 1.0);
    }
    //    System.out.println();
    //    for (int j = 0; j < nExpPoints; j++) {
    //      times[j] = 0 + 10.0 * j / (nExpPoints - 1.0);
    //      System.out.print("\t" + times[j]);
    //    }
    //
    //    for (int i = 0; i < nStrikePoints; i++) {
    //      System.out.print("\n" + strikes[i]);
    //      for (int j = 0; j < nExpPoints; j++) {
    //        final Double vol = vs.getVolatility(times[j], strikes[i]);
    //        System.out.print("\t" + vol);
    //      }
    //    }
    //    System.out.println();

    final DoubleMatrix2D penalty = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {tSize + tDegree - 1, kSize + tDegree - 1 }, new int[] {2, 2 }, new double[] {tLambda, kLambda });
    final double p = MA.getInnerProduct(x, MA.multiply(penalty, x));
    System.out.println(penalty);
  }
}
