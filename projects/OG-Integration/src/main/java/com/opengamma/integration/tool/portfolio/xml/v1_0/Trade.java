package com.opengamma.integration.tool.portfolio.xml.v1_0;


import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.threeten.bp.LocalDate;

@XmlRootElement
// Ensure we look at subclasses when unmarshalling
@XmlSeeAlso({ FxOptionTrade.class, SwapTrade.class })
@XmlAccessorType(XmlAccessType.FIELD)
public class Trade {

  @XmlAttribute
  @XmlID
  private String _id;

  @XmlElement(name = "externalSystemId", required = true)
  private IdWrapper _externalSystemId;

  @XmlElement(name = "tradeDate")
  private LocalDate _tradeDate;

  @XmlElement(name = "maturityDate")
  private LocalDate _maturityDate;

  @XmlElement(name = "settlementDate")
  private LocalDate _settlementDate;

  @XmlElement(name = "counterparty")
  private IdWrapper _counterparty;

  @XmlElement(name = "premium")
  private BigDecimal _premium;

  @XmlElement(name = "premiumCurrency")
  private String _premiumCurrency;

  public IdWrapper getExternalSystemId() {
    return _externalSystemId;
  }

  public void setExternalSystemId(IdWrapper externalSystemId) {
    this._externalSystemId = externalSystemId;
  }

  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  public void setTradeDate(LocalDate tradeDate) {
    this._tradeDate = tradeDate;
  }

  public LocalDate getMaturityDate() {
    return _maturityDate;
  }

  public void setMaturityDate(LocalDate maturityDate) {
    this._maturityDate = maturityDate;
  }

  public IdWrapper getCounterparty() {
    return _counterparty;
  }

  public void setCounterparty(IdWrapper counterparty) {
    _counterparty = counterparty;
  }

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    _id = id;
  }

  public LocalDate getSettlementDate() {
    return _settlementDate;
  }

  public void setSettlementDate(LocalDate settlementDate) {
    _settlementDate = settlementDate;
  }

  public BigDecimal getPremium() {
    return _premium;
  }

  public void setPremium(BigDecimal premium) {
    _premium = premium;
  }

  public String getPremiumCurrency() {
    return _premiumCurrency;
  }

  public void setPremiumCurrency(String premiumCurrency) {
    _premiumCurrency = premiumCurrency;
  }

  /*


      <!--optional-->
      <isCleared>false</isCleared>
      <!--optional-->
      <clearingBroker>dependency on isCleared? / Id format??</clearingBroker>
      <!--optional-->
      <clearingHouse>dependency on isCleared? / Id format??</clearingHouse>
      <!--optional-->
      <executingBroker>Id format??</executingBroker>
      <!--optional-->
      <primeBroker>Id format??</primeBroker>
      <!--optional-->
      <collateralAgreement>Only if not cleared</collateralAgreement>

      <!-- Additional trade attributes - generally data that won't affect analytics -->
      <tradeAttributes>
        <entry key="STRING">STRING</entry>
      </tradeAttributes>
   */

}
