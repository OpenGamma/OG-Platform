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
public class FxOptionTrade extends AbstractFxOptionTrade {

  @XmlElement(name = "notional", required = true)
  private BigDecimal _notional;

  @XmlElement(name = "notionalCurrency", required = true)
  private Currency _notionalCurrency;

  @XmlElement(name = "settlementType")
  private SettlementType _settlementType;

  @XmlElement(name = "settlementCurrency")
  private Currency _settlementCurrency;

  @XmlElement(name = "exerciseType")
  private ExerciseType _exerciseType;

  public BigDecimal getNotional() {
    return _notional;
  }

  public void setNotional(BigDecimal notional) {
    _notional = notional;
  }

  public Currency getNotionalCurrency() {
    return _notionalCurrency;
  }

  public void setNotionalCurrency(Currency notionalCurrency) {
    _notionalCurrency = notionalCurrency;
  }

  public SettlementType getSettlementType() {
    return _settlementType;
  }

  public void setSettlementType(SettlementType settlementType) {
    _settlementType = settlementType;
  }

  public ExerciseType getExerciseType() {
    return _exerciseType;
  }

  public void setExerciseType(ExerciseType exerciseType) {
    _exerciseType = exerciseType;
  }

  public Currency getSettlementCurrency() {
    return _settlementCurrency;
  }

  public void setSettlementCurrency(Currency settlementCurrency) {
    _settlementCurrency = settlementCurrency;
  }
}
