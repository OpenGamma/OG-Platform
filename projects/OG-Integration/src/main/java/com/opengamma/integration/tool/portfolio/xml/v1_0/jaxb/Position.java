package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {}) // Indicate we don't care about element ordering
public class Position {

  @XmlAttribute
  @XmlID
  private String _id;

  @XmlElementWrapper(name = "trades")
  @XmlElement(name = "trade")
  @XmlJavaTypeAdapter(TradeRefAdapter.class)
  private List<Trade> _trades;

  @XmlElement(name = "quantity")
  private BigDecimal _quantity;

  @XmlElement(name = "security")
  private IdWrapper _security;

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    _id = id;
  }

  public List<Trade> getTrades() {
    return _trades;
  }

  public void setTrades(List<Trade> trades) {
    _trades = trades;
  }

  public IdWrapper getSecurity() {
    return _security;
  }

  public void setSecurity(IdWrapper security) {
    _security = security;
  }

  public BigDecimal getQuantity() {
    return _quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    _quantity = quantity;
  }
}
