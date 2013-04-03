/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 *
 */
public class InterpolatedYieldCurveSpecificationWithSecurities implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  private final LocalDate _curveDate;
  private final Currency _currency;
  private final String _name;
  private final Interpolator1D _interpolator;
  private final boolean _interpolateYield;
  private final SortedSet<FixedIncomeStripWithSecurity> _strips = new TreeSet<FixedIncomeStripWithSecurity>();

  public InterpolatedYieldCurveSpecificationWithSecurities(final LocalDate curveDate, final String name, final Currency currency, final Interpolator1D interpolator,
      final Collection<FixedIncomeStripWithSecurity> resolvedStrips) {
    this(curveDate, name, currency, interpolator, true, resolvedStrips);
  }

  public InterpolatedYieldCurveSpecificationWithSecurities(final LocalDate curveDate, final String name, final Currency currency, final Interpolator1D interpolator,
      final boolean interpolateYield, final Collection<FixedIncomeStripWithSecurity> resolvedStrips) {
    Validate.notNull(curveDate, "CurveDate");
    Validate.notNull(currency, "Currency");
    Validate.notNull(interpolator, "Interpolator1D");
    Validate.notNull(resolvedStrips, "ResolvedStrips");
    // Name can be null.
    _curveDate = curveDate;
    _currency = currency;
    _name = name;
    _interpolator = interpolator;
    _interpolateYield = interpolateYield;
    for (final FixedIncomeStripWithSecurity strip : resolvedStrips) {
      addStrip(strip);
    }
  }

  public void addStrip(final FixedIncomeStripWithSecurity strip) {
    ArgumentChecker.notNull(strip, "Strip");
    _strips.add(strip);
  }

  /**
   * @return the curve date
   */
  public LocalDate getCurveDate() {
    return _curveDate;
  }

  /**
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * @return the interpolator
   */
  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  public boolean interpolateYield() {
    return _interpolateYield;
  }
  /**
   * @return the strips
   */
  public Set<FixedIncomeStripWithSecurity> getStrips() {
    return Collections.unmodifiableSortedSet(_strips);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof InterpolatedYieldCurveSpecificationWithSecurities)) {
      return false;
    }
    final InterpolatedYieldCurveSpecificationWithSecurities other = (InterpolatedYieldCurveSpecificationWithSecurities) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_interpolator, other._interpolator)) {
      return false;
    }
    if (!ObjectUtils.equals(_strips, other._strips)) {
      return false;
    }
    if (_interpolateYield != other._interpolateYield) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 37;
    int result = 1;
    result = (result * prime) + _currency.hashCode();
    if (_name != null) {
      result = (result * prime) + _name.hashCode();
    }
    // since currency/name/date are a candidate key we leave it at that.
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
