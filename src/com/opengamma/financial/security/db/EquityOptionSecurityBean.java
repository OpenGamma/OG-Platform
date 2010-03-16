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

import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.OptionType;

/**
 * A bean representation of a {@link EquityOptionSecurity}.
 * 
 * @author Andrew
 */
public class EquityOptionSecurityBean extends SecurityBean {

  private EquityOptionType _equityOptionType;
  private OptionType _optionType;
  private double _strike;
  private Date _expiry;
  private String _underlyingIdentityKey;
  private CurrencyBean _currency;
  private ExchangeBean _exchange;
  private double _power;

  public EquityOptionSecurityBean() {
    super();
  }

  public EquityOptionSecurityBean(final EquityOptionType equityOptionType, final OptionType optionType,
      final double strike, final Date expiry,
      final String underlyingIdentityKey, final CurrencyBean currency,
      final ExchangeBean exchange) {
    this();
    _equityOptionType = equityOptionType;
    _optionType = optionType;
    _strike = strike;
    _expiry = expiry;
    _underlyingIdentityKey = underlyingIdentityKey;
    _currency = currency;
    _exchange = exchange;
  }

  public EquityOptionSecurityBean(final EquityOptionType equityOptionType, final OptionType optionType,
      final double strike, final Date expiry,
      final String underlyingIdentityKey, final CurrencyBean currency,
      final ExchangeBean exchange, final double power) {
    this();
    _equityOptionType = equityOptionType;
    _optionType = optionType;
    _strike = strike;
    _expiry = expiry;
    _underlyingIdentityKey = underlyingIdentityKey;
    _currency = currency;
    _exchange = exchange;
    _power = power;
  }

  /**
   * @return the equityOptionType
   */
  public EquityOptionType getEquityOptionType() {
    return _equityOptionType;
  }

  /**
   * @param equityOptionType the equityOptionType to set
   */
  public void setEquityOptionType(EquityOptionType equityOptionType) {
    _equityOptionType = equityOptionType;
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
  public CurrencyBean getCurrency() {
    return _currency;
  }

  /**
   * @param currency
   *          the currency to set
   */
  public void setCurrency(CurrencyBean currency) {
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
  public void setExchange(ExchangeBean exchange) {
    _exchange = exchange;
  }
  
  public double getPower () {
    return _power;
  }
  
  public void setPower (final double power) {
    _power = power;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof EquityOptionSecurityBean)) {
      return false;
    }
    EquityOptionSecurityBean equityOption = (EquityOptionSecurityBean) other;
    if (getId() != -1 && equityOption.getId() != -1) {
      return getId().longValue() == equityOption.getId().longValue();
    }
    return new EqualsBuilder().append(getOptionType(),
        equityOption.getOptionType()).append(getStrike(),
        equityOption.getStrike()).append(getExpiry(), equityOption.getExpiry())
        .append(getUnderlyingIdentityKey(),
            equityOption.getUnderlyingIdentityKey()).append(getCurrency(),
            equityOption.getCurrency()).append(getExchange(),
            equityOption.getExchange()).append(getPower(),equityOption.getPower ()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getOptionType()).append(getStrike())
        .append(getExpiry()).append(getUnderlyingIdentityKey()).append(
            getCurrency()).append(getExchange()).toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
