/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.newstrippers;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloorPricer;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingSetup;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * Fit a flat (caplet) volatility surface to the cap prices 
 */
public class FlatSurfaceFitTest extends CapletStrippingSetup {

  private final static VolatilitySurfaceProvider FLAT_SURFACE;
  private final static Function2D<Double, DoubleMatrix1D> FUNC;

  static {
    FUNC = new Function2D<Double, DoubleMatrix1D>() {
      DoubleMatrix1D one = new DoubleMatrix1D(1.0);

      @Override
      public DoubleMatrix1D evaluate(final Double x1, final Double x2) {
        return one;
      }
    };

    FLAT_SURFACE = new VolatilitySurfaceProvider() {

      @Override
      public Surface<Double, Double, DoubleMatrix1D> getVolSurfaceAdjoint(final DoubleMatrix1D modelParameters) {
        return new FunctionalSurface<>(FUNC);
      }

      @Override
      public VolatilitySurface getVolSurface(final DoubleMatrix1D modelParameters) {
        ArgumentChecker.isTrue(modelParameters.getNumberOfElements() == 1, "this has only one parameter");
        return new VolatilitySurface(ConstantDoublesSurface.from(modelParameters.getEntry(0)));
      }

      @Override
      public int getNumModelParameters() {
        return 1;
      }
    };
  }

  @Test
  public void priceFitTest() {
    final CapletStripper stripper = new CapletStripper(getAllCaps(), getYieldCurves(), FLAT_SURFACE);
    final LeastSquareResults res = stripper.leastSqrSolveForCapPrices(getAllCapPrices(), new DoubleMatrix1D(0.4));

    //since this is an unbiased LS fit to price it is skewed to fitting the long (10 year caps) 
    assertEquals(0.3654148224492559, res.getFitParameters().getEntry(0), 1e-15);
    //System.out.println(res.toString());
  }

  @Test
  public void priceVegaFitTest() {
    final CapletStripper stripper = new CapletStripper(getAllCaps(), getYieldCurves(), FLAT_SURFACE);
    final MultiCapFloorPricer2 pricer = new MultiCapFloorPricer2(getAllCaps(), getYieldCurves());
    final double[] capVols = getAllCapVols();
    final double[] vega = pricer.vega(capVols);
    final double[] prices = pricer.price(capVols);

    final LeastSquareResults res = stripper.leastSqrSolveForCapPrices(prices, vega, new DoubleMatrix1D(0.4));

    //since this is a vega weighted LS fit to price it looks more like the average cap volatility 
    assertEquals(0.49945534507287803, res.getFitParameters().getEntry(0), 1e-15);
    //System.out.println(res.toString());
  }

  @Test
  public void volFitTest() {
    final double[] vols = getAllCapVols();
    final int n = vols.length;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += vols[i];
    }
    sum /= n;
    //  System.out.println("average cap vol: " + sum);
    final CapletStripper stripper = new CapletStripper(getAllCaps(), getYieldCurves(), FLAT_SURFACE);
    final LeastSquareResults res = stripper.leastSqrSolveForCapVols(vols, new DoubleMatrix1D(0.4));

    //since this is an unbiased LS fit to cap volatilities, the fit is very close to the average cap volatility
    assertEquals(sum, res.getFitParameters().getEntry(0), 1e-7);
    assertEquals(0.6051342199655784, res.getFitParameters().getEntry(0), 1e-15);
    //  System.out.println(res.toString());
  }

  @Test
  public void functionTest() {
    final CapletStripper stripper = new CapletStripper(getAllCaps(), getYieldCurves(), FLAT_SURFACE);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = stripper.getCapVolFunction();
    final VectorFieldFirstOrderDifferentiator diff = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFD = diff.differentiate(func);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = stripper.getCapVolJacobianFunction();

    final DoubleMatrix1D pos = new DoubleMatrix1D(0.4);
    //if all the caplets have a volatility of 40%, then by definition the all the cap volatilities must be 40%
    final DoubleMatrix1D capVols = func.evaluate(pos);
    final int n = capVols.getNumberOfElements();
    final List<CapFloor> allCaps = getAllCaps();
    for (int i = 0; i < n; i++) {
      if (Math.abs(capVols.getEntry(i) - 0.4) > 1e-3) {
        final CapFloor cap = allCaps.get(i);
        final CapFloorPricer pricer = new CapFloorPricer(cap, getYieldCurves());
        final double capPrice = pricer.price(0.4);
        final double iv = pricer.impliedVol(capPrice);
        final double capPrice2 = pricer.price(iv);
        System.out.println(cap.getEndTime() + "\t" + cap.getStrike() + "\t" + capPrice + "\t" + iv + "\t" + capPrice2);
      }
    }

    System.out.println(func.evaluate(pos) + "\n");
    System.out.println(jacFD.evaluate(pos) + "\n");
    System.out.println(jac.evaluate(pos) + "\n");
  }

}
