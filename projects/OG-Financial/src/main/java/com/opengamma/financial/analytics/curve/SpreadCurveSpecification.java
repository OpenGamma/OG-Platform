/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * Specification for spread curves. There are two forms supported:
 * <ul>
 *   <li> Two curves and the operation to perform on these curves.
 *   <li> One curve, a constant spread value with units, and the operation.
 * </ul>
 * <p>
 * Note that the operation might not be commutative, so the order of the inputs is important.
 */
public class SpreadCurveSpecification extends AbstractCurveSpecification {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The first curve */
  private final AbstractCurveSpecification _firstCurve;
  /** The second curve */
  private final AbstractCurveSpecification _secondCurve;
  /** The operation */
  private final String _operation;

  /**
   * @param curveDate The curve date, not null
   * @param name The curve name, not null
   * @param firstCurve The first curve, not null
   * @param secondCurve The second curve, not null
   * @param operation The operation, not null
   */
  public SpreadCurveSpecification(final LocalDate curveDate, final String name, final AbstractCurveSpecification firstCurve,
      final AbstractCurveSpecification secondCurve, final String operation) {
    super(curveDate, name);
    ArgumentChecker.notNull(firstCurve, "first curve");
    ArgumentChecker.notNull(secondCurve, "second curve");
    ArgumentChecker.notNull(operation, "operation");
    _firstCurve = firstCurve;
    _secondCurve = secondCurve;
    _operation = operation;
  }

  /**
   * Gets the first curve.
   * @return the first curve
   */
  public AbstractCurveSpecification getFirstCurve() {
    return _firstCurve;
  }

  /**
   * Gets the second curve.
   * @return the second curve
   */
  public AbstractCurveSpecification getSecondCurve() {
    return _secondCurve;
  }

  /**
   * Gets the operation name.
   * @return the operation name
   */
  public String getOperation() {
    return _operation;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _firstCurve.hashCode();
    result = prime * result + _operation.hashCode();
    result = prime * result + _secondCurve.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof SpreadCurveSpecification)) {
      return false;
    }
    final SpreadCurveSpecification other = (SpreadCurveSpecification) obj;
    if (!ObjectUtils.equals(_operation, other._operation)) {
      return false;
    }
    if (!ObjectUtils.equals(_secondCurve, other._secondCurve)) {
      return false;
    }
    if (!ObjectUtils.equals(_firstCurve, other._firstCurve)) {
      return false;
    }
    return true;
  }

}
