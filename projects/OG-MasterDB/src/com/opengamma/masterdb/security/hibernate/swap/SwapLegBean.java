/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.masterdb.security.hibernate.BusinessDayConventionBean;
import com.opengamma.masterdb.security.hibernate.DayCountBean;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.FrequencyBean;

/**
 * A bean representation of a {@link SwapLeg}.
 */
public class SwapLegBean {

  // No identifier as this will be part of a SwapSecurityBean
  private SwapLegType _swapLegType;
  private DayCountBean _dayCount;
  private FrequencyBean _frequency;
  private ExternalIdBean _region;
  private BusinessDayConventionBean _businessDayConvention;
  private NotionalBean _notional;
  private Double _rate;
  private Double _spread;
  private ExternalIdBean _rateIdentifier;
  private boolean _eom;
  private FloatingRateType _floatingRateType;
  private Integer _settlementDays;
  private FrequencyBean _offsetFixing;
  private Double _gearing;
  
  /**
   * Gets the swapLegType.
   * @return the swapLegType
   */
  public SwapLegType getSwapLegType() {
    return _swapLegType;
  }
  /**
   * Sets the swapLegType.
   * @param swapLegType  the swapLegType
   */
  public void setSwapLegType(SwapLegType swapLegType) {
    _swapLegType = swapLegType;
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
   * Gets the region.
   * @return the region
   */
  public ExternalIdBean getRegion() {
    return _region;
  }
  /**
   * Sets the region.
   * @param region  the region
   */
  public void setRegion(ExternalIdBean region) {
    _region = region;
  }
  /**
   * Gets the businessDayConvention.
   * @return the businessDayConvention
   */
  public BusinessDayConventionBean getBusinessDayConvention() {
    return _businessDayConvention;
  }
  /**
   * Sets the businessDayConvention.
   * @param businessDayConvention  the businessDayConvention
   */
  public void setBusinessDayConvention(BusinessDayConventionBean businessDayConvention) {
    _businessDayConvention = businessDayConvention;
  }
  /**
   * Gets the notional.
   * @return the notional
   */
  public NotionalBean getNotional() {
    return _notional;
  }
  /**
   * Sets the notional.
   * @param notional  the notional
   */
  public void setNotional(NotionalBean notional) {
    _notional = notional;
  }
  /**
   * Gets the rate.
   * @return the rate
   */
  public Double getRate() {
    return _rate;
  }
  /**
   * Sets the rate.
   * @param rate  the rate
   */
  public void setRate(Double rate) {
    _rate = rate;
  }
  /**
   * Gets the spread.
   * @return the spread
   */
  public Double getSpread() {
    return _spread;
  }
  /**
   * Sets the spread.
   * @param spread  the spread
   */
  public void setSpread(Double spread) {
    _spread = spread;
  }
  /**
   * Gets the rateIdentifier.
   * @return the rateIdentifier
   */
  public ExternalIdBean getRateIdentifier() {
    return _rateIdentifier;
  }
  /**
   * Sets the rateIdentifier.
   * @param rateIdentifier  the rateIdentifier
   */
  public void setRateIdentifier(ExternalIdBean rateIdentifier) {
    _rateIdentifier = rateIdentifier;
  }
  /**
   * Gets the eom.
   * @return the eom
   */
  public boolean isEom() {
    return _eom;
  }
  /**
   * Sets the eom.
   * @param eom  the eom
   */
  public void setEom(boolean eom) {
    _eom = eom;
  }
  /**
   * Gets the floatingRateType.
   * @return the floatingRateType
   */
  public FloatingRateType getFloatingRateType() {
    return _floatingRateType;
  }
  /**
   * Sets the floatingRateType.
   * @param floatingRateType  the floatingRateType
   */
  public void setFloatingRateType(FloatingRateType floatingRateType) {
    _floatingRateType = floatingRateType;
  }
  /**
   * Gets the settlementDays.
   * @return the settlementDays
   */
  public Integer getSettlementDays() {
    return _settlementDays;
  }
  /**
   * Sets the settlementDays.
   * @param settlementDays  the settlementDays
   */
  public void setSettlementDays(Integer settlementDays) {
    _settlementDays = settlementDays;
  }
  /**
   * Gets the offsetFixing.
   * @return the offsetFixing
   */
  public FrequencyBean getOffsetFixing() {
    return _offsetFixing;
  }
  /**
   * Sets the offsetFixing.
   * @param offsetFixing  the offsetFixing
   */
  public void setOffsetFixing(FrequencyBean offsetFixing) {
    _offsetFixing = offsetFixing;
  }
  /**
   * Gets the gearing.
   * @return the gearing
   */
  public Double getGearing() {
    return _gearing;
  }
  /**
   * Sets the gearing.
   * @param gearing  the gearing
   */
  public void setGearing(Double gearing) {
    _gearing = gearing;
  }
  
}
