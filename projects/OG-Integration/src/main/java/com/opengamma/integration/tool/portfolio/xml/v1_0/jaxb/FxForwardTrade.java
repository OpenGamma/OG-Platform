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

import com.opengamma.util.money.Currency;

@XmlRootElement
public class FxForwardTrade extends Trade {

  @XmlElement(name = "payAmount", required = true)
  private BigDecimal _payAmount;

  @XmlElement(name = "payCurrency", required = true)
  private Currency _payCurrency;

  @XmlElement(name = "receiveAmount", required = true)
  private BigDecimal _receiveAmount;

  @XmlElement(name = "receiveCurrency", required = true)
  private Currency _receiveCurrency;

  @XmlElement(name = "settlementCurrency")
  private Currency _settlementCurrency;

  @XmlElement(name = "fxExpiry")
  private FxExpiry _fxExpiry;

  @XmlElementWrapper(name = "paymentCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _paymentCalendars;

  public BigDecimal getPayAmount() {
    return _payAmount;
  }

  public void setPayAmount(BigDecimal payAmount) {
    _payAmount = payAmount;
  }

  public Currency getPayCurrency() {
    return _payCurrency;
  }

  public void setPayCurrency(Currency payCurrency) {
    _payCurrency = payCurrency;
  }

  public BigDecimal getReceiveAmount() {
    return _receiveAmount;
  }

  public void setReceiveAmount(BigDecimal receiveAmount) {
    _receiveAmount = receiveAmount;
  }

  public Currency getReceiveCurrency() {
    return _receiveCurrency;
  }

  public void setReceiveCurrency(Currency receiveCurrency) {
    _receiveCurrency = receiveCurrency;
  }

  public Set<Calendar> getPaymentCalendars() {
    return _paymentCalendars;
  }

  public void setPaymentCalendars(Set<Calendar> paymentCalendars) {
    _paymentCalendars = paymentCalendars;
  }

  public Currency getSettlementCurrency() {
    return _settlementCurrency;
  }

  public void setSettlementCurrency(Currency settlementCurrency) {
    _settlementCurrency = settlementCurrency;
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
