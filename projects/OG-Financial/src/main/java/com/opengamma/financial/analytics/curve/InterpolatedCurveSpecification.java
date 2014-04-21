/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Specification for an interpolated curve.
 */
public class InterpolatedCurveSpecification extends CurveSpecification {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The interpolator name */
  private final String _interpolatorName;
  /** The right extrapolator name */
  private final String _rightExtrapolatorName;
  /** The left extrapolator name */
  private final String _leftExtrapolatorName;

  /**
   * @param curveDate The curve construction date, not null
   * @param name The curve name, not null
   * @param nodes The nodes that are used to construct this curve, not null
   * @param interpolatorName The interpolator name, not null
   * @param rightExtrapolatorName The right extrapolator name, not null
   * @param leftExtrapolatorName The left extrapolator name, not null
   */
  public InterpolatedCurveSpecification(final LocalDate curveDate, final String name, final Collection<CurveNodeWithIdentifier> nodes,
      final String interpolatorName, final String rightExtrapolatorName, final String leftExtrapolatorName) {
    super(curveDate, name, nodes);
    ArgumentChecker.notNull(interpolatorName, "interpolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    _interpolatorName = interpolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
    _leftExtrapolatorName = leftExtrapolatorName;
  }

  /**
   * Gets the interpolator name,
   * @return The interpolator name
   */
  public String getInterpolatorName() {
    return _interpolatorName;
  }

  /**
   * Gets the right extrapolator name.
   * @return The right extrapolator name
   */
  public String getRightExtrapolatorName() {
    return _rightExtrapolatorName;
  }

  /**
   * Gets the left extrapolator name.
   * @return The left extrapolator name
   */
  public String getLeftExtrapolatorName() {
    return _leftExtrapolatorName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _interpolatorName.hashCode();
    result = prime * result + _rightExtrapolatorName.hashCode();
    result = prime * result + _leftExtrapolatorName.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InterpolatedCurveSpecification)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final InterpolatedCurveSpecification other = (InterpolatedCurveSpecification) obj;
    return ObjectUtils.equals(_interpolatorName, other._interpolatorName) &&
        ObjectUtils.equals(_rightExtrapolatorName, other._rightExtrapolatorName) &&
        ObjectUtils.equals(_leftExtrapolatorName, other._leftExtrapolatorName);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
