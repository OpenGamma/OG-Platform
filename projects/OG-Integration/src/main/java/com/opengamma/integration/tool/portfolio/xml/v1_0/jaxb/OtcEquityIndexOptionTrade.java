/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.security.option.OptionType;
import com.opengamma.util.money.Currency;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OtcEquityIndexOptionTrade extends Trade {

  @XmlElement(name = "optionType", required = true)
  private OptionType _optionType;

  @XmlElement(name = "buySell", required = true)
  private BuySell _buySell;

  @XmlElement(name = "underlyingId", required = true)
  private IdWrapper _underlyingId;

  @XmlElement(name = "notional", required = true)
  private BigDecimal notional;

  @XmlElement(name = "notionalCurrency", required = true)
  private Currency notionalCurrency;

  @XmlElement(name = "strike", required = true)
  private BigDecimal _strike;

  @XmlElement(name = "exerciseType", required = true)
  private ExerciseType _exerciseType;

  @XmlElement(name = "expiryDate", required = true)
  private LocalDate expiryDate;

  @XmlElementWrapper(name = "expiryCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _expiryCalendars;

  @XmlElementWrapper(name = "settlementCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _settlementCalendars;

  public OptionType getOptionType() {
    return _optionType;
  }

  public void setOptionType(OptionType optionType) {
    _optionType = optionType;
  }

  public BuySell getBuySell() {
    return _buySell;
  }

  public void setBuySell(BuySell buySell) {
    _buySell = buySell;
  }

  public IdWrapper getUnderlyingId() {
    return _underlyingId;
  }

  public void setUnderlyingId(IdWrapper underlyingId) {
    _underlyingId = underlyingId;
  }

  public BigDecimal getNotional() {
    return notional;
  }

  public void setNotional(BigDecimal notional) {
    this.notional = notional;
  }

  public Currency getNotionalCurrency() {
    return notionalCurrency;
  }

  public void setNotionalCurrency(Currency notionalCurrency) {
    this.notionalCurrency = notionalCurrency;
  }

  public BigDecimal getStrike() {
    return _strike;
  }

  public void setStrike(BigDecimal strike) {
    _strike = strike;
  }

  public ExerciseType getExerciseType() {
    return _exerciseType;
  }

  public void setExerciseType(ExerciseType exerciseType) {
    _exerciseType = exerciseType;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
  }

  public Set<Calendar> getExpiryCalendars() {
    return _expiryCalendars;
  }

  public void setExpiryCalendars(Set<Calendar> expiryCalendars) {
    _expiryCalendars = expiryCalendars;
  }

  public Set<Calendar> getSettlementCalendars() {
    return _settlementCalendars;
  }

  public void setSettlementCalendars(Set<Calendar> settlementCalendars) {
    _settlementCalendars = settlementCalendars;
  }

  @Override
  public boolean canBePositionAggregated() {
    return false;
  }
}