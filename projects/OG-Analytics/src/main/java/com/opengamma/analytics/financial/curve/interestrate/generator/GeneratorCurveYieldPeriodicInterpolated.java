/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the rate (periodally compounded).
 * Only the lastTimeCalculator is stored. The node are computed from the instruments.
 */
@SuppressWarnings("deprecation")
public class GeneratorCurveYieldPeriodicInterpolated extends GeneratorYDCurve {

  /**
   * Calculator of the node associated to instruments.
   */
  private final InstrumentDerivativeVisitorAdapter<Object, Double> _nodeTimeCalculator;
  /**
   * The interpolator used for the curve.
   */
  private final Interpolator1D _interpolator;
  /**
   * The number of composition periods per year for the storage curve (1 for annual, 2 for semi-annual, etc.).
   */
  private final int _compoundingPeriodsPerYear;

  /**
   * Constructor.
   * @param nodeTimeCalculator Calculator of the node associated to instruments.
   * @param compoundingPeriodsPerYear The number of composition periods per year for the storage curve (1 for annual, 2 for semi-annual, etc.).
   * @param interpolator The interpolator used for the curve.
   */
  public GeneratorCurveYieldPeriodicInterpolated(final InstrumentDerivativeVisitorAdapter<Object, Double> nodeTimeCalculator, final int compoundingPeriodsPerYear, final Interpolator1D interpolator) {
    _nodeTimeCalculator = nodeTimeCalculator;
    _interpolator = interpolator;
    _compoundingPeriodsPerYear = compoundingPeriodsPerYear;
  }

  @Override
  public int getNumberOfParameter() {
    throw new UnsupportedOperationException("Cannot return the number of parameter for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  /**
   * {@inheritDoc}
   * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
   */
  @Deprecated
  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final YieldCurveBundle bundle, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface multicurve, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public GeneratorYDCurve finalGenerator(final Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    final InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    final double[] node = new double[instruments.length];
    for (int loopins = 0; loopins < instruments.length; loopins++) {
      node[loopins] = instruments[loopins].accept(_nodeTimeCalculator);
    }
    return new GeneratorCurveYieldPeriodicInterpolatedNode(node, _compoundingPeriodsPerYear, _interpolator);
  }

}
