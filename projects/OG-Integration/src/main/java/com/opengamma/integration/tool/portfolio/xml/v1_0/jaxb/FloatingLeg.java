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

@XmlAccessorType(XmlAccessType.FIELD)
public class FloatingLeg extends SwapLeg {

  public enum ResetType {
    InAdvance, InArrears
  }

  public enum StubPeriodCouponAdjustment {
    LegIndex, AverageOfNearestIndices, ClosestButNotLongerThanLegIndex, ClosestButNotShorterThanLegIndex
  }

  @XmlElement(name = "resetFrequency")
  private String _resetFrequency;

  @XmlElement(name = "compoundingMethod")
  private String _compoundingMethod;

  @XmlElement(name = "resetLag")
  private String _resetLag;

  @XmlElement(name = "resetType")
  private ResetType _resetType;

  @XmlElement(name = "stubPeriodCouponAdjustment")
  private StubPeriodCouponAdjustment _stubPeriodCouponAdjustment;

  @XmlElementWrapper(name = "fixingCalendars")
  @XmlElement(name = "calendar")
  private Set<Calendar> _fixingCalendars;

  private FixingIndex fixingIndex;

  @XmlElement(name = "spread")
  private BigDecimal _spread;

  @XmlElement(name = "gearing")
  private BigDecimal _gearing;

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
    return _resetFrequency;
  }

  public void setResetFrequency(String resetFrequency) {
    this._resetFrequency = resetFrequency;
  }

  public String getCompoundingMethod() {
    return _compoundingMethod;
  }

  public void setCompoundingMethod(String compoundingMethod) {
    this._compoundingMethod = compoundingMethod;
  }

  public String getResetLag() {
    return _resetLag;
  }

  public void setResetLag(String resetLag) {
    this._resetLag = resetLag;
  }

  public ResetType getResetType() {
    return _resetType;
  }

  public void setResetType(ResetType resetType) {
    _resetType = resetType;
  }

  public StubPeriodCouponAdjustment getStubPeriodCouponAdjustment() {
    return _stubPeriodCouponAdjustment;
  }

  public void setStubPeriodCouponAdjustment(StubPeriodCouponAdjustment stubPeriodCouponAdjustment) {
    _stubPeriodCouponAdjustment = stubPeriodCouponAdjustment;
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
    return _spread;
  }

  public void setSpread(BigDecimal spread) {
    this._spread = spread;
  }

  public BigDecimal getGearing() {
    return _gearing;
  }

  public void setGearing(BigDecimal gearing) {
    this._gearing = gearing;
  }
}
