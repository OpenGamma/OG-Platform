package com.opengamma.integration.tool.portfolio.xml.v1_0;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.threeten.bp.LocalDate;

@XmlRootElement
public class SwapTrade extends Trade {

  @XmlElement(name = "effectiveDate")
  private LocalDate _effectiveDate;

  private FixedLeg fixedLeg;

  private FloatingLeg floatingLeg;

  public FixedLeg getFixedLeg() {
    return fixedLeg;
  }

  public void setFixedLeg(FixedLeg fixedLeg) {
    this.fixedLeg = fixedLeg;
  }

  public FloatingLeg getFloatingLeg() {
    return floatingLeg;
  }

  public void setFloatingLeg(FloatingLeg floatingLeg) {
    this.floatingLeg = floatingLeg;
  }

  public LocalDate getEffectiveDate() {
    return _effectiveDate;
  }

  public void setEffectiveDate(LocalDate effectiveDate) {
    this._effectiveDate = effectiveDate;
  }
}
