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

import org.threeten.bp.LocalDate;

import com.opengamma.util.money.Currency;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EquityVarianceSwapTrade extends Trade {

  @XmlElement(name = "buySell", required = true)
  private BuySell _buySell;

  @XmlElement(name = "currency", required = true)
  private Currency _currency;

  @XmlElement(name = "strike", required = true)
  private BigDecimal _strike;

  @XmlElement(name = "underlying", required = true)
  private IdWrapper _underlying;

  @XmlElement(name = "vegaAmount", required = true)
  private BigDecimal _vegaAmount;

  @XmlElement(name = "observationStartDate", required = true)
  private LocalDate _observationStartDate;

  @XmlElement(name = "observationEndDate", required = true)
  private LocalDate _observationEndDate;

  @XmlElement(name = "observationfrequency")
  private String _observationfrequency;

  @XmlElement(name = "annualizationFactor")
  private double _annualizationFactor;

  public BuySell getBuySell() {
    return _buySell;
  }

  public void setBuySell(BuySell buySell) {
    _buySell = buySell;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public void setCurrency(Currency currency) {
    _currency = currency;
  }

  public BigDecimal getStrike() {
    return _strike;
  }

  public void setStrike(BigDecimal strike) {
    _strike = strike;
  }

  public IdWrapper getUnderlying() {
    return _underlying;
  }

  public void setUnderlying(IdWrapper underlying) {
    _underlying = underlying;
  }

  public BigDecimal getVegaAmount() {
    return _vegaAmount;
  }

  public void setVegaAmount(BigDecimal vegaAmount) {
    _vegaAmount = vegaAmount;
  }

  public LocalDate getObservationStartDate() {
    return _observationStartDate;
  }

  public void setObservationStartDate(LocalDate observationStartDate) {
    _observationStartDate = observationStartDate;
  }

  public LocalDate getObservationEndDate() {
    return _observationEndDate;
  }

  public void setObservationEndDate(LocalDate observationEndDate) {
    _observationEndDate = observationEndDate;
  }

  public String getObservationfrequency() {
    return _observationfrequency;
  }

  public void setObservationfrequency(String observationfrequency) {
    _observationfrequency = observationfrequency;
  }

  public double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  public void setAnnualizationFactor(double annualizationFactor) {
    _annualizationFactor = annualizationFactor;
  }

  @Override
  public boolean canBePositionAggregated() {
    return false;
  }
}
