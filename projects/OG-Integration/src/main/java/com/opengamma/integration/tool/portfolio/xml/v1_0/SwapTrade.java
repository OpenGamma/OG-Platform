package com.opengamma.integration.tool.portfolio.xml.v1_0;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.threeten.bp.LocalDate;

@XmlRootElement
public class SwapTrade extends Trade {

  @XmlElement(name = "effectiveDate", required = true)
  private LocalDate _effectiveDate;

  @XmlElement(name = "fixedLeg", required = true)
  private FixedLeg _fixedLeg;

  @XmlElement(name = "floatingLeg", required = true)
  private FloatingLeg _floatingLeg;

  public FixedLeg getFixedLeg() {
    return _fixedLeg;
  }

  public void setFixedLeg(FixedLeg fixedLeg) {
    this._fixedLeg = fixedLeg;
  }

  public FloatingLeg getFloatingLeg() {
    return _floatingLeg;
  }

  public void setFloatingLeg(FloatingLeg floatingLeg) {
    this._floatingLeg = floatingLeg;
  }

  public LocalDate getEffectiveDate() {
    return _effectiveDate;
  }

  public void setEffectiveDate(LocalDate effectiveDate) {
    this._effectiveDate = effectiveDate;
  }
}
