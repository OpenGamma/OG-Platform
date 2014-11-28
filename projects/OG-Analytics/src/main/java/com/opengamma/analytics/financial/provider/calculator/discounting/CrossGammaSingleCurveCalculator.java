/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Computes the cross-gamma to the curve parameters for a single curve.
 * The curve should be represented by a YieldCurve with an InterpolatedDoublesCurve on the zero-coupon rates.
 * By default the gamma is computed using a one basis-point shift. This default can be change in a constructor.
 * The results themselves are not scaled (the represent the second order derivative).
 * <p> Reference: Interest rate cross-gamma for single curve. OpenGamma quantitative research 15, July 14
 */
public class CrossGammaSingleCurveCalculator {

  /** Default size of bump: 1 basis point. */
  private static final double BP1 = 1.0E-4;

  /** The sensitivity calculator to the curve parameters used for the delta computation */
  private final ParameterSensitivityParameterCalculator<ParameterProviderInterface> _psc;
  /** The shift used for finite difference Gamma using two deltas. */
  private final double _shift;

  /**
   * Constructor.
   * @param shift The shift used for finite difference Gamma using two deltas.
   * @param curveSensitivityCalculator The delta (curve sensitivity) calculator.
   */
  public CrossGammaSingleCurveCalculator(final double shift,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    _psc = new ParameterSensitivityParameterCalculator<>(curveSensitivityCalculator);
    _shift = shift;
  }

  /**
   * Constructor.
   * @param curveSensitivityCalculator The delta (curve sensitivity) calculator.
   */
  public CrossGammaSingleCurveCalculator(final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    _psc = new ParameterSensitivityParameterCalculator<>(curveSensitivityCalculator);
    _shift = BP1;
  }

  /**
   * Computes the gamma matrix for a given instrument. The curve provider should contain only one curve which should be of the 
   * type YieldCurve with an underlying InterpolatedDoublesCurve.
   * @param instrument The instrument for which the cross-gamma should be computed.
   * @param multicurve The multi-curve provider.
   * @return The cross-gamma matrix.
   */
  public DoubleMatrix2D calculateCrossGamma(final InstrumentDerivative instrument, final MulticurveProviderDiscount multicurve) {
    ArgumentChecker.isTrue(multicurve.getAllNames().size() == 1, "provider should have only one curve for GammaSingleCurve computation");
    String name = multicurve.getAllNames().iterator().next();
    Currency ccy = multicurve.getCurrencyForName(name);
    YieldAndDiscountCurve curve = multicurve.getCurve(name);
    ArgumentChecker.isTrue(curve instanceof YieldCurve, "curve should be YieldCurve");
    YieldCurve yieldCurve = (YieldCurve) curve;
    ArgumentChecker.isTrue(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
    InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) yieldCurve.getCurve();
    double[] y = interpolatedCurve.getYDataAsPrimitive();
    double[] x = interpolatedCurve.getXDataAsPrimitive();
    int nbNode = y.length;
    MultipleCurrencyParameterSensitivity ps0 = _psc.calculateSensitivity(instrument, multicurve);
    DoubleMatrix1D ps0Mat = ps0.getSensitivity(name, ccy);
    double[] ps0Array = ps0Mat.getData();
    MultipleCurrencyParameterSensitivity[] psShift = new MultipleCurrencyParameterSensitivity[nbNode];
    double[][] gammaArray = new double[nbNode][nbNode];
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final double[] yieldBumped = y.clone();
      yieldBumped[loopnode] += _shift;
      final YieldAndDiscountCurve curveBumped = new YieldCurve(name,
          new InterpolatedDoublesCurve(x, yieldBumped, interpolatedCurve.getInterpolator(), true));
      MulticurveProviderDiscount multicurveBumped = new MulticurveProviderDiscount();
      multicurveBumped.setForexMatrix(multicurve.getFxRates());
      for (Currency loopccy : multicurve.getCurrencies()) {
        multicurveBumped.setCurve(loopccy, curveBumped);
      }
      for (IborIndex loopibor : multicurve.getIndexesIbor()) {
        multicurveBumped.setCurve(loopibor, curveBumped);
      }
      for (IndexON loopon : multicurve.getIndexesON()) {
        multicurveBumped.setCurve(loopon, curveBumped);
      }
      psShift[loopnode] = _psc.calculateSensitivity(instrument, multicurveBumped);
      double[] psShiftArray = psShift[loopnode].getSensitivity(name, ccy).getData();
      for (int loopnode2 = 0; loopnode2 < nbNode; loopnode2++) {
        gammaArray[loopnode][loopnode2] = (psShiftArray[loopnode2] - ps0Array[loopnode2]) / _shift;
      }
    }
    DoubleMatrix2D gammaMat = new DoubleMatrix2D(gammaArray);
    return gammaMat;
  }

}
