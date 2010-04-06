/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;

/**
 * A bean representation of a {@link OptionSecurity}.
 * 
 * @author Andrew
 */
public class OptionSecurityBean extends SecurityBean {

  private OptionSecurityType _optionSecurityType;
  private OptionType _optionType;
  private double _strike;
  private Date _expiry;
  private String _underlyingIdentityKey;
  private CurrencyBean _currency1;
  private CurrencyBean _currency2;
  private CurrencyBean _currency3;
  private ExchangeBean _exchange;
  private String _counterparty;
  private double _power;
  private boolean _margined;

  public OptionSecurityBean() {
    super();
  }

  public OptionSecurityType getOptionSecurityType() {
    return _optionSecurityType;
  }

  public void setOptionSecurityType(OptionSecurityType equityOptionType) {
    _optionSecurityType = equityOptionType;
  }

  /**
   * @param optionType
   *          the optionType to set
   */
  public void setOptionType(OptionType optionType) {
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
  public void setStrike(double strike) {
    _strike = strike;
  }

  /**
   * @return the expiry
   */
  public Date getExpiry() {
    return _expiry;
  }

  /**
   * @param expiry
   *          the expiry to set
   */
  public void setExpiry(Date expiry) {
    _expiry = expiry;
  }

  /**
   * @return the underlyingIdentityKey
   */
  public String getUnderlyingIdentityKey() {
    return _underlyingIdentityKey;
  }

  /**
   * @param underlyingIdentityKey
   *          the underlyingIdentityKey to set
   */
  public void setUnderlyingIdentityKey(String underlyingIdentityKey) {
    _underlyingIdentityKey = underlyingIdentityKey;
  }

  /**
   * @return the currency
   */
  public CurrencyBean getCurrency1() {
    return _currency1;
  }

  /**
   * @param currency
   *          the currency to set
   */
  public void setCurrency1(CurrencyBean currency) {
    _currency1 = currency;
  }
  
  public CurrencyBean getCurrency2 () {
    return _currency2;
  }
  
  public void setCurrency2 (final CurrencyBean currency) {
    _currency2 = currency;
  }
  
  public CurrencyBean getCurrency3 () {
    return _currency3;
  }
  
  public void setCurrency3 (final CurrencyBean currency) {
    _currency3 = currency;
  }

  /**
   * @return the exchange
   */
  public ExchangeBean getExchange() {
    return _exchange;
  }
  
  public void setCounterparty (final String counterparty) {
    _counterparty = counterparty;
  }
  
  public String getCounterparty () {
    return _counterparty;
  }

  /**
   * @param exchange
   *          the exchange to set
   */
  public void setExchange(ExchangeBean exchange) {
    _exchange = exchange;
  }
  
  public double getPower () {
    return _power;
  }
  
  public void setPower (final double power) {
    _power = power;
  }
  
  public boolean isMargined () {
    return _margined;
  }
  
  public void setMargined (final boolean margined) {
    _margined = margined;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof OptionSecurityBean)) {
      return false;
    }
    OptionSecurityBean option = (OptionSecurityBean) other;
    if (getId() != -1 && option.getId() != -1) {
      return getId().longValue() == option.getId().longValue();
    }
    return new EqualsBuilder()
        .append(getOptionType(), option.getOptionType())
        .append(getStrike(), option.getStrike())
        .append(getExpiry(), option.getExpiry())
        .append(getUnderlyingIdentityKey(), option.getUnderlyingIdentityKey())
        .append(getCurrency1(), option.getCurrency1())
        .append(getCurrency2(), option.getCurrency2())
        .append(getCurrency3(), option.getCurrency3())
        .append(getExchange(), option.getExchange())
        .append(getCounterparty(), option.getCounterparty())
        .append(getPower(),option.getPower ())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(getOptionType())
        .append(getStrike())
        .append(getExpiry())
        .append(getUnderlyingIdentityKey())
        .append(getCurrency1())
        .append(getCurrency2())
        .append(getCurrency3())
        .append(getExchange())
        .append(getCounterparty())
        .toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
