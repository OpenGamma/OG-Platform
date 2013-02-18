package com.opengamma.integration.tool.portfolio.xml.v1_0;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "og-portfolio")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {}) // Indicate we don't care about element ordering
public class PortfolioDocumentV1_0 {

  @XmlAttribute(name = "schemaVersion")
  private String _schemaVersion;

  @XmlElementWrapper(name = "portfolios")
  @XmlElement(name = "portfolio")
  private Set<Portfolio> _portfolios;

  @XmlElementWrapper(name = "positions")
  @XmlElement(name = "position")
  private Set<Position> _positions;

  @XmlElementWrapper(name = "trades")
  // Ensure the trade type is derived from the element name (e.g. trade, swapTrade, ...)
  @XmlElementRef
  private Set<Trade> _trades;

  public String getSchemaVersion() {
    return _schemaVersion;
  }

  public void setSchemaVersion(String schemaVersion) {
    this._schemaVersion = schemaVersion;
  }

  public Set<Trade> getTrades() {
    return _trades;
  }

  public void setTrades(Set<Trade> trades) {
    this._trades = trades;
  }

  public Set<Portfolio> getPortfolios() {
    return _portfolios;
  }

  public void setPortfolios(Set<Portfolio> portfolios) {
    this._portfolios = portfolios;
  }

  public Set<Position> getPositions() {
    return _positions;
  }

  public void setPositions(Set<Position> positions) {
    this._positions = positions;
  }
}
