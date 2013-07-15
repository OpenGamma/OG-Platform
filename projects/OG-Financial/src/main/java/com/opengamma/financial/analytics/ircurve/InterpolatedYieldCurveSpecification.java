/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 *
 */
public class InterpolatedYieldCurveSpecification implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  private final LocalDate _curveDate;
  private final Currency _currency;
  private final String _name;
  private final Interpolator1D _interpolator;
  private final boolean _interpolateYield;
  private final SortedSet<FixedIncomeStripWithIdentifier> _strips = new TreeSet<FixedIncomeStripWithIdentifier>();
  private final ExternalId _region;

  public InterpolatedYieldCurveSpecification(final LocalDate curveDate, final String name, final Currency currency,
      final Interpolator1D interpolator, final Collection<FixedIncomeStripWithIdentifier> resolvedStrips,
      final ExternalId region) {
    ArgumentChecker.notNull(curveDate, "CurveDate");
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(interpolator, "Interpolator1D");
    ArgumentChecker.notNull(resolvedStrips, "ResolvedStrips");
    ArgumentChecker.notNull(region, "RegionID");
    // Name can be null.
    _curveDate = curveDate;
    _currency = currency;
    _name = name;
    _interpolator = interpolator;
    _interpolateYield = true;
    _region = region;
    for (final FixedIncomeStripWithIdentifier strip : resolvedStrips) {
      addStrip(strip);
    }
  }

  public InterpolatedYieldCurveSpecification(final LocalDate curveDate, final String name, final Currency currency,
      final Interpolator1D interpolator, final Collection<FixedIncomeStripWithIdentifier> resolvedStrips,
      final ExternalId region, final Tenor fraBasis, final Tenor swapBasis) {
    ArgumentChecker.notNull(curveDate, "CurveDate");
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(interpolator, "Interpolator1D");
    ArgumentChecker.notNull(resolvedStrips, "ResolvedStrips");
    ArgumentChecker.notNull(region, "RegionID");
    // Name can be null.
    _curveDate = curveDate;
    _currency = currency;
    _name = name;
    _interpolator = interpolator;
    _interpolateYield = true;
    _region = region;
    for (final FixedIncomeStripWithIdentifier strip : resolvedStrips) {
      addStrip(strip);
    }
  }

  public InterpolatedYieldCurveSpecification(final LocalDate curveDate, final String name, final Currency currency,
      final Interpolator1D interpolator, final boolean interpolateYield, final Collection<FixedIncomeStripWithIdentifier> resolvedStrips,
      final ExternalId region) {
    ArgumentChecker.notNull(curveDate, "CurveDate");
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(interpolator, "Interpolator1D");
    ArgumentChecker.notNull(resolvedStrips, "ResolvedStrips");
    ArgumentChecker.notNull(region, "RegionID");
    // Name can be null.
    _curveDate = curveDate;
    _currency = currency;
    _name = name;
    _interpolator = interpolator;
    _interpolateYield = interpolateYield;
    _region = region;
    for (final FixedIncomeStripWithIdentifier strip : resolvedStrips) {
      addStrip(strip);
    }
  }

  public InterpolatedYieldCurveSpecification(final LocalDate curveDate, final String name, final Currency currency,
      final Interpolator1D interpolator, final boolean interpolateYield, final Collection<FixedIncomeStripWithIdentifier> resolvedStrips,
      final ExternalId region, final Tenor fraBasis, final Tenor swapBasis) {
    ArgumentChecker.notNull(curveDate, "CurveDate");
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(interpolator, "Interpolator1D");
    ArgumentChecker.notNull(resolvedStrips, "ResolvedStrips");
    ArgumentChecker.notNull(region, "RegionID");
    // Name can be null.
    _curveDate = curveDate;
    _currency = currency;
    _name = name;
    _interpolator = interpolator;
    _interpolateYield = interpolateYield;
    _region = region;
    for (final FixedIncomeStripWithIdentifier strip : resolvedStrips) {
      addStrip(strip);
    }
  }
  public void addStrip(final FixedIncomeStripWithIdentifier strip) {
    ArgumentChecker.notNull(strip, "Strip");
    _strips.add(strip);
  }

  /**
   * Gets the region field.
   * @return the region
   */
  public ExternalId getRegion() {
    return _region;
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

  /**
   * @return Whether to interpolate the yield (true) or discount factors (false)
   */
  public boolean interpolateYield() {
    return _interpolateYield;
  }

  /**
   * @return the strips
   */
  public SortedSet<FixedIncomeStripWithIdentifier> getStrips() {
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
    if (!(obj instanceof InterpolatedYieldCurveSpecification)) {
      return false;
    }
    final InterpolatedYieldCurveSpecification other = (InterpolatedYieldCurveSpecification) obj;
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
    if (!ObjectUtils.equals(_region, other._region)) {
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
