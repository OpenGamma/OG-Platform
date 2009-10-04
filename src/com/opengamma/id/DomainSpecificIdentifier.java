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

import com.opengamma.fudge.FudgeFieldContainer;
import com.opengamma.fudge.FudgeMsg;


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
  public static final String DOMAIN_FUDGE_FIELD_NAME = "Domain";
  public static final String VALUE_FUDGE_FIELD_NAME = "Value";
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
  
  public DomainSpecificIdentifier(FudgeFieldContainer fudgeMsg) {
    String domain = fudgeMsg.getString(DOMAIN_FUDGE_FIELD_NAME);
    String value = fudgeMsg.getString(VALUE_FUDGE_FIELD_NAME);
    if(domain == null) {
      throw new NullPointerException("Message does not contain field " + DOMAIN_FUDGE_FIELD_NAME);
    }
    if(value == null) {
      throw new NullPointerException("Message does not contain field " + VALUE_FUDGE_FIELD_NAME);
    }
    _domain = new IdentificationDomain(domain);
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
  
  public FudgeFieldContainer toFudgeMsg() {
    FudgeMsg msg = new FudgeMsg();
    msg.add(getDomain().getDomainName(), DOMAIN_FUDGE_FIELD_NAME);
    msg.add(getValue(), VALUE_FUDGE_FIELD_NAME);
    return msg;
  }

}
