package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FxOptionTrade extends AbstractFxOptionTrade {

  public enum ExerciseType {European, American}

  public enum SettlementType {Physical, CashSettled}

  @XmlElement(name = "notional", required = true)
  private BigDecimal _notional;

  @XmlElement(name = "notionalCurrency", required = true)
  private String _notionalCurrency;

  @XmlElement(name = "settlementType")
  private SettlementType _settlementType;

  @XmlElement(name = "settlementCurrency")
  private String _settlementCurrency;

  @XmlElement(name = "exerciseType")
  private ExerciseType _exerciseType;

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

  public String getSettlementCurrency() {
    return _settlementCurrency;
  }

  public void setSettlementCurrency(String settlementCurrency) {
    _settlementCurrency = settlementCurrency;
  }
}
