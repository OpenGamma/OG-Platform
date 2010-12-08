/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExchangeBean;
import com.opengamma.masterdb.security.hibernate.ExpiryBean;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of a {@link OptionSecurity}.
 * 
 * @author Andrew
 */
public class OptionSecurityBean extends SecurityBean {

  private OptionExerciseType _optionExerciseType;
  private OptionPayoffStyle _optionPayoffStyle;
  private OptionSecurityType _optionSecurityType;
  private OptionType _optionType;
  private double _strike;
  private ExpiryBean _expiry;
  private CurrencyBean _currency;
  private CurrencyBean _putCurrency;
  private CurrencyBean _callCurrency;
  private ExchangeBean _exchange;
  private String _counterparty;
  private Double _power;
  private Double _cap;
  private Boolean _margined;
  private Double _pointValue;
  private Double _payment;
  private Double _lowerBound;
  private Double _upperBound;
  private ZonedDateTimeBean _chooseDate;
  private Double _underlyingStrike;
  private ExpiryBean _underlyingExpiry;
  private Boolean _reverse;
  private IdentifierBean _underlying;

  public OptionSecurityBean() {
    super();
  }

  public OptionSecurityType getOptionSecurityType() {
    return _optionSecurityType;
  }

  public void setOptionSecurityType(final OptionSecurityType equityOptionType) {
    _optionSecurityType = equityOptionType;
  }

  /**
   * Gets the optionExerciseType field.
   * @return the optionExerciseType
   */
  public OptionExerciseType getOptionExerciseType() {
    return _optionExerciseType;
  }

  /**
   * Sets the optionExerciseType field.
   * @param optionExerciseType  the optionExerciseType
   */
  public void setOptionExerciseType(final OptionExerciseType optionExerciseType) {
    _optionExerciseType = optionExerciseType;
  }

  /**
   * Gets the optionPayoffStyle field.
   * @return the optionPayoffStyle
   */
  public OptionPayoffStyle getOptionPayoffStyle() {
    return _optionPayoffStyle;
  }

  /**
   * Sets the optionPayoffStyle field.
   * @param optionPayoffStyle  the optionPayoffStyle
   */
  public void setOptionPayoffStyle(final OptionPayoffStyle optionPayoffStyle) {
    _optionPayoffStyle = optionPayoffStyle;
  }

  /**
   * @param optionType
   *          the optionType to set
   */
  public void setOptionType(final OptionType optionType) {
    _optionType = optionType;
  }

  /**
   * @return the optionType
   */
  public OptionType getOptionType() {
    return _optionType;
  }

  /**
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * @param strike
   *          the strike to set
   */
  public void setStrike(final double strike) {
    _strike = strike;
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

  /**
   * @return the underlyingIdentifier
   */
  public IdentifierBean getUnderlying() {
    return _underlying;
  }

  /**
   * @param underlying the underlyingIdentifier to set
   */
  public void setUnderlying(final IdentifierBean underlying) {
    _underlying = underlying;
  }

  /**
   * @return the currency
   */
  public CurrencyBean getCurrency() {
    return _currency;
  }

  /**
   * @param currency the currency to set
   */
  public void setCurrency(final CurrencyBean currency) {
    _currency = currency;
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

  /**
   * @return the exchange
   */
  public ExchangeBean getExchange() {
    return _exchange;
  }

  public void setCounterparty(final String counterparty) {
    _counterparty = counterparty;
  }

  public String getCounterparty() {
    return _counterparty;
  }

  /**
   * @param exchange
   *          the exchange to set
   */
  public void setExchange(final ExchangeBean exchange) {
    _exchange = exchange;
  }

  public Double getPower() {
    return _power;
  }

  public void setPower(final Double power) {
    _power = power;
  }

  public Boolean isMargined() {
    return _margined;
  }

  public void setMargined(final Boolean margined) {
    _margined = margined;
  }

  public Double getPointValue() {
    return _pointValue;
  }

  public void setPointValue(final Double pointValue) {
    _pointValue = pointValue;
  }

  public Double getPayment() {
    return _payment;
  }

  public void setPayment(final Double payment) {
    _payment = payment;
  }

  public Double getLowerBound() {
    return _lowerBound;
  }

  public void setLowerBound(final Double lowerBound) {
    _lowerBound = lowerBound;
  }

  public Double getUpperBound() {
    return _upperBound;
  }

  public void setUpperBound(final Double upperBound) {
    _upperBound = upperBound;
  }

  public ZonedDateTimeBean getChooseDate() {
    return _chooseDate;
  }
  
  public void setChooseDate(ZonedDateTimeBean chooseDate) {
    _chooseDate = chooseDate;
  }
  
  public Double getUnderlyingStrike() {
    return _underlyingStrike;
  }
  
  public void setUnderlyingStrike(Double underlyingStrike) {
    _underlyingStrike = underlyingStrike;
  }
  
  public ExpiryBean getUnderlyingExpiry() {
    return _underlyingExpiry;
  }
  
  public void setUnderlyingExpiry(ExpiryBean underlyingExpiry) {
    _underlyingExpiry = underlyingExpiry;
  }
  
  public Boolean isReverse() {
    return _reverse;
  }

  public void setReverse(Boolean reverse) {
    _reverse = reverse;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof OptionSecurityBean)) {
      return false;
    }
    final OptionSecurityBean option = (OptionSecurityBean) other;
    //    if (getId() != -1 && option.getId() != -1) {
    //      return getId().longValue() == option.getId().longValue();
    //    }
    return new EqualsBuilder()
      .append(getId(), option.getId())
      .append(getOptionType(), option.getOptionType())
      .append(getStrike(), option.getStrike())
      .append(getExpiry(), option.getExpiry())
      .append(getUnderlying(), option.getUnderlying())
      .append(getCurrency(), option.getCurrency())
      .append(getPutCurrency(), option.getPutCurrency())
      .append(getCallCurrency(), option.getCallCurrency())
      .append(getExchange(), option.getExchange())
      .append(getCounterparty(), option.getCounterparty())
      .append(getPower(), option.getPower())
      .append(isMargined(), option.isMargined())
      .append(getPointValue(), option.getPointValue())
      .append(getPayment(), option.getPayment())
      .append(getCap(), option.getCap())
      .append(getLowerBound(), option.getLowerBound())
      .append(getUpperBound(), option.getUpperBound())
      .append(getChooseDate(), option.getChooseDate())
      .append(getUnderlyingStrike(), option.getUnderlyingStrike())
      .append(getUnderlyingExpiry(), option.getUnderlyingExpiry())
      .append(isReverse(), option.isReverse())
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getOptionType())
      .append(getStrike())
      .append(getExpiry())
      .append(getUnderlying())
      .append(getCurrency())
      .append(getPutCurrency())
      .append(getCallCurrency())
      .append(getExchange())
      .append(getCounterparty())
      .append(getPower())
      .append(isMargined())
      .append(getPointValue())
      .append(getPayment())
      .append(getCap())
      .append(getLowerBound())
      .append(getUpperBound())
      .append(getChooseDate())
      .append(getUnderlyingStrike())
      .append(getUnderlyingExpiry())
      .append(isReverse())
      .toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * Sets the cap field.
   * @param cap  the cap
   */
  public void setCap(final Double cap) {
    _cap = cap;
  }

  /**
   * Gets the cap field.
   * @return the cap
   */
  public Double getCap() {
    return _cap;
  }

}
