/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.masterdb.security.hibernate.BusinessDayConventionBean;
import com.opengamma.masterdb.security.hibernate.DayCountBean;
import com.opengamma.masterdb.security.hibernate.FrequencyBean;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.UniqueIdentifierBean;

/**
 * A bean representation of a {@link SwapLeg}.
 */
public class SwapLegBean {

  // No identifier as this will be part of a SwapSecurityBean
  private SwapLegType _swapLegType;
  private DayCountBean _dayCount;
  private FrequencyBean _frequency;
  private IdentifierBean _region;
  private BusinessDayConventionBean _businessDayConvention;
  private NotionalBean _notional;
  private double _rate;
  private double _spread;
  private UniqueIdentifierBean _rateIdentifier;
  private Boolean _ibor;

  public SwapLegType getSwapLegType() {
    return _swapLegType;
  }

  public void setSwapLegType(final SwapLegType swapLegType) {
    _swapLegType = swapLegType;
  }

  /**
   * Gets the dayCount field.
   * @return the dayCount
   */
  public DayCountBean getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the dayCount field.
   * @param dayCount  the dayCount
   */
  public void setDayCount(DayCountBean dayCount) {
    _dayCount = dayCount;
  }

  /**
   * Gets the frequency field.
   * @return the frequency
   */
  public FrequencyBean getFrequency() {
    return _frequency;
  }

  /**
   * Sets the frequency field.
   * @param frequency  the frequency
   */
  public void setFrequency(FrequencyBean frequency) {
    _frequency = frequency;
  }

  /**
   * Gets the region field.
   * @return the region
   */
  public IdentifierBean getRegion() {
    return _region;
  }

  /**
   * Sets the region field.
   * @param region  the region
   */
  public void setRegion(IdentifierBean region) {
    _region = region;
  }

  /**
   * Gets the businessDayConvention field.
   * @return the businessDayConvention
   */
  public BusinessDayConventionBean getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Sets the businessDayConvention field.
   * @param businessDayConvention  the businessDayConvention
   */
  public void setBusinessDayConvention(BusinessDayConventionBean businessDayConvention) {
    _businessDayConvention = businessDayConvention;
  }

  /**
   * Gets the notional field.
   * @return the notional
   */
  public NotionalBean getNotional() {
    return _notional;
  }

  /**
   * Sets the notional field.
   * @param notional  the notional
   */
  public void setNotional(NotionalBean notional) {
    _notional = notional;
  }

  /**
   * Gets the rate field.
   * @return the rate
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Sets the rate field.
   * @param rate  the rate
   */
  public void setRate(double rate) {
    _rate = rate;
  }

  /**
   * Gets the spread field.
   * @return the spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Sets the spread field.
   * @param spread  the spread
   */
  public void setSpread(double spread) {
    _spread = spread;
  }

  /**
   * Gets the rateIdentifier field.
   * @return the rateIdentifier
   */
  public UniqueIdentifierBean getRateIdentifier() {
    return _rateIdentifier;
  }

  /**
   * Sets the rateIdentifier field.
   * @param rateIdentifier  the rateIdentifier
   */
  public void setRateIdentifier(UniqueIdentifierBean rateIdentifier) {
    _rateIdentifier = rateIdentifier;
  }
  
  public Boolean isIBOR() {
    return _ibor;
  }
  
  public void setIBOR(final Boolean ibor) {
    _ibor = ibor;
  }
  
}
