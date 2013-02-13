package com.opengamma.integration.tool.portfolio.xml.v1_0;

import java.math.BigDecimal;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.threeten.bp.LocalDate;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FxOptionTrade extends Trade {

  public enum CallPut {Call, Put}

  public enum BuySell {Buy, Sell}

  public enum ExerciseType {European, American}
  public enum SettlementType {Physical, CashSettled}

  @XmlElement(name = "callPut")
  private CallPut _callPut;

  @XmlElement(name = "buySell")
  private BuySell _buySell;

  @XmlElement(name = "currencyPair")
  private String _currencyPair;

  @XmlElement(name = "optionCurrency")
  private String _optionCurrency;

  @XmlElement(name = "notional")
  private BigDecimal _notional;

  @XmlElement(name = "notionalCurrency")
  private String _notionalCurrency;

  @XmlElement(name = "expiryDate")
  private LocalDate _expiryDate;

  @XmlElement(name = "expiryCutoff")
  private String _expiryCutoff;

  @XmlElement(name = "settlementType")
  private SettlementType _settlementType;

  @XmlElement(name = "exerciseType")
  private ExerciseType _exerciseType;

  @XmlElement(name = "strike")
  private BigDecimal _strike;

  @XmlElement(name = "settlementCurrency")
  private String _settlementCurrency;

  @XmlElementWrapper(name = "expiryCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _expiryCalendars;

  @XmlElementWrapper(name = "settlementCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _settlementCalendars;

  public CallPut getCallPut() {
    return _callPut;
  }

  public void setCallPut(CallPut callPut) {
    _callPut = callPut;
  }

  public BuySell getBuySell() {
    return _buySell;
  }

  public void setBuySell(BuySell buySell) {
    _buySell = buySell;
  }

  public String getCurrencyPair() {
    return _currencyPair;
  }

  public void setCurrencyPair(String currencyPair) {
    _currencyPair = currencyPair;
  }

  public String getOptionCurrency() {
    return _optionCurrency;
  }

  public void setOptionCurrency(String optionCurrency) {
    _optionCurrency = optionCurrency;
  }

  public BigDecimal getNotional() {
    return _notional;
  }

  public void setNotional(BigDecimal notional) {
    _notional = notional;
  }

  public String getNotionalCurrency() {
    return _notionalCurrency;
  }

  public void setNotionalCurrency(String notionalCurrency) {
    _notionalCurrency = notionalCurrency;
  }

  public LocalDate getExpiryDate() {
    return _expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    _expiryDate = expiryDate;
  }

  public String getExpiryCutoff() {
    return _expiryCutoff;
  }

  public void setExpiryCutoff(String expiryCutoff) {
    _expiryCutoff = expiryCutoff;
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

  public BigDecimal getStrike() {
    return _strike;
  }

  public void setStrike(BigDecimal strike) {
    _strike = strike;
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

  public String getSettlementCurrency() {
    return _settlementCurrency;
  }

  public void setSettlementCurrency(String settlementCurrency) {
    _settlementCurrency = settlementCurrency;
  }
}
