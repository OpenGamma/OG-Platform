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
  /** The spread */
  private final Double _spread;
  /** The units */
  private final String _units;
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
    _spread = null;
    _units = null;
    _operation = operation;
  }

  /**
   * @param curveDate The curve date, not null
   * @param name The curve name, not null
   * @param curve The curve, not null
   * @param spread The spread
   * @param units The units, not null
   * @param operation The operation, not null
   */
  public SpreadCurveSpecification(final LocalDate curveDate, final String name, final AbstractCurveSpecification curve,
      final double spread, final String units, final String operation) {
    super(curveDate, name);
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(units, "units");
    ArgumentChecker.notNull(operation, "operation");
    _firstCurve = curve;
    _secondCurve = null;
    _spread = spread;
    _units = units;
    _operation = operation;
  }

  /**
   * Returns true if the spread curve is constructed using a curve
   * and a constant value.
   * @return True if the spread curve is represented as a curve, operation and
   * constant value.
   */
  public boolean isNumericalSpread() {
    return _units != null;
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
   * @throws UnsupportedOperationException If the spread curve is not constructed from two curves.
   */
  public AbstractCurveSpecification getSecondCurve() {
    if (_secondCurve != null) {
      return _secondCurve;
    }
    throw new UnsupportedOperationException("Spread curve is of the form (curve " + _operation + " " + _spread + ")");
  }

  /**
   * Gets the constant spread.
   * @return The spread
   * @throws UnsupportedOperationException If the spread curve is not constructed from a curve and a
   * spread value.
   */
  public Double getSpread() {
    if (_spread != null) {
      return _spread;
    }
    throw new UnsupportedOperationException("Spread curve is of the form (curve " + _operation + " curve)");
  }

  /**
   * Gets the constant spread units.
   * @return The units
   * @throws UnsupportedOperationException If the spread curve is not constructed from a curve and a
   * spread value.
   */
  public String getUnits() {
    if (_units != null) {
      return _units;
    }
    throw new UnsupportedOperationException("Spread curve is of the form (curve " + _operation + " curve)");
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
    result = prime * result + ((_secondCurve == null) ? 0 : _secondCurve.hashCode());
    result = prime * result + ((_spread == null) ? 0 : _spread.hashCode());
    result = prime * result + ((_units == null) ? 0 : _units.hashCode());
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
    if (!ObjectUtils.equals(_units, other._units)) {
      return false;
    }
    if (_spread != null) {
      if (other._spread != null) {
        return Double.compare(_spread, other._spread) == 0;
      }
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
