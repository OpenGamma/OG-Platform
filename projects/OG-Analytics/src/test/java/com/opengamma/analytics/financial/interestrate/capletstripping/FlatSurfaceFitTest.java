/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Fit a flat (caplet) volatility surface to the cap market values. Clearly, having only one parameter, this will not be
 * a good fit in terms of recovering the market values, however this does test a lot of the functionality of the caplet
 * stripper.
 */
public class FlatSurfaceFitTest extends CapletStrippingSetup {

  private final static DiscreteVolatilityFunctionProvider FLAT_SURFACE;

  static {

    FLAT_SURFACE = new DiscreteVolatilityFunctionProvider() {

      @Override
      public DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints) {
        final int size = expiryStrikePoints.length;
        final DoubleMatrix2D one = new DoubleMatrix2D(size, 1);
        for (int i = 0; i < size; i++) {
          one.getData()[i][0] = 1.0;
        }
        return new DiscreteVolatilityFunction() {

          @Override
          public DoubleMatrix2D calculateJacobian(final DoubleMatrix1D x) {
            return one;
          }

          @Override
          public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
            return new DoubleMatrix1D(size, x.getEntry(0));
          }

          @Override
          public int getLengthOfDomain() {
            return 1;
          }

          @Override
          public int getLengthOfRange() {
            return size;
          }
        };
      }

    };
  }

  @Test
  public void priceFitTest() {
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, FLAT_SURFACE);
    final CapletStrippingResult res = imp.leastSqrSolveForCapPrices(getAllCapPrices(), new DoubleMatrix1D(0.4));

    // since this is an unbiased LS fit to price it is skewed to fitting the long (10 year caps)
    assertEquals(0.3654148224492559, res.getFitParameters().getEntry(0), 1e-15);
  }

  @Test
  public void priceVegaFitTest() {
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, FLAT_SURFACE);
    final double[] capVols = getAllCapVols();
    final double[] vega = pricer.vega(capVols);
    final double[] prices = pricer.price(capVols);

    final CapletStrippingResult res = imp.solveForCapPrices(prices, vega, new DoubleMatrix1D(0.4));

    // since this is a vega weighted LS fit to price it looks more like the average cap volatility
    assertEquals(0.49945534507287803, res.getFitParameters().getEntry(0), 1e-15);
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
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, FLAT_SURFACE);

    final CapletStrippingResult res = imp.leastSqrSolveForCapVols(vols, new DoubleMatrix1D(0.4));

    // since this is an unbiased LS fit to cap volatilities, the fit is very close to the average cap volatility
    assertEquals(sum, res.getFitParameters().getEntry(0), 1e-7);
    assertEquals(0.6051342199655784, res.getFitParameters().getEntry(0), 1e-15);
  }

  /**
   * This tests the cap-vol Jacobian function
   */
  @Test
  public void functionTest() {
    final double vol = 0.4;

    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, FLAT_SURFACE);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = imp.getCapVolFunction();
    final VectorFieldFirstOrderDifferentiator diff = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFDFunc = diff.differentiate(func);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = imp.getCapVolJacobianFunction();

    final DoubleMatrix1D pos = new DoubleMatrix1D(vol);

    final DoubleMatrix1D capVols = func.evaluate(pos);
    final DoubleMatrix2D jac = jacFunc.evaluate(pos);
    final DoubleMatrix2D jacFD = jacFDFunc.evaluate(pos);
    final int n = capVols.getNumberOfElements();
    for (int i = 0; i < n; i++) {
      assertEquals(vol, capVols.getEntry(i), 1e-9);
      assertEquals(1.0, jac.getEntry(i, 0), 5e-8);
      assertEquals(1.0, jacFD.getEntry(i, 0), 1e-6);
    }

  }

}
