/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExpiryBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of a {@link FXBarrierOptionSecurity}.
 */
public class FXBarrierOptionSecurityBean extends SecurityBean {
  
  private double _putAmount;
  private double _callAmount;
  private ExpiryBean _expiry;
  private CurrencyBean _putCurrency;
  private CurrencyBean _callCurrency;
  private ZonedDateTimeBean _settlementDate;
  private BarrierType _barrierType;
  private BarrierDirection _barrierDirection;
  private MonitoringType _monitoringType;
  private SamplingFrequency _samplingFrequency;
  private double _barrierLevel;


  public FXBarrierOptionSecurityBean() {
    super();
  }
  
  /**
   * Gets the barrierType.
   * @return the barrierType
   */
  public BarrierType getBarrierType() {
    return _barrierType;
  }

  /**
   * Sets the barrierType.
   * @param barrierType  the barrierType
   */
  public void setBarrierType(BarrierType barrierType) {
    _barrierType = barrierType;
  }

  /**
   * Gets the barrierDirection.
   * @return the barrierDirection
   */
  public BarrierDirection getBarrierDirection() {
    return _barrierDirection;
  }

  /**
   * Sets the barrierDirection.
   * @param barrierDirection  the barrierDirection
   */
  public void setBarrierDirection(BarrierDirection barrierDirection) {
    _barrierDirection = barrierDirection;
  }

  /**
   * Gets the monitoringType.
   * @return the monitoringType
   */
  public MonitoringType getMonitoringType() {
    return _monitoringType;
  }

  /**
   * Sets the monitoringType.
   * @param monitoringType  the monitoringType
   */
  public void setMonitoringType(MonitoringType monitoringType) {
    _monitoringType = monitoringType;
  }

  /**
   * Gets the samplingFrequency.
   * @return the samplingFrequency
   */
  public SamplingFrequency getSamplingFrequency() {
    return _samplingFrequency;
  }

  /**
   * Sets the samplingFrequency.
   * @param samplingFrequency  the samplingFrequency
   */
  public void setSamplingFrequency(SamplingFrequency samplingFrequency) {
    _samplingFrequency = samplingFrequency;
  }

  /**
   * @return the expiry
   */
  public ExpiryBean getExpiry() {
    return _expiry;
  }

  /**
   * @param expiry
   *          the expiry to set
   */
  public void setExpiry(ExpiryBean expiry) {
    _expiry = expiry;
  }

  public CurrencyBean getPutCurrency() {
    return _putCurrency;
  }

  public void setPutCurrency(final CurrencyBean currency) {
    _putCurrency = currency;
  }

  public CurrencyBean getCallCurrency() {
    return _callCurrency;
  }

  public void setCallCurrency(final CurrencyBean currency) {
    _callCurrency = currency;
  }

  public ZonedDateTimeBean getSettlementDate() {
    return _settlementDate;
  }
  
  public void setSettlementDate(ZonedDateTimeBean settlementDate) {
    _settlementDate = settlementDate;
  }
  
  /**
   * Gets the putAmount.
   * @return the putAmount
   */
  public double getPutAmount() {
    return _putAmount;
  }

  /**
   * Sets the putAmount.
   * @param putAmount  the putAmount
   */
  public void setPutAmount(double putAmount) {
    _putAmount = putAmount;
  }

  /**
   * Gets the callAmount.
   * @return the callAmount
   */
  public double getCallAmount() {
    return _callAmount;
  }

  /**
   * Sets the callAmount.
   * @param callAmount  the callAmount
   */
  public void setCallAmount(double callAmount) {
    _callAmount = callAmount;
  }
  
  /**
   * Gets the barrierLevel.
   * @return the barrierLevel
   */
  public double getBarrierLevel() {
    return _barrierLevel;
  }

  /**
   * Sets the barrierLevel.
   * @param barrierLevel  the barrierLevel
   */
  public void setBarrierLevel(double barrierLevel) {
    _barrierLevel = barrierLevel;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof FXOptionSecurityBean)) {
      return false;
    }
    final FXBarrierOptionSecurityBean option = (FXBarrierOptionSecurityBean) other;

    return new EqualsBuilder()
      .append(getId(), option.getId())
      .append(getExpiry(), option.getExpiry())
      .append(getPutCurrency(), option.getPutCurrency())
      .append(getCallCurrency(), option.getCallCurrency())
      .append(getCallAmount(), option.getCallAmount())
      .append(getPutAmount(), option.getPutAmount())
      .append(getSettlementDate(), option.getSettlementDate())
      .append(getBarrierType(), option.getBarrierType())
      .append(getBarrierDirection(), option.getBarrierDirection())
      .append(getMonitoringType(), option.getMonitoringType())
      .append(getSamplingFrequency(), option.getSamplingFrequency())
      .append(getBarrierLevel(), option.getBarrierLevel())
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getExpiry())
      .append(getPutCurrency())
      .append(getCallCurrency())
      .append(getSettlementDate())
      .append(getPutAmount())
      .append(getCallAmount())
      .append(getBarrierType())
      .append(getBarrierDirection())
      .append(getMonitoringType())
      .append(getSamplingFrequency())
      .append(getBarrierLevel())
      .toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
