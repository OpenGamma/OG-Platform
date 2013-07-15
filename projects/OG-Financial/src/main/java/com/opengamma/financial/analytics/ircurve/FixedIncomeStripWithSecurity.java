/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class FixedIncomeStripWithSecurity implements Comparable<FixedIncomeStripWithSecurity> {
  private final FixedIncomeStrip _originalStrip;
  private final Tenor _resolvedTenor;
  private final ZonedDateTime _maturity;
  private final ExternalId _securityIdentifier;
  private final Security _security;

  /**
   * Gets the fixed income strip.
   * @return The fixed income strip
   */
  public FixedIncomeStrip getStrip() {
    return _originalStrip;
  }

  /**
   * Gets the instrumentType field.
   * @return the instrumentType
   */
  public StripInstrumentType getInstrumentType() {
    return _originalStrip.getInstrumentType();
  }

  /**
   * Gets the tenor field
   * @return the tenor
   */
  public Tenor getTenor() {
    return _originalStrip.getCurveNodePointTime();
  }

  /**
   * Gets the resolved tenor field
   * @return the tenor
   */
  public Tenor getResolvedTenor() {
    return _resolvedTenor;
  }

  /**
   * Get the number of the quarterly IR futures after the tenor to choose.
   * NOTE: THIS DOESN'T REFER TO A GENERIC FUTURE
   * @return number of futures after the tenor
   * @throws IllegalStateException if called on a non-future strip
   */
  public int getNumberOfFuturesAfterTenor() {
    return _originalStrip.getNumberOfFuturesAfterTenor();
  }

  /**
   * Get the periods per year of a periodic zero deposit security
   *
   * @return the number of periods per year
   * @throws IllegalStateException if called on a non-periodic zero deposit strip
   */
  public int getPeriodsPerYear() {
    return _originalStrip.getPeriodsPerYear();
  }

  /**
   * Gets the years field.
   * @return the years
   */
  public ZonedDateTime getMaturity() {
    return _maturity;
  }

  /**
   * Gets the identifier that was used to resolve the security
   * This is available, just so the same identifier can be conveniently used to retrieve requested market data.
   * @return The security identifier
   */
  public ExternalId getSecurityIdentifier() {
    return _securityIdentifier;
  }

  /**
   * Gets the security field.
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  public FixedIncomeStripWithSecurity(final FixedIncomeStrip originalStrip,
      final Tenor resolvedTenor,
      final ZonedDateTime maturity,
      final ExternalId securityIdentifier,
      final Security security) {
    Validate.notNull(originalStrip, "original strip");
    Validate.notNull(resolvedTenor, "resolved tenor");
    Validate.notNull(maturity, "maturity");
    Validate.notNull(securityIdentifier, "security identifier");
    Validate.notNull(security, "security");
    _originalStrip = originalStrip;
    _resolvedTenor = resolvedTenor;
    _maturity = maturity;
    _securityIdentifier = securityIdentifier;
    _security = security;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStripWithSecurity) {
      final FixedIncomeStripWithSecurity other = (FixedIncomeStripWithSecurity) obj;
      return ObjectUtils.equals(_originalStrip, other._originalStrip) &&
          ObjectUtils.equals(_maturity, other._maturity) &&
          ObjectUtils.equals(_security, other._security);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _maturity.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int compareTo(final FixedIncomeStripWithSecurity o) {
    int result = 0;
    if (getStrip().getInstrumentType() == StripInstrumentType.FUTURE || o.getStrip().getInstrumentType() == StripInstrumentType.FUTURE) {
      result = getMaturity().compareTo(o.getMaturity());
    }
    if (result != 0) {
      return result;
    }
    result = getStrip().compareTo(o.getStrip());
    if (result != 0) {
      return result;
    }
    result = getResolvedTenor().compareTo(o.getResolvedTenor());
    if (result != 0) {
      return result;
    }
    if (getSecurity().getUniqueId() == null) {
      if (o.getSecurity().getUniqueId() == null) {
        return 0;
      } else {
        //TODO check this
        return -1;
      }
    }
    result = getSecurity().getUniqueId().getValue().compareTo(o.getSecurity().getUniqueId().getValue());
    return result;
  }
}
