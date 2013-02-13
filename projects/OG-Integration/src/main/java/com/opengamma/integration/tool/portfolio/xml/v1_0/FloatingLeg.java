package com.opengamma.integration.tool.portfolio.xml.v1_0;

import java.math.BigDecimal;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class FloatingLeg extends SwapLeg {

  public enum ResetType {InAdvance, InArrears}

  private String resetFrequency;

  private String compoundingMethod;

  private String resetLag;

  @XmlElement(name = "resetType")
  private ResetType _resetType;

  @XmlElementWrapper(name = "fixingCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _fixingCalendars;

  private FixingIndex fixingIndex;

  private BigDecimal spread;

  private BigDecimal gearing;

/*  <resetFrequency></resetFrequency>
  <!-- not supported at the moment as frequencies must match -->
  <compoundingMethod></compoundingMethod>
  <!-- we're assuming that resetLag == settlementLag at the moment -->
  <resetLag></resetLag>
  <!-- In Advance or In Arrears.  Only support In advance at the mo -->
  <resetType>InAdvance</resetType>
  <!-- same as payment calendar at the moment -->
  <fixingCalendar></fixingCalendar>
  <fixingIndex>
  <id scheme="BLOOMBERG_TICKER">US0003M Curncy</id>
  <!-- OIS|CMS|IBOR -->
  <rateType>IBOR</rateType>
  </fixingIndex>*/

  public String getResetFrequency() {
    return resetFrequency;
  }

  public void setResetFrequency(String resetFrequency) {
    this.resetFrequency = resetFrequency;
  }

  public String getCompoundingMethod() {
    return compoundingMethod;
  }

  public void setCompoundingMethod(String compoundingMethod) {
    this.compoundingMethod = compoundingMethod;
  }

  public String getResetLag() {
    return resetLag;
  }

  public void setResetLag(String resetLag) {
    this.resetLag = resetLag;
  }

  public ResetType getResetType() {
    return _resetType;
  }

  public void setResetType(ResetType resetType) {
    _resetType = resetType;
  }

  public Set<Calendar> getFixingCalendars() {
    return _fixingCalendars;
  }

  public void setFixingCalendars(Set<Calendar> fixingCalendars) {
    this._fixingCalendars = fixingCalendars;
  }

  public FixingIndex getFixingIndex() {
    return fixingIndex;
  }

  public void setFixingIndex(FixingIndex fixingIndex) {
    this.fixingIndex = fixingIndex;
  }

  public BigDecimal getSpread() {
    return spread;
  }

  public void setSpread(BigDecimal spread) {
    this.spread = spread;
  }

  public BigDecimal getGearing() {
    return gearing;
  }

  public void setGearing(BigDecimal gearing) {
    this.gearing = gearing;
  }
}
