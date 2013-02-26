/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.opengamma.util.money.Currency;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FxDigitalOptionTrade extends AbstractFxOptionTrade {

  @XmlElement(name = "payout", required = true)
  private BigDecimal _payout;

  @XmlElement(name = "payoutCurrency", required = true)
  private Currency _payoutCurrency;

  public BigDecimal getPayout() {
    return _payout;
  }

  public void setPayout(BigDecimal payout) {
    _payout = payout;
  }

  public Currency getPayoutCurrency() {
    return _payoutCurrency;
  }

  public void setPayoutCurrency(Currency payoutCurrency) {
    _payoutCurrency = payoutCurrency;
  }
}
