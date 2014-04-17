/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.joda.beans.JodaBeanUtils;

import com.opengamma.util.money.Currency;

@XmlAccessorType(XmlAccessType.FIELD)

// Note that we cannot use JodaBean for the getters and setters
// as the use of the @XmlValue annotations means this class is not
// allowed to extend another
public class MonetaryAmount {

  @XmlValue
  private BigDecimal _amount;

  @XmlAttribute(name = "currency", required = true)
  private Currency _currency;

  /**
   * Gets the amount.
   * @return the value of the property, not null
   */
  public BigDecimal getAmount() {
    return _amount;
  }

  /**
   * Sets the amount.
   * @param amount  the new value of the property, not null
   */
  public void setAmount(BigDecimal amount) {
    JodaBeanUtils.notNull(amount, "amount");
    this._amount = amount;
  }

  /**
   * Gets the currency.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }
}
