/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * A particular identifier, within a particular domain, which can be used
 * to identify an entity.
 * Real-world examples of {@code DomainSpecificIdentifier} would include:
 * <ul>
 *   <li>Cusip</li>
 *   <li>Isin</li>
 *   <li>RIC code</li>
 *   <li>Bloomberg ID</li>
 *   <li>Bloomberg Ticker</li>
 *   <li>Trading system OTC trade ID</li>
 * </ul>
 *
 * @author kirk
 */
public final class DomainSpecificIdentifier implements Serializable, Cloneable {
  private final IdentificationDomain _domain;
  private final String _value;

  public DomainSpecificIdentifier(IdentificationDomain domain, String value) {
    if(domain == null) {
      throw new NullPointerException("Must provide a valid domain.");
    }
    if(value == null) {
      throw new NullPointerException("Must provide a valid value.");
    }
    _domain = domain;
    _value = value;
  }
  
  /**
   * @return the domain
   */
  public IdentificationDomain getDomain() {
    return _domain;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return _value;
  }

  @Override
  protected DomainSpecificIdentifier clone() {
    try {
      return (DomainSpecificIdentifier) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Cloning is definitely supported.");
    }
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
