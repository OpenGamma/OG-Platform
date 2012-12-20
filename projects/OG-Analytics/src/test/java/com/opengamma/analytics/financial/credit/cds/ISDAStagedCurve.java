/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Unmarshal a staged curve for use with an ISDA test grid
 */
@XmlRootElement(name = "curve")
public class ISDAStagedCurve {

  /** The currency the curve is describing */
  @XmlElement(name = "ccy")
  private String _ccy;

  /** The date the curve was snapped */
  @XmlElement(name = "snap")
  private String _snapDate;
  
  /** The curve effective date */
  @XmlElement(name = "effective", required = true)
  private String _effectiveDate;
  
  /** The curve spot date, data points are relative to this date */
  @XmlElement(name = "spot", required = true)
  private String _spotDate;
  
  /** The data points for the curve */
  @XmlElementWrapper(name = "points", required = true)
  @XmlElement(name = "point", required = true)
  private List<Point> _points;
  
  /** Describe an individual data point on the curve */
  @XmlType(name = "point")
  public static class Point {
    
    /** Date */
    @XmlElement(name = "date", required = true)
    private String _date;
    
    /** Rate */
    @XmlElement(name = "rate", required = true)
    private double _rate;
    
    public String getDate() {
      return _date;
    }
    
    public double getRate() {
      return _rate;
    }
  }
  
  public String getCCY() {
    return _ccy;
  }
  
  public String getSnapDate() {
    return _snapDate;
  }
  
  public String getEffectiveDate() {
    return _effectiveDate;
  }
  
  public String getSpotDate() {
    return _spotDate;
  }
  
  public List<Point> getPoints() {
    return _points;
  }
}
