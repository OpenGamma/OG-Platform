/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * Base class for curve specifications, which contain at least the curve date and name.
 */
public abstract class AbstractCurveSpecification implements Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The curve date */
  private final LocalDate _curveDate;
  /** The curve name */
  private final String _name;

  /**
   * @param curveDate The curve date, not null
   * @param name The curve name, not null
   */
  public AbstractCurveSpecification(final LocalDate curveDate, final String name) {
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(name, "name");
    _curveDate = curveDate;
    _name = name;
  }

  /**
   * Gets the curve date.
   * @return The curve date
   */
  public LocalDate getCurveDate() {
    return _curveDate;
  }

  /**
   * Gets the curve name.
   * @return The curve name
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveDate.hashCode();
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AbstractCurveSpecification)) {
      return false;
    }
    final AbstractCurveSpecification other = (AbstractCurveSpecification) obj;
    if (!ObjectUtils.equals(_curveDate, other._curveDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}
