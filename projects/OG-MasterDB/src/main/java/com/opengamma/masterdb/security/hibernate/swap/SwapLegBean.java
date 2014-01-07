/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.VarianceSwapType;
import com.opengamma.masterdb.security.hibernate.BusinessDayConventionBean;
import com.opengamma.masterdb.security.hibernate.DayCountBean;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.FrequencyBean;

/**
 * A Hibernate bean representation of {@link SwapLeg}.
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
  private Double _strike;
  private VarianceSwapType _varianceSwapType;
  private ExternalIdBean _underlyingId;
  private FrequencyBean _monitoringFrequency;
  private Double _annualizationFactor;
  private Integer _conventionalIndexationLag;
  private Integer _actualIndexationLag;
  private InterpolationMethod _indexInterpolationMethod;

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

  /**
   * Gets the strike.  For fixed variance swap legs.
   * @return The strike
   */
  public Double getStrike() {
    return _strike;
  }

  /**
   * Sets the strike.  For fixed variance swap legs.
   * @param strike The strike
   */
  public void setStrike(Double strike) {
    _strike = strike;
  }

  /**
   * Gets the variance swap type.  For fixed variance swap legs.
   * @return The variance swap type
   */
  public VarianceSwapType getVarianceSwapType() {
    return _varianceSwapType;
  }

  /**
   * Sets the variance swap type.  For fixed variance swap legs.
   * @param varianceSwapType The variance swap type
   */
  public void setVarianceSwapType(VarianceSwapType varianceSwapType) {
    _varianceSwapType = varianceSwapType;
  }

  /**
   * Gets the underlying ID.  For floating variance swap legs.
   * @return The underlying ID
   */
  public ExternalIdBean getUnderlyingId() {
    return _underlyingId;
  }

  /**
   * Sets the underlying ID.  For floating variance swap legs.
   * @param underlyingId The underlying ID
   */
  public void setUnderlyingId(ExternalIdBean underlyingId) {
    _underlyingId = underlyingId;
  }

  /**
   * Gets the monitoring frequency.  For floating variance swap legs.
   * @return The monitoring frequency
   */
  public FrequencyBean getMonitoringFrequency() {
    return _monitoringFrequency;
  }

  /**
   * Gets the monitoring frequency.  For floating variance swap legs.
   * @param monitoringFrequency The monitoring frequency
   */
  public void setMonitoringFrequency(FrequencyBean monitoringFrequency) {
    _monitoringFrequency = monitoringFrequency;
  }

  /**
   * Gets the annualization factor.  For floating variance swap legs.
   * @return The annualization factor
   */
  public Double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  /**
   * Sets the annualization factor.  For floating variance swap legs.
   * @param annualizationFactor The annualization factor
   */
  public void setAnnualizationFactor(Double annualizationFactor) {
    _annualizationFactor = annualizationFactor;
  }
  
  /**
   * Gets the inflation leg conventional indexation lag. For inflation swap legs.
   * @return lag The conventional indexation lag.
   */
  public Integer getConventionalIndexationLag() {
    return _conventionalIndexationLag;
  }
  
  /**
   * Gets the conventional indexation lag. For inflation swap legs.
   * @param conventionalIndexationLag The conventional indexation lag.
   */
  public void setConventionalIndexationLag(Integer conventionalIndexationLag) {
    _conventionalIndexationLag = conventionalIndexationLag;
  }
  
  /**
   * Gets the actual indexation lag. For inflation swap legs.
   * @return lag The actual indexation lag.
   */
  public Integer getActualIndexationLag() {
    return _actualIndexationLag;
  }
  
  /**
   * Gets the actual indexation lag. For inflation swap legs.
   * @param actualIndexationLag The actual indexation lag.
   */
  public void setActualIndexationLag(Integer actualIndexationLag) {
    _actualIndexationLag = actualIndexationLag;
  }

  /**
   * Gets the inflation leg interpolation method. For inflation swap legs.
   * @return The inflation leg interpolation method.
   */
  public InterpolationMethod getIndexInterpolationMethod() {
    return _indexInterpolationMethod;
  }
  
  /**
   * Sets the inflation leg interpolation method. For inflation swap legs.
   * @param indexInterpolationMethod The inflation leg interpolation method.
   */
  public void setIndexInterpolationMethod(InterpolationMethod indexInterpolationMethod) {
    _indexInterpolationMethod = indexInterpolationMethod;
  }
}
