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
public class ConstantSpreadCurveSpecification extends AbstractCurveSpecification {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The curve */
  private final AbstractCurveSpecification _curve;
  /** The spread */
  private final Double _spread;
  /** The units */
  private final String _units;
  /** The operation */
  private final String _operation;

  /**
   * @param curveDate The curve date, not null
   * @param name The curve name, not null
   * @param curve The curve, not null
   * @param spread The spread
   * @param units The units, not null
   * @param operation The operation, not null
   */
  public ConstantSpreadCurveSpecification(final LocalDate curveDate, final String name, final AbstractCurveSpecification curve,
      final double spread, final String units, final String operation) {
    super(curveDate, name);
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(units, "units");
    ArgumentChecker.notNull(operation, "operation");
    _curve = curve;
    _spread = spread;
    _units = units;
    _operation = operation;
  }

  /**
   * Gets the curve.
   * @return the curve
   */
  public AbstractCurveSpecification getCurve() {
    return _curve;
  }

  /**
   * Gets the constant spread.
   * @return The spread
   * @throws UnsupportedOperationException If the spread curve is not constructed from a curve and a
   * spread value.
   */
  public Double getSpread() {
    return _spread;
  }

  /**
   * Gets the constant spread units.
   * @return The units
   * @throws UnsupportedOperationException If the spread curve is not constructed from a curve and a
   * spread value.
   */
  public String getUnits() {
    return _units;
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
    result = prime * result + _curve.hashCode();
    result = prime * result + _operation.hashCode();
    result = prime * result + _spread.hashCode();
    result = prime * result + _units.hashCode();
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
    if (!(obj instanceof ConstantSpreadCurveSpecification)) {
      return false;
    }
    final ConstantSpreadCurveSpecification other = (ConstantSpreadCurveSpecification) obj;
    if (!ObjectUtils.equals(_operation, other._operation)) {
      return false;
    }
    if (!ObjectUtils.equals(_units, other._units)) {
      return false;
    }
    if (!ObjectUtils.equals(_spread, other._spread)) {
      return false;
    }
    if (!ObjectUtils.equals(_curve, other._curve)) {
      return false;
    }
    return true;
  }

}
