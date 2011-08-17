/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.equity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.core.security.Security;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.FrequencyBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 */
public class EquityVarianceSwapSecurityBean extends SecurityBean {

  private double _annualizationFactor;
  private CurrencyBean _currency;
  private ZonedDateTimeBean _firstObservationDate;
  private ZonedDateTimeBean _lastObservationDate;
  private double _notional;
  private FrequencyBean _observationFrequency;
  private boolean _parameterisedAsVariance;
  private ExternalIdBean _region;
  private ZonedDateTimeBean _settlementDate;
  private ExternalIdBean _spotUnderlyingIdentifier;
  private double _strike;
 
  /**
   * Gets the annualizationFactor.
   * @return the annualizationFactor
   */
  public double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  /**
   * Sets the annualizationFactor.
   * @param annualizationFactor  the annualizationFactor
   */
  public void setAnnualizationFactor(double annualizationFactor) {
    _annualizationFactor = annualizationFactor;
  }

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
   * Gets the firstObservationDate.
   * @return the firstObservationDate
   */
  public ZonedDateTimeBean getFirstObservationDate() {
    return _firstObservationDate;
  }

  /**
   * Sets the firstObservationDate.
   * @param firstObservationDate  the firstObservationDate
   */
  public void setFirstObservationDate(ZonedDateTimeBean firstObservationDate) {
    _firstObservationDate = firstObservationDate;
  }

  /**
   * Gets the lastObservationDate.
   * @return the lastObservationDate
   */
  public ZonedDateTimeBean getLastObservationDate() {
    return _lastObservationDate;
  }

  /**
   * Sets the lastObservationDate.
   * @param lastObservationDate  the lastObservationDate
   */
  public void setLastObservationDate(ZonedDateTimeBean lastObservationDate) {
    _lastObservationDate = lastObservationDate;
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
   * Gets the observationFrequency.
   * @return the observationFrequency
   */
  public FrequencyBean getObservationFrequency() {
    return _observationFrequency;
  }

  /**
   * Sets the observationFrequency.
   * @param observationFrequency  the observationFrequency
   */
  public void setObservationFrequency(FrequencyBean observationFrequency) {
    _observationFrequency = observationFrequency;
  }

  /**
   * Gets the parameterisedAsVariance.
   * @return the parameterisedAsVariance
   */
  public boolean isParameterisedAsVariance() {
    return _parameterisedAsVariance;
  }

  /**
   * Sets the parameterisedAsVariance.
   * @param parameterisedAsVariance  the parameterisedAsVariance
   */
  public void setParameterisedAsVariance(boolean parameterisedAsVariance) {
    _parameterisedAsVariance = parameterisedAsVariance;
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
   * Gets the settlementDate.
   * @return the settlementDate
   */
  public ZonedDateTimeBean getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Sets the settlementDate.
   * @param settlementDate  the settlementDate
   */
  public void setSettlementDate(ZonedDateTimeBean settlementDate) {
    _settlementDate = settlementDate;
  }

  /**
   * Gets the spotUnderlyingIdentifier.
   * @return the spotUnderlyingIdentifier
   */
  public ExternalIdBean getSpotUnderlyingIdentifier() {
    return _spotUnderlyingIdentifier;
  }

  /**
   * Sets the spotUnderlyingIdentifier.
   * @param spotUnderlyingIdentifier  the spotUnderlyingIdentifier
   */
  public void setSpotUnderlyingIdentifier(ExternalIdBean spotUnderlyingIdentifier) {
    _spotUnderlyingIdentifier = spotUnderlyingIdentifier;
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

  public boolean equals(Object other) {
    if (!(other instanceof EquityVarianceSwapSecurityBean)) {
      return false;
    }
    EquityVarianceSwapSecurityBean swap = (EquityVarianceSwapSecurityBean) other;
    if (getId() != -1 && swap.getId() != -1) {
      return getId().longValue() == swap.getId().longValue();
    }
    return new EqualsBuilder().append(getAnnualizationFactor(), swap.getAnnualizationFactor())
        .append(getCurrency(), swap.getCurrency())
        .append(getFirstObservationDate(), swap.getFirstObservationDate())
        .append(getLastObservationDate(), swap.getLastObservationDate())
        .append(getNotional(), swap.getNotional())
        .append(getObservationFrequency(), swap.getObservationFrequency())
        .append(isParameterisedAsVariance(), swap.isParameterisedAsVariance())
        .append(getRegion(), swap.getRegion())
        .append(getSettlementDate(), swap.getSettlementDate())
        .append(getSpotUnderlyingIdentifier(), swap.getSpotUnderlyingIdentifier())
        .append(getStrike(), swap.getStrike())
        .isEquals();
  }
  
  public int hashCode() {
    return new HashCodeBuilder().append(getAnnualizationFactor())
        .append(getCurrency())
        .append(getFirstObservationDate())
        .append(getLastObservationDate())
        .append(getNotional())
        .append(getObservationFrequency())
        .append(isParameterisedAsVariance())
        .append(getRegion())
        .append(getSettlementDate())
        .append(getSpotUnderlyingIdentifier())
        .append(getStrike())
        .toHashCode();
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
