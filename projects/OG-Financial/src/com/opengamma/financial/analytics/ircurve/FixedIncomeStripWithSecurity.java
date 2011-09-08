/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 *  
 */
public class FixedIncomeStripWithSecurity implements Comparable<FixedIncomeStripWithSecurity> {
  private final StripInstrumentType _instrumentType;
  private final Tenor _tenor;
  private final Tenor _resolvedTenor;
  private int _nthFutureFromTenor;
  //private Tenor _floatingLength;
  private final ZonedDateTime _maturity;
  private final ExternalId _securityIdentifier;
  private final Security _security;

  /**
   * Gets the instrumentType field.
   * @return the instrumentType
   */
  public StripInstrumentType getInstrumentType() {
    return _instrumentType;
  }

  /**
   * Gets the tenor field
   * @return the tenor
   */
  public Tenor getTenor() {
    return _tenor;
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
    if (_instrumentType != StripInstrumentType.FUTURE) {
      throw new IllegalStateException("Cannot get number of futures after tenor for a non future strip " + toString());
    }
    return _nthFutureFromTenor;
  }

  /**
   * Get the tenor of the floating rate
   * @return The tenor
   * @throws IllegalStateException if called on a non-FRA or non-swap strip
   */
//  public Tenor getFloatingLength() {
//    if (_instrumentType == StripInstrumentType.FRA || _instrumentType == StripInstrumentType.SWAP) {
//      return _floatingLength;
//    }
//    throw new IllegalStateException("Cannot get floating length for a non-FRA or non-swap security " + toString());
//  }
//  
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

  public FixedIncomeStripWithSecurity(final StripInstrumentType instrumentType, final Tenor originalTenor,
                                      final Tenor resolvedTenor, final int nthFutureFromOriginalTenor,
                                      final ZonedDateTime maturity, final ExternalId securityIdentifier,
                                      final Security security) {
    Validate.isTrue(instrumentType == StripInstrumentType.FUTURE, "Trying to create a node with a nthFutureFromOriginalTenor param when not a future node");
    _instrumentType = instrumentType;
    _tenor = originalTenor;
    _resolvedTenor = resolvedTenor;
    _nthFutureFromTenor = nthFutureFromOriginalTenor;
    _maturity = maturity;
    _securityIdentifier = securityIdentifier;
    _security = security;
  }

  public FixedIncomeStripWithSecurity(final StripInstrumentType instrumentType, final Tenor originalTenor,
      final Tenor resolvedTenor, final ZonedDateTime maturity, final ExternalId securityIdentifier,
      final Security security) {
    Validate.isTrue(instrumentType != StripInstrumentType.FUTURE, "Trying to create a node without a nthFutureFromOriginalTenor param when a future node");
  //  Validate.isTrue(instrumentType != StripInstrumentType.FRA || instrumentType != StripInstrumentType.SWAP, "Trying to create a node without a floating length tenor when a swap or FRA node");
    _instrumentType = instrumentType;
    _tenor = originalTenor;
    _resolvedTenor = resolvedTenor;
    _maturity = maturity;
    _securityIdentifier = securityIdentifier;
    _security = security;
  }

//  public FixedIncomeStripWithSecurity(final StripInstrumentType instrumentType, final Tenor originalTenor,
//      final Tenor resolvedTenor, Tenor floatingTenor, final ZonedDateTime maturity, final ExternalId securityIdentifier,
//      final Security security) {
//    Validate.isTrue(instrumentType == StripInstrumentType.FRA || instrumentType == StripInstrumentType.SWAP, "Trying to create a node with a floating length tenor when not a swap or FRA node"); 
//    _instrumentType = instrumentType;
//    _tenor = originalTenor;
//    _resolvedTenor = resolvedTenor;
//    _floatingLength = floatingTenor;
//    _maturity = maturity;
//    _securityIdentifier = securityIdentifier;
//    _security = security;
//  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStripWithSecurity) {
      final FixedIncomeStripWithSecurity other = (FixedIncomeStripWithSecurity) obj;
      return ObjectUtils.equals(_tenor, other._tenor) &&
             ObjectUtils.equals(_nthFutureFromTenor, other._nthFutureFromTenor) &&
    //         ObjectUtils.equals(_floatingLength, other._floatingLength) &&
             ObjectUtils.equals(_maturity, other._maturity) &&
             ObjectUtils.equals(_security, other._security) &&
             _instrumentType == other._instrumentType;
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
    int result = getMaturity().compareTo(o.getMaturity());
    if (result != 0) {
      return result;
    }
    result = getInstrumentType().ordinal() - o.getInstrumentType().ordinal();
    if (result != 0) {
      return result;
    }
//    if (getInstrumentType() == StripInstrumentType.FRA || getInstrumentType() == StripInstrumentType.SWAP) {
//      result = getFloatingLength().compareTo(o.getFloatingLength());
//      if (result != 0) {
//        return result;
//      }
//    }
    if (getInstrumentType() == StripInstrumentType.FUTURE) {
      result = getNumberOfFuturesAfterTenor() - o.getNumberOfFuturesAfterTenor();
      if (result != 0) {
        return result;
      }
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
