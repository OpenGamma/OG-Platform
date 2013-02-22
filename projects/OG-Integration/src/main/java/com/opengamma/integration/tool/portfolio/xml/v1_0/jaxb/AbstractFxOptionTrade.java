package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.threeten.bp.LocalDate;

@XmlRootElement
@XmlSeeAlso({FxOptionTrade.class, FxDigitalOptionTrade.class})
public abstract class AbstractFxOptionTrade extends Trade {

  @XmlElement(name = "callPut", required = true)
  private CallPut _callPut;
  @XmlElement(name = "buySell", required = true)
  private BuySell _buySell;
  @XmlElement(name = "currencyPair", required = true)
  private String _currencyPair;
  @XmlElement(name = "optionCurrency", required = true)
  private String _optionCurrency;
  @XmlElement(name = "expiryDate")
  private LocalDate _expiryDate;
  @XmlElement(name = "expiryCutoff")
  private String _expiryCutoff;
  @XmlElement(name = "strike", required = true)
  private BigDecimal _strike;
  @XmlElementWrapper(name = "expiryCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _expiryCalendars;

  @XmlElementWrapper(name = "paymentCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _paymentCalendars;

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

  public Set<Calendar> getPaymentCalendars() {
    return _paymentCalendars;
  }

  public void setPaymentCalendars(Set<Calendar> paymentCalendars) {
    _paymentCalendars = paymentCalendars;
  }

  @Override
  public boolean canBePositionAggregated() {
    return false;
  }
}
