/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.threeten.bp.LocalDate;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SwaptionTrade extends Trade {

  @XmlElement(name = "buySell", required = true)
  private BuySell _buySell;

  @XmlElementWrapper(name = "paymentCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _paymentCalendars;

  @XmlElementWrapper(name = "exerciseCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _exerciseCalendars;

  @XmlElement(name = "expirationDate", required = true)
  private LocalDate _expirationDate;

  @XmlElement(name = "exerciseType", required = true)
  private ExerciseType _exerciseType;

/*
 <!--    In case of bermudan we need to specify the dates, so it is similar to a swap:
          Maybe we can turn this "schedule builders" into an object if it makes it easier
          If our data model does not support bermudans, this next block can be ignored-->
      <frequency>12m</frequency>
      <businessDayConvention>Modified Following</businessDayConvention>
      <scheduleGenerationDirection>Backward</scheduleGenerationDirection>
      <generationRule>EOM</generationRule>
*/

  @XmlElement(name = "stubPeriodType")
  private StubPeriodType _stubPeriodType;

  @XmlElement(name = "settlementType")
  private SettlementType _settlementType;

  @XmlElement(name = "cashSettlementCalculationMethod")
  private CashSettlementCalculationMethod _cashSettlementCalculationMethod;

  @XmlElement(name = "cashSettlementPaymentDate")
  private LocalDate _cashSettlementPaymentDate;

  @XmlElement(name = "cashSettlementCurrency")
  private String _cashSettlementCurrency;

  @XmlElement(name = "underlyingSwapTrade")
  private SwapTrade _underlyingSwapTrade;


  public BuySell getBuySell() {
    return _buySell;
  }

  public void setBuySell(BuySell buySell) {
    _buySell = buySell;
  }

  public Set<Calendar> getPaymentCalendars() {
    return _paymentCalendars;
  }

  public void setPaymentCalendars(Set<Calendar> paymentCalendars) {
    _paymentCalendars = paymentCalendars;
  }

  public Set<Calendar> getExerciseCalendars() {
    return _exerciseCalendars;
  }

  public void setExerciseCalendars(Set<Calendar> exerciseCalendars) {
    _exerciseCalendars = exerciseCalendars;
  }

  public LocalDate getExpirationDate() {
    return _expirationDate;
  }

  public void setExpirationDate(LocalDate expirationDate) {
    _expirationDate = expirationDate;
  }

  public ExerciseType getExerciseType() {
    return _exerciseType;
  }

  public void setExerciseType(ExerciseType exerciseType) {
    _exerciseType = exerciseType;
  }

  public StubPeriodType getStubPeriodType() {
    return _stubPeriodType;
  }

  public void setStubPeriodType(StubPeriodType stubPeriodType) {
    _stubPeriodType = stubPeriodType;
  }

  public SettlementType getSettlementType() {
    return _settlementType;
  }

  public void setSettlementType(SettlementType settlementType) {
    _settlementType = settlementType;
  }

  public CashSettlementCalculationMethod getCashSettlementCalculationMethod() {
    return _cashSettlementCalculationMethod;
  }

  public void setCashSettlementCalculationMethod(CashSettlementCalculationMethod cashSettlementCalculationMethod) {
    _cashSettlementCalculationMethod = cashSettlementCalculationMethod;
  }

  public LocalDate getCashSettlementPaymentDate() {
    return _cashSettlementPaymentDate;
  }

  public void setCashSettlementPaymentDate(LocalDate cashSettlementPaymentDate) {
    _cashSettlementPaymentDate = cashSettlementPaymentDate;
  }

  public String getCashSettlementCurrency() {
    return _cashSettlementCurrency;
  }

  public void setCashSettlementCurrency(String cashSettlementCurrency) {
    _cashSettlementCurrency = cashSettlementCurrency;
  }

  public SwapTrade getUnderlyingSwapTrade() {
    return _underlyingSwapTrade;
  }

  public void setUnderlyingSwapTrade(SwapTrade underlyingSwapTrade) {
    _underlyingSwapTrade = underlyingSwapTrade;
  }

  @Override
  public boolean canBePositionAggregated() {
    return false;
  }
}