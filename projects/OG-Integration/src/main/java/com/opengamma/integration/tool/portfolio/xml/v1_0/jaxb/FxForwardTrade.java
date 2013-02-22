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

@XmlRootElement
public class FxForwardTrade extends Trade {

  @XmlElement(name = "payAmount", required = true)
  private BigDecimal _payAmount;

  @XmlElement(name = "payCurrency", required = true)
  private String _payCurrency;

  @XmlElement(name = "receiveAmount", required = true)
  private BigDecimal _receiveAmount;

  @XmlElement(name = "receiveCurrency", required = true)
  private String _receiveCurrency;

  @XmlElement(name = "settlementCurrency")
  private String _settlementCurrency;

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

  public String getPayCurrency() {
    return _payCurrency;
  }

  public void setPayCurrency(String payCurrency) {
    _payCurrency = payCurrency;
  }

  public BigDecimal getReceiveAmount() {
    return _receiveAmount;
  }

  public void setReceiveAmount(BigDecimal receiveAmount) {
    _receiveAmount = receiveAmount;
  }

  public String getReceiveCurrency() {
    return _receiveCurrency;
  }

  public void setReceiveCurrency(String receiveCurrency) {
    _receiveCurrency = receiveCurrency;
  }

  public Set<Calendar> getPaymentCalendars() {
    return _paymentCalendars;
  }

  public void setPaymentCalendars(Set<Calendar> paymentCalendars) {
    _paymentCalendars = paymentCalendars;
  }

  public String getSettlementCurrency() {
    return _settlementCurrency;
  }

  public void setSettlementCurrency(String settlementCurrency) {
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
