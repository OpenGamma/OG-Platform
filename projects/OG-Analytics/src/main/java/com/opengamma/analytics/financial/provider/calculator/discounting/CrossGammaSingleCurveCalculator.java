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
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Computes the cross-gamma to the curve parameters for a single curve.
 * The curve should be represented by a YieldCurve with an InterpolatedDoublesCurve on the zero-coupon rates.
 * By default the gamma is computed using a one basis-point shift. This default can be change in a constructor.
 * The results themselves are not scaled (the represent the second order derivative).
 * <p> Reference: Interest rate cross-gamma for single curve. OpenGamma Quantitative Analysis 1, August 14
 */
public class CrossGammaSingleCurveCalculator {

  /** Default size of bump: 1 basis point. */
  private static final double BP1 = 1.0E-4;
  
  /** The tool used to differentiate the delta. */
  private final VectorFieldFirstOrderDifferentiator _differentiator;
  /** The sensitivity calculator to the curve parameters used for the delta computation */
  private final ParameterSensitivityParameterCalculator<ParameterProviderInterface> _psc;
  /** The shift used for finite difference Gamma using two deltas. */
  private final double _shift;

  /**
   * Constructor. 
   * The default shift is used (1.0E-4 = 1 basis point). The default finite difference is used (FiniteDifferenceType = FORWARD)
   * @param curveSensitivityCalculator The delta (curve sensitivity) calculator.
   */
  public CrossGammaSingleCurveCalculator(
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    _psc = new ParameterSensitivityParameterCalculator<>(curveSensitivityCalculator);
    _shift = BP1;
    _differentiator = new VectorFieldFirstOrderDifferentiator(FiniteDifferenceType.FORWARD, _shift);
  }

  /**
   * Constructor.
   * @param shift The shift used for finite difference Gamma using two deltas.
   * @param curveSensitivityCalculator The delta (curve sensitivity) calculator.
   */
  public CrossGammaSingleCurveCalculator(final double shift,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    _psc = new ParameterSensitivityParameterCalculator<>(curveSensitivityCalculator);
    _shift = shift;
    _differentiator = new VectorFieldFirstOrderDifferentiator(FiniteDifferenceType.FORWARD, _shift);
  }

  /**
   * Computes the gamma matrix for a given instrument. The curve provider should contain only one curve which should be of the 
   * type YieldCurve with an underlying InterpolatedDoublesCurve.
   * @param instrument The instrument for which the cross-gamma should be computed.
   * @param multicurve The multi-curve provider.
   * @return The cross-gamma matrix.
   */
  public DoubleMatrix2D calculateCrossGamma(final InstrumentDerivative instrument,
      final MulticurveProviderDiscount multicurve) {
    ArgumentChecker.isTrue(multicurve.getAllNames().size() == 1,
        "provider should have only one curve for GammaSingleCurve computation");
    String name = multicurve.getAllNames().iterator().next();
    Currency ccy = multicurve.getCurrencyForName(name);
    YieldAndDiscountCurve curve = multicurve.getCurve(name);
    ArgumentChecker.isTrue(curve instanceof YieldCurve || curve instanceof DiscountCurve, 
        "curve should be YieldCurve or DiscountCurve");
    boolean isZc = curve instanceof YieldCurve;
    InterpolatedDoublesCurve interpolatedCurve;
    if (isZc) {
      YieldCurve yieldCurve = (YieldCurve) curve;
      ArgumentChecker.isTrue(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve,
          "Yield curve should be based on InterpolatedDoublesCurve");
      interpolatedCurve = (InterpolatedDoublesCurve) yieldCurve.getCurve();
    } else {
      DiscountCurve discountCurve = (DiscountCurve) curve;
      ArgumentChecker.isTrue(discountCurve.getCurve() instanceof InterpolatedDoublesCurve,
          "Discount curve should be based on InterpolatedDoublesCurve");
      interpolatedCurve = (InterpolatedDoublesCurve) discountCurve.getCurve();
    }
    double[] y = interpolatedCurve.getYDataAsPrimitive();
    double[] x = interpolatedCurve.getXDataAsPrimitive();
    int nbNode = y.length;
    MultipleCurrencyParameterSensitivity ps0 = _psc.calculateSensitivity(instrument, multicurve);
    DoubleMatrix1D ps0Mat = ps0.getSensitivity(name, ccy);
    double[] ps0Array = ps0Mat.getData();
    MultipleCurrencyParameterSensitivity[] psShift = new MultipleCurrencyParameterSensitivity[nbNode];
    double[][] gammaArray = new double[nbNode][nbNode];
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      double[] parametersBumped = y.clone();
      parametersBumped[loopnode] += _shift;
      YieldAndDiscountCurve curveBumped;
      if (isZc) {
        curveBumped = new YieldCurve(name,
            new InterpolatedDoublesCurve(x, parametersBumped, interpolatedCurve.getInterpolator(), true));
      } else {
        curveBumped = new DiscountCurve(name,
            new InterpolatedDoublesCurve(x, parametersBumped, interpolatedCurve.getInterpolator(), true));
      }
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
    // Due to approximation using a finite difference approach, the matrix computed may be (slightly) non-symmetrical.
    // The matrix is made symmetric by using only one half.
    for (int loopnode1 = 1; loopnode1 < nbNode; loopnode1++) {
      for (int loopnode2 = loopnode1; loopnode2 < nbNode; loopnode2++) {
        gammaArray[loopnode2][loopnode1] = gammaArray[loopnode1][loopnode2];
      }      
    }
    DoubleMatrix2D gammaMat = new DoubleMatrix2D(gammaArray);
    return gammaMat;
  }
  
  /**
   * Computes the gamma "sum-of-column" for a given instrument. See the documentation for the definition.
   * The curve provider should contain only one curve which should be of the 
   * type YieldCurve with an underlying InterpolatedDoublesCurve.
   * @param instrument The instrument for which the cross-gamma should be computed.
   * @param multicurve The multi-curve provider.
   * @return The gamma "sum-of-columns" vector.
   */
  public double[] calculateSumOfColumnsGamma(final InstrumentDerivative instrument,
      final MulticurveProviderDiscount multicurve) {
    ArgumentChecker.isTrue(multicurve.getAllNames().size() == 1,
        "provider should have only one curve for GammaSingleCurve computation");
    String name = multicurve.getAllNames().iterator().next();
    YieldAndDiscountCurve curve = multicurve.getCurve(name);
    ArgumentChecker.isTrue(curve instanceof YieldCurve, "curve should be YieldCurve");
    YieldCurve yieldCurve = (YieldCurve) curve;
    ArgumentChecker.isTrue(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve,
        "Yield curve should be based on InterpolatedDoublesCurve");
    Delta deltaShift = new Delta(multicurve, instrument, _psc);
    Function1D<DoubleMatrix1D, DoubleMatrix2D> gammaFn = _differentiator.differentiate(deltaShift);
    double[][] gamma2 = gammaFn.evaluate(new DoubleMatrix1D(new double[1])).getData();
    double[] gamma = new double[gamma2.length];
    for (int i = 0; i < gamma2.length; i++) {
      gamma[i] = gamma2[i][0];
    }
    return gamma;
  }

}

/**
 * Inner class to compute the delta for a given parallel shift of the curve.
 */
class Delta extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {  
  private final double[] _x;
  private final double[] _y;
  private final int _nbNode;
  private final String _name;
  private final MulticurveProviderDiscount _multicurve;
  private final InstrumentDerivative _instrument;
  private final Currency _ccy;
  private final InterpolatedDoublesCurve _interpolatedCurve;
  private final ParameterSensitivityParameterCalculator<ParameterProviderInterface> _psc;
  
  public Delta(MulticurveProviderDiscount multicurve, InstrumentDerivative instrument, 
      ParameterSensitivityParameterCalculator<ParameterProviderInterface> psc) {
    _multicurve = multicurve;
    _name = multicurve.getAllNames().iterator().next();
    _instrument = instrument;
    _ccy = multicurve.getCurrencyForName(_name);
    YieldAndDiscountCurve curve = multicurve.getCurve(_name);
    YieldCurve yieldCurve = (YieldCurve) curve;
    _interpolatedCurve = (InterpolatedDoublesCurve) yieldCurve.getCurve();
    _y = _interpolatedCurve.getYDataAsPrimitive();
    _x = _interpolatedCurve.getXDataAsPrimitive();
    _nbNode = _x.length;
    _psc = psc;
  }

  @Override
  public DoubleMatrix1D evaluate(DoubleMatrix1D s) {
    double shift = s.getEntry(0);
    final double[] yieldBumped = _y.clone();
    for (int loopnode = 0; loopnode < _nbNode; loopnode++) {
      yieldBumped[loopnode] += shift;
    }
    final YieldAndDiscountCurve curveBumped = new YieldCurve(_name,
        new InterpolatedDoublesCurve(_x, yieldBumped, _interpolatedCurve.getInterpolator(), true));
    MulticurveProviderDiscount multicurveBumped = new MulticurveProviderDiscount();
    multicurveBumped.setForexMatrix(_multicurve.getFxRates());
    for (Currency loopccy : _multicurve.getCurrencies()) {
      multicurveBumped.setCurve(loopccy, curveBumped);
    }
    for (IborIndex loopibor : _multicurve.getIndexesIbor()) {
      multicurveBumped.setCurve(loopibor, curveBumped);
    }
    for (IndexON loopon : _multicurve.getIndexesON()) {
      multicurveBumped.setCurve(loopon, curveBumped);
    }
    MultipleCurrencyParameterSensitivity psShift = _psc.calculateSensitivity(_instrument, multicurveBumped);
    double[] ps = psShift.getSensitivity(_name, _ccy).getData();
    return new DoubleMatrix1D(ps);
  }
  
}
