/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import javax.xml.bind.annotation.XmlEnumValue;

import com.opengamma.util.money.Currency;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class SwapLeg {

  public enum Direction {
    @XmlEnumValue(value = "Pay")
    PAY,
    @XmlEnumValue(value = "Receive")
    RECEIVE
  }

  public enum InterestCalculation {
    @XmlEnumValue(value = "Adjusted")
    ADJUSTED
  }

  public enum ScheduleGenerationDirection {
    @XmlEnumValue(value = "Backward")
    BACKWARD
  }

  @XmlElement(name = "payReceive")
  private Direction _direction;

  @XmlElement(name = "currency")
  private Currency _currency;

  @XmlElement(name = "notional")
  private BigDecimal _notional;

  @XmlElement(name = "frequency")
  private String _frequency;

  @XmlElement(name = "dayCount")
  private String _dayCount;

  @XmlElement(name = "interestCalculation")
  private InterestCalculation _interestCalculation;

  @XmlElement(name = "businessDayConvention")
  private String _businessDayConvention;

  @XmlElement(name = "scheduleGenerationDirection")
  private ScheduleGenerationDirection _scheduleGenerationDirection;

  @XmlElement(name = "endOfMonth")
  private boolean _isEndOfMonth;

  @XmlElement(name = "isIMM")
  private boolean _isImm;

  @XmlElementWrapper(name = "paymentCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _paymentCalendars;

  @XmlElement(name = "stubPeriodType")
  private StubPeriodType _stubPeriodType;

  public Direction getDirection() {
    return _direction;
  }

  public void setDirection(Direction direction) {
    this._direction = direction;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public void setCurrency(Currency currency) {
    this._currency = currency;
  }

  public BigDecimal getNotional() {
    return _notional;
  }

  public void setNotional(BigDecimal notional) {
    this._notional = notional;
  }

  public String getFrequency() {
    return _frequency;
  }

  public void setFrequency(String frequency) {
    this._frequency = frequency;
  }

  public String getDayCount() {
    return _dayCount;
  }

  public void setDayCount(String dayCount) {
    this._dayCount = dayCount;
  }

  public InterestCalculation getInterestCalculation() {
    return _interestCalculation;
  }

  public void setInterestCalculation(InterestCalculation interestCalculation) {
    _interestCalculation = interestCalculation;
  }

  public String getBusinessDayConvention() {
    return _businessDayConvention;
  }

  public void setBusinessDayConvention(String businessDayConvention) {
    this._businessDayConvention = businessDayConvention;
  }

  public ScheduleGenerationDirection getScheduleGenerationDirection() {
    return _scheduleGenerationDirection;
  }

  public void setScheduleGenerationDirection(ScheduleGenerationDirection scheduleGenerationDirection) {
    this._scheduleGenerationDirection = scheduleGenerationDirection;
  }

  public boolean isEndOfMonth() {
    return _isEndOfMonth;
  }

  public void setEndOfMonth(boolean endOfMonth) {
    _isEndOfMonth = endOfMonth;
  }

  public boolean isImm() {
    return _isImm;
  }

  public void setImm(boolean imm) {
    _isImm = imm;
  }

  public Set<Calendar> getPaymentCalendars() {
    return _paymentCalendars;
  }

  public void setPaymentCalendars(Set<Calendar> paymentCalendars) {
    this._paymentCalendars = paymentCalendars;
  }

  public StubPeriodType getStubPeriodType() {
    return _stubPeriodType;
  }

  public void setStubPeriodType(StubPeriodType stubPeriodType) {
    _stubPeriodType = stubPeriodType;
  }
}
