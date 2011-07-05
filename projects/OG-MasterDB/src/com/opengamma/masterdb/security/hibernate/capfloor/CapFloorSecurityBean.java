/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.capfloor;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.DayCountBean;
import com.opengamma.masterdb.security.hibernate.FrequencyBean;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of {@link CapFloorSecurity}.
 */
public class CapFloorSecurityBean extends SecurityBean {
  private CurrencyBean _currency;
  private DayCountBean _dayCount;
  private FrequencyBean _frequency;
  private boolean _isCap;
  private boolean _isIbor;
  private boolean _isPayer;
  private ZonedDateTimeBean _maturityDate;
  private double _notional;
  private ZonedDateTimeBean _startDate;
  private double _strike;
  private IdentifierBean _underlyingIdentifier;
  
  /**
   * Gets the currency.
   * @return the currency
   */
  public CurrencyBean getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the currency
   */
  public void setCurrency(CurrencyBean currency) {
    _currency = currency;
  }

  /**
   * Gets the dayCount.
   * @return the dayCount
   */
  public DayCountBean getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the dayCount.
   * @param dayCount  the dayCount
   */
  public void setDayCount(DayCountBean dayCount) {
    _dayCount = dayCount;
  }

  /**
   * Gets the frequency.
   * @return the frequency
   */
  public FrequencyBean getFrequency() {
    return _frequency;
  }

  /**
   * Sets the frequency.
   * @param frequency  the frequency
   */
  public void setFrequency(FrequencyBean frequency) {
    _frequency = frequency;
  }

  /**
   * Gets the isCap.
   * @return the isCap
   */
  public boolean isCap() {
    return _isCap;
  }

  /**
   * Sets the isCap.
   * @param isCap  the isCap
   */
  public void setCap(boolean isCap) {
    _isCap = isCap;
  }

  /**
   * Gets the isIbor.
   * @return the isIbor
   */
  public boolean isIbor() {
    return _isIbor;
  }

  /**
   * Sets the isIbor.
   * @param isIbor  the isIbor
   */
  public void setIbor(boolean isIbor) {
    _isIbor = isIbor;
  }

  /**
   * Gets the isPayer.
   * @return the isPayer
   */
  public boolean isPayer() {
    return _isPayer;
  }

  /**
   * Sets the isPayer.
   * @param isPayer  the isPayer
   */
  public void setPayer(boolean isPayer) {
    _isPayer = isPayer;
  }

  /**
   * Gets the maturityDate.
   * @return the maturityDate
   */
  public ZonedDateTimeBean getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the maturityDate.
   * @param maturityDate  the maturityDate
   */
  public void setMaturityDate(ZonedDateTimeBean maturityDate) {
    _maturityDate = maturityDate;
  }

  /**
   * Gets the notional.
   * @return the notional
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Sets the notional.
   * @param notional  the notional
   */
  public void setNotional(double notional) {
    _notional = notional;
  }

  /**
   * Gets the startDate.
   * @return the startDate
   */
  public ZonedDateTimeBean getStartDate() {
    return _startDate;
  }

  /**
   * Sets the startDate.
   * @param startDate  the startDate
   */
  public void setStartDate(ZonedDateTimeBean startDate) {
    _startDate = startDate;
  }

  /**
   * Gets the strike.
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Sets the strike.
   * @param strike  the strike
   */
  public void setStrike(double strike) {
    _strike = strike;
  }

  /**
   * Gets the underlyingIdentifier.
   * @return the underlyingIdentifier
   */
  public IdentifierBean getUnderlyingIdentifier() {
    return _underlyingIdentifier;
  }

  /**
   * Sets the underlyingIdentifier.
   * @param underlyingIdentifier  the underlyingIdentifier
   */
  public void setUnderlyingIdentifier(IdentifierBean underlyingIdentifier) {
    _underlyingIdentifier = underlyingIdentifier;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof CapFloorSecurityBean)) {
      return false;
    }
    CapFloorSecurityBean capFloor = (CapFloorSecurityBean) other;
    return new EqualsBuilder()
        .append(getId(), capFloor.getId())
        .append(getNotional(), capFloor.getNotional())
        .append(getCurrency(), capFloor.getCurrency())
        .append(getDayCount(), capFloor.getDayCount())
        .append(getFrequency(), capFloor.getFrequency())
        .append(getMaturityDate(), capFloor.getMaturityDate())
        .append(getStartDate(), capFloor.getStartDate())
        .append(getStrike(), capFloor.getStrike())
        .append(getUnderlyingIdentifier(), capFloor.getUnderlyingIdentifier())
        .append(isCap(), capFloor.isCap())
        .append(isIbor(), capFloor.isIbor())
        .append(isPayer(), capFloor.isPayer())
        .isEquals();
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(getNotional())
        .append(getCurrency())
        .append(getDayCount())
        .append(getFrequency())
        .append(getMaturityDate())
        .append(getStartDate())
        .append(getStrike())
        .append(getUnderlyingIdentifier())
        .append(isCap())
        .append(isIbor())
        .append(isPayer())
        .toHashCode();
  }

}
