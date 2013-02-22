package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FxDigitalOptionTrade extends AbstractFxOptionTrade {

  @XmlElement(name = "payout", required = true)
  private BigDecimal _payout;

  @XmlElement(name = "payoutCurrency", required = true)
  private String _payoutCurrency;

  public BigDecimal getPayout() {
    return _payout;
  }

  public void setPayout(BigDecimal payout) {
    _payout = payout;
  }

  public String getPayoutCurrency() {
    return _payoutCurrency;
  }

  public void setPayoutCurrency(String payoutCurrency) {
    _payoutCurrency = payoutCurrency;
  }
}
