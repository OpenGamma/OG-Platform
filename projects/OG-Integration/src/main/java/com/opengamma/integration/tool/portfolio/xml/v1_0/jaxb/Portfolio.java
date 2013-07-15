/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class Portfolio {

  @XmlAttribute(name = "name")
  private String _name;

  @XmlElementWrapper(name = "positions")
  @XmlElement(name = "position")
  @XmlJavaTypeAdapter(PositionRefAdapter.class)
  private Set<Position> _positions;

  @XmlElementWrapper(name = "trades")
  @XmlElement(name = "trade")
  @XmlJavaTypeAdapter(TradeRefAdapter.class)
  private Set<Trade> _trades;

  @XmlElement(name = "portfolio")
  private Set<Portfolio> _portfolios;

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public Set<Position> getPositions() {
    return _positions;
  }

  public void setPositions(Set<Position> positions) {
    _positions = positions;
  }

  public Set<Trade> getTrades() {
    return _trades;
  }

  public void setTrades(Set<Trade> trades) {
    _trades = trades;
  }

  public Set<Portfolio> getPortfolios() {
    return _portfolios;
  }

  public void setPortfolios(Set<Portfolio> portfolios) {
    _portfolios = portfolios;
  }
}
