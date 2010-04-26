/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * A unique identifier within the OpenGamma system.
 * <p>
 * The identifier is formed from two parts.
 * The first is the standalone identifier, which is a simple string.
 * The second is the {@link IdentificationScheme scheme} that provides meaning
 * to the standalone identifier.
 * The standalone identifier is meaningless without the scheme, as the same standalone
 * identifier can refer to two different things in two different schemes.
 * <p>
 * Real-world examples of {@code Identifier} include instances of:
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
public final class Identifier implements Identifiable, Comparable<Identifier>, Cloneable, Serializable {

  /**
   * Fudge message key for the scheme.
   */
  public static final String DOMAIN_FUDGE_FIELD_NAME = "Domain";
  /**
   * Fudge message key for the value.
   */
  public static final String VALUE_FUDGE_FIELD_NAME = "Value";

  /**
   * The scheme that provides meaning to the standalone identifier.
   */
  private final IdentificationScheme _scheme;
  /**
   * The standalone identifier.
   */
  private final String _value;

  /**
   * Constructs an identifier from the scheme and standalone identifier.
   * @param scheme  the scheme, not null
   * @param standaloneId  the standalone identifier, not null
   */
  public Identifier(IdentificationScheme scheme, String standaloneId) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(standaloneId, "standaloneId");
    _scheme = scheme;
    _value = standaloneId;
  }

  /**
   * Constructs an identifier from the scheme and standalone identifier.
   * @param schemeName  the scheme name, not null
   * @param standaloneId  the standalone identifier, not null
   */
  public Identifier(String schemeName, String standaloneId) {
    this(new IdentificationScheme(schemeName), standaloneId);
  }

  /**
   * Constructs an identifier from a Fudge message.
   * @param fudgeMsg  the fudge message, not null
   */
  public Identifier(FudgeFieldContainer fudgeMsg) {
    String domain = fudgeMsg.getString(DOMAIN_FUDGE_FIELD_NAME);
    String value = fudgeMsg.getString(VALUE_FUDGE_FIELD_NAME);
    if (domain == null) {
      throw new NullPointerException("Message does not contain field " + DOMAIN_FUDGE_FIELD_NAME);
    }
    if (value == null) {
      throw new NullPointerException("Message does not contain field " + VALUE_FUDGE_FIELD_NAME);
    }
    _scheme = new IdentificationScheme(domain);
    _value = value;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the identification scheme.
   * This provides the universe within which the standalone identifier has meaning.
   * @return the scheme, never null
   */
  public IdentificationScheme getScheme() {
    return _scheme;
  }

  /**
   * Gets the standalone identifier.
   * @return the value, never null
   */
  public String getValue() {
    return _value;
  }

  @Override
  public Identifier getIdentityKey() {
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Identifier clone() {
    try {
      return (Identifier) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Cloning is definitely supported.");
    }
  }

  @Override
  public int compareTo(Identifier o) {
    if (_scheme.compareTo(o._scheme) != 0) {
      return _scheme.compareTo(o._scheme);
    }
    return _value.compareTo(o._value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Identifier) {
      Identifier other = (Identifier) obj;
      return ObjectUtils.equals(_scheme.getName(), other._scheme.getName()) &&
              ObjectUtils.equals(_value, other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _scheme.getName().hashCode() ^ _value.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    ArgumentChecker.notNull(fudgeMessageFactory, "Fudge Context");
    MutableFudgeFieldContainer msg = fudgeMessageFactory.newMessage();
    msg.add(DOMAIN_FUDGE_FIELD_NAME, getScheme().getName());
    msg.add(VALUE_FUDGE_FIELD_NAME, getValue());
    return msg;
  }

}
