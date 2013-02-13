package com.opengamma.integration.tool.portfolio.xml.v1_0;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TradeRef {

  @XmlIDREF
  @XmlAttribute(name = "ref")
  private Trade _trade;

  public TradeRef() {
  }

  public TradeRef(Trade trade) {
    _trade = trade;
  }

  public Trade getTrade() {
    return _trade;
  }

  public void setTrade(Trade trade) {
    _trade = trade;
  }
}
