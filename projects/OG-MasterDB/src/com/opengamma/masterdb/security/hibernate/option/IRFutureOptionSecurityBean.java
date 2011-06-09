/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExchangeBean;
import com.opengamma.masterdb.security.hibernate.ExpiryBean;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;

/**
 * A bean representation of a {@link IRFutureOptionSecurity}.
 */
public class IRFutureOptionSecurityBean extends SecurityBean {

  private OptionExerciseType _optionExerciseType;
  private OptionType _optionType;
  private double _strike;
  private ExpiryBean _expiry;
  private CurrencyBean _currency;
  private ExchangeBean _exchange;
  private Boolean _margined;
  private Double _pointValue;
  private IdentifierBean _underlying;

  public IRFutureOptionSecurityBean() {
    super();
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

  /**
   * @return the exchange
   */
  public ExchangeBean getExchange() {
    return _exchange;
  }

  /**
   * @param exchange
   *          the exchange to set
   */
  public void setExchange(final ExchangeBean exchange) {
    _exchange = exchange;
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

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof IRFutureOptionSecurityBean)) {
      return false;
    }
    final IRFutureOptionSecurityBean option = (IRFutureOptionSecurityBean) other;
    return new EqualsBuilder()
      .append(getId(), option.getId())
      .append(getOptionType(), option.getOptionType())
      .append(getStrike(), option.getStrike())
      .append(getExpiry(), option.getExpiry())
      .append(getUnderlying(), option.getUnderlying())
      .append(getCurrency(), option.getCurrency())
      .append(getExchange(), option.getExchange())
      .append(isMargined(), option.isMargined())
      .append(getPointValue(), option.getPointValue())
      .append(getOptionExerciseType(), option.getOptionExerciseType())
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
      .append(getExchange())
      .append(isMargined())
      .append(getPointValue())
      .append(getOptionExerciseType())
      .toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
