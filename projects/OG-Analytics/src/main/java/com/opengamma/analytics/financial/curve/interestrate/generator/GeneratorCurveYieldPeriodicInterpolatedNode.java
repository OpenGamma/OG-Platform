/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the rate (periodically compounded).
 */
@SuppressWarnings("deprecation")
public class GeneratorCurveYieldPeriodicInterpolatedNode extends GeneratorYDCurve {

  /**
   * The nodes (times) on which the interpolated curves is constructed.
   */
  private final double[] _nodePoints;
  /**
   * The interpolator used for the curve.
   */
  private final Interpolator1D _interpolator;
  /**
   * The number of points (or nodes). Is the length of _nodePoints.
   */
  private final int _nbPoints;
  /**
   * The number of composition periods per year for the storage curve (1 for annual, 2 for semi-annual, etc.).
   */
  private final int _compoundingPeriodsPerYear;

  /**
   * Constructor.
   * @param nodePoints The node points (X) used to define the interpolated curve.
   * @param compoundingPeriodsPerYear The number of composition periods per year for the storage curve (1 for annual, 2 for semi-annual, etc.).
   * @param interpolator The interpolator.
   */
  public GeneratorCurveYieldPeriodicInterpolatedNode(final double[] nodePoints, final int compoundingPeriodsPerYear, final Interpolator1D interpolator) {
    ArgumentChecker.notNull(nodePoints, "Node points");
    ArgumentChecker.notNull(interpolator, "Interpolator");
    _nodePoints = nodePoints;
    _nbPoints = _nodePoints.length;
    _interpolator = interpolator;
    _compoundingPeriodsPerYear = compoundingPeriodsPerYear;
  }

  @Override
  public int getNumberOfParameter() {
    return _nbPoints;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final double[] x) {
    ArgumentChecker.isTrue(x.length == _nbPoints, "Incorrect dimension for the rates");
    return new YieldPeriodicCurve(name, _compoundingPeriodsPerYear, new InterpolatedDoublesCurve(_nodePoints, x, _interpolator, true, name));
  }

  /**
   * {@inheritDoc}
   * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
   */
  @Deprecated
  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final YieldCurveBundle bundle, final double[] parameters) {
    return generateCurve(name, parameters);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface multicurve, final double[] parameters) {
    return generateCurve(name, parameters);
  }

}
