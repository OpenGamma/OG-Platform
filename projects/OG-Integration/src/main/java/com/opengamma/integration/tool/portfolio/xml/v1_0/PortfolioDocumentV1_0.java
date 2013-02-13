package com.opengamma.integration.tool.portfolio.xml.v1_0;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "og-portfolio")
@XmlAccessorType(XmlAccessType.FIELD)
public class PortfolioDocumentV1_0 {

  @XmlElementWrapper
  @XmlElement(name = "portfolio")
  private Set<Portfolio> portfolios;

  @XmlElementWrapper
  @XmlElement(name = "position")
  private Set<Position> positions;

  @XmlElementWrapper
  // Ensure the trade type is derived from the element name (e.g. trade, swapTrade, ...)
  @XmlElementRef
  private Set<Trade> trades;

  public Set<Trade> getTrades() {
    return trades;
  }

  public void setTrades(Set<Trade> trades) {
    this.trades = trades;
  }

  public Set<Portfolio> getPortfolios() {
    return portfolios;
  }

  public void setPortfolios(Set<Portfolio> portfolios) {
    this.portfolios = portfolios;
  }

  public Set<Position> getPositions() {
    return positions;
  }

  public void setPositions(Set<Position> positions) {
    this.positions = positions;
  }
}
