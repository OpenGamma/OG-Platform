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
 * Store the details and generate the required curve. The curve is interpolated on the rate (continuously compounded).
 * Only the lastTimeCalculator is stored. The node are computed from the instruments. The number of instruments is set exogenously.
 */
@SuppressWarnings("deprecation")
public class GeneratorCurveYieldInterpolatedNumber extends GeneratorYDCurve {

  /**
   * Calculator of the node associated to instruments.
   */
  private final InstrumentDerivativeVisitorAdapter<Object, Double> _nodeTimeCalculator;
  /**
   * The interpolator used for the curve.
   */
  private final Interpolator1D _interpolator;
  /**
   * The number of nodes in the interpolated curve.
   */
  private final int _numberNode;

  /**
   * Constructor.
   * @param nodeTimeCalculator Calculator of the node associated to instruments.
   * @param numberNode The number of node in the interpolated curve.
   * @param interpolator The interpolator used for the curve.
   */
  public GeneratorCurveYieldInterpolatedNumber(final InstrumentDerivativeVisitorAdapter<Object, Double> nodeTimeCalculator, final int numberNode, final Interpolator1D interpolator) {
    _nodeTimeCalculator = nodeTimeCalculator;
    _interpolator = interpolator;
    _numberNode = numberNode;
  }

  @Override
  public int getNumberOfParameter() {
    return _numberNode;
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
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface bundle, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public GeneratorYDCurve finalGenerator(final Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    final InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    final double[] node = new double[_numberNode];
    for (int loopins = 0; loopins < _numberNode; loopins++) {
      node[loopins] = instruments[loopins].accept(_nodeTimeCalculator);
    }
    return new GeneratorCurveYieldInterpolatedNode(node, _interpolator);
  }

}
