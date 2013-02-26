/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.security.option.OptionType;

@XmlRootElement
@XmlSeeAlso({FxOptionTrade.class, FxDigitalOptionTrade.class})
public abstract class AbstractFxOptionTrade extends Trade {

  @XmlElement(name = "callPut", required = true)
  private OptionType _callPut;
  @XmlElement(name = "buySell", required = true)
  private BuySell _buySell;
  @XmlElement(name = "currencyPair", required = true)
  private String _currencyPair;
  @XmlElement(name = "optionCurrency", required = true)
  private String _optionCurrency;
  @XmlElement(name = "strike", required = true)
  private BigDecimal _strike;

  @XmlElement(name = "fxExpiry")
  private FxExpiry _fxExpiry;

  @XmlElementWrapper(name = "paymentCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _paymentCalendars;

  public OptionType getOptionType() {
    return _callPut;
  }

  public void setCallPut(OptionType callPut) {
    _callPut = callPut;
  }

  public BuySell getBuySell() {
    return _buySell;
  }

  public void setBuySell(BuySell buySell) {
    _buySell = buySell;
  }

  public String getCurrencyPair() {
    return _currencyPair;
  }

  public void setCurrencyPair(String currencyPair) {
    _currencyPair = currencyPair;
  }

  public String getOptionCurrency() {
    return _optionCurrency;
  }

  public void setOptionCurrency(String optionCurrency) {
    _optionCurrency = optionCurrency;
  }

  public BigDecimal getStrike() {
    return _strike;
  }

  public void setStrike(BigDecimal strike) {
    _strike = strike;
  }

  public Set<Calendar> getPaymentCalendars() {
    return _paymentCalendars;
  }

  public void setPaymentCalendars(Set<Calendar> paymentCalendars) {
    _paymentCalendars = paymentCalendars;
  }

  public FxExpiry getFxExpiry() {
    return _fxExpiry;
  }

  public void setFxExpiry(FxExpiry fxExpiry) {
    _fxExpiry = fxExpiry;
  }

  @Override
  public boolean canBePositionAggregated() {
    return false;
  }
}
