/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;
import java.util.Set;

/**
 * 
 * @author Andrew
 */
public class FutureSecurityBean extends SecurityBean {
  
  private FutureType _futureType;
  private Date _expiry;
  private ExchangeBean _tradingExchange;
  private ExchangeBean _settlementExchange;
  private CurrencyBean _currency1;
  private CurrencyBean _currency2;
  private BondFutureTypeBean _bondType;
  private CommodityFutureTypeBean _commodityType;
  private CashRateTypeBean _cashRateType;
  private UnitBean _unitName;
  private Double _unitNumber;
  private Set<FutureBasketAssociationBean> _basket;
  
  public FutureSecurityBean () {
    super ();
  }
  
  public FutureSecurityBean (final FutureType futureType, final Date expiry, final ExchangeBean tradingExchange, final ExchangeBean settlementExchange) {
    this ();
    _futureType = futureType;
    _expiry = expiry;
    _tradingExchange = tradingExchange;
    _settlementExchange = settlementExchange;
  }
  
  /**
   * @return the future type
   */
  public FutureType getFutureType () {
    return _futureType;
  }

  /**
   * @return the expiry
   */
  public Date getExpiry() {
    return _expiry;
  }

  /**
   * @param expiry the expiry to set
   */
  public void setExpiry(Date expiry) {
    _expiry = expiry;
  }

  /**
   * @return the tradingExchange
   */
  public ExchangeBean getTradingExchange() {
    return _tradingExchange;
  }

  /**
   * @param tradingExchange the tradingExchange to set
   */
  public void setTradingExchange(ExchangeBean tradingExchange) {
    _tradingExchange = tradingExchange;
  }

  /**
   * @return the settlementExchange
   */
  public ExchangeBean getSettlementExchange() {
    return _settlementExchange;
  }

  /**
   * @param settlementExchange the settlementExchange to set
   */
  public void setSettlementExchange(ExchangeBean settlementExchange) {
    _settlementExchange = settlementExchange;
  }

  /**
   * @return the currency1
   */
  public CurrencyBean getCurrency1() {
    return _currency1;
  }

  /**
   * @param currency1 the currency1 to set
   */
  public void setCurrency1(CurrencyBean currency1) {
    _currency1 = currency1;
  }

  /**
   * @return the currency2
   */
  public CurrencyBean getCurrency2() {
    return _currency2;
  }

  /**
   * @param currency2 the currency2 to set
   */
  public void setCurrency2(CurrencyBean currency2) {
    _currency2 = currency2;
  }

  /**
   * @return the commodityType
   */
  public CommodityFutureTypeBean getCommodityType() {
    return _commodityType;
  }

  /**
   * @param commodityType the commodityType to set
   */
  public void setCommodityType(CommodityFutureTypeBean commodityType) {
    _commodityType = commodityType;
  }

  /**
   * @return the cashRateType
   */
  public CashRateTypeBean getCashRateType() {
    return _cashRateType;
  }

  /**
   * @param cashRateType the cashRateType to set
   */
  public void setCashRateType(CashRateTypeBean cashRateType) {
    _cashRateType = cashRateType;
  }

  /**
   * @return the unitName
   */
  public UnitBean getUnitName() {
    return _unitName;
  }

  /**
   * @param unitName the unitName to set
   */
  public void setUnitName(UnitBean unitName) {
    _unitName = unitName;
  }

  /**
   * @return the unitNumber
   */
  public Double getUnitNumber() {
    return _unitNumber;
  }

  /**
   * @param unitNumber the unitNumber to set
   */
  public void setUnitNumber(Double unitNumber) {
    _unitNumber = unitNumber;
  }

  /**
   * @param futureType the futureType to set
   */
  public void setFutureType(FutureType futureType) {
    _futureType = futureType;
  }

  /**
   * @return the bondType
   */
  public BondFutureTypeBean getBondType() {
    return _bondType;
  }

  /**
   * @param bondType the bondType to set
   */
  public void setBondType(BondFutureTypeBean bondType) {
    _bondType = bondType;
  }

  /**
   * @return the basket
   */
  public Set<FutureBasketAssociationBean> getBasket() {
    return _basket;
  }

  /**
   * @param basket the basket to set
   */
  public void setBasket(Set<FutureBasketAssociationBean> basket) {
    _basket = basket;
  }

}