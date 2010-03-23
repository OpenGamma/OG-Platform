/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * An Identification Domain defines a particular domain in which values
 * can be limited. By doing this, it provides a particular universe
 * of keys for an underlying object.
 * Fundamentally, this is nothing other than a typesafe wrapper on top of
 * a name describing the identification domain.
 * <p/>
 * Real-world examples of an Identification Domain might include:
 * <ul>
 *   <li>ISIN, CUSIP for globally unique identifiers on traded securities.</li>
 *   <li>A trading system instance name for trades and positions.</li>
 *   <li>RIC for a reuters-provided market data object.</li>
 * </ul>
 *
 * @author kirk
 */
public class IdentificationDomain implements Serializable, Cloneable {
  public static final IdentificationDomain BLOOMBERG_BUID = new IdentificationDomain("BLOOMBERG_BUID");
  public static final IdentificationDomain BLOOMBERG_TICKER = new IdentificationDomain("BLOOMBERG_TICKER");
  public static final IdentificationDomain CUSIP = new IdentificationDomain("CUSIP");
  public static final IdentificationDomain ISIN = new IdentificationDomain("ISIN");
  public static final IdentificationDomain RIC = new IdentificationDomain("RIC");
  private final String _domainName;
  
  public IdentificationDomain(String domainName) {
    if(domainName == null) {
      throw new NullPointerException("Must name this domain.");
    }
    _domainName = domainName;
  }

  /**
   * @return the domainName
   */
  public String getDomainName() {
    return _domainName;
  }

  @Override
  protected IdentificationDomain clone() {
    try {
      return (IdentificationDomain) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Cloning actually IS supported");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof IdentificationDomain)) {
      return false;
    }
    IdentificationDomain other = (IdentificationDomain) obj;
    if(!ObjectUtils.equals(_domainName, other._domainName)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return _domainName.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
}
