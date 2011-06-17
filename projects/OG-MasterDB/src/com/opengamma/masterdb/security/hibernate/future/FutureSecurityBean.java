/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExchangeBean;
import com.opengamma.masterdb.security.hibernate.ExpiryBean;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * Hibernate bean for storage.
 */
public class FutureSecurityBean extends SecurityBean {

  private FutureType _futureType; // TODO 2011-06-01 Case - Describe FutureSecurityBean members.
  private ExpiryBean _expiry;
  private ExchangeBean _tradingExchange;
  private ExchangeBean _settlementExchange;
  private CurrencyBean _currency1;
  private CurrencyBean _currency2;
  private CurrencyBean _currency3;
  private BondFutureTypeBean _bondType;
  private CommodityFutureTypeBean _commodityType;
  private Double _unitAmount;
  private UnitBean _unitName;
  private Double _unitNumber;
  private IdentifierBean _underlying;
  private Set<FutureBundleBean> _basket;
  private ZonedDateTimeBean _firstDeliveryDate;
  private ZonedDateTimeBean _lastDeliveryDate;

  public FutureSecurityBean() {
    super();
  }

  /**
   * @return the future type
   */
  public FutureType getFutureType() {
    return _futureType;
  }

  /**
   * @return the expiry
   */
  public ExpiryBean getExpiry() {
    return _expiry;
  }

  /**
   * @param expiry the expiry to set
   */
  public void setExpiry(ExpiryBean expiry) {
    _expiry = expiry;
  }

  /**
   * @return the first delivery date (bond futures only)
   */
  public ZonedDateTimeBean getFirstDeliveryDate() {
    return _firstDeliveryDate;
  }

  /**
   * @param firstDeliveryDate the first delivery date (bond futures only)
   */
  public void setFirstDeliveryDate(ZonedDateTimeBean firstDeliveryDate) {
    _firstDeliveryDate = firstDeliveryDate;
  }

  /**
   * @return the last delivery date (bond futures only)
   */
  public ZonedDateTimeBean getLastDeliveryDate() {
    return _lastDeliveryDate;
  }

  /**
   * @param lastDeliveryDate the last delivery date (bond futures only)
   */
  public void setLastDeliveryDate(ZonedDateTimeBean lastDeliveryDate) {
    _lastDeliveryDate = lastDeliveryDate;
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
   * @param unitAmount the unitAmount to set
   */
  public void setUnitAmount(Double unitAmount) {
    _unitAmount = unitAmount;
  }

  /**
   * @return the unitAmount
   */
  public Double getUnitAmount() {
    return _unitAmount;
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
  public Set<FutureBundleBean> getBasket() {
    return _basket;
  }

  /**
   * @param basket the basket to set
   */
  public void setBasket(Set<FutureBundleBean> basket) {
    _basket = basket;
  }

  public IdentifierBean getUnderlying() {
    return _underlying;
  }

  public void setUnderlying(final IdentifierBean underlying) {
    _underlying = underlying;
  }

  /**
   * @param currency the currency to set
   */
  public void setCurrency3(CurrencyBean currency) {
    _currency3 = currency;
  }

  /**
   * @return the currency3
   */
  public CurrencyBean getCurrency3() {
    return _currency3;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
