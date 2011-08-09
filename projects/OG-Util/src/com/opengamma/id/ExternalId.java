/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An immutable external identifier for an item.
 * <p>
 * This identifier is used as a handle within the system to refer to an externally defined identifier.
 * By contrast, the {@code ObjectId} and {@code UniqueId} represent identifiers within an OpenGamma system.
 * <p>
 * The external identifier is formed from two parts, the scheme and value.
 * The scheme defines a single way of identifying items, while the value is an identifier
 * within that scheme. A value from one scheme may refer to a completely different
 * real-world item than the same value from a different scheme.
 * <p>
 * Real-world examples of {@code ExternalId} include instances of:
 * <ul>
 *   <li>Cusip</li>
 *   <li>Isin</li>
 *   <li>Reuters RIC</li>
 *   <li>Bloomberg BUID</li>
 *   <li>Bloomberg Ticker</li>
 *   <li>Trading system OTC trade ID</li>
 * </ul>
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class ExternalId
    implements ExternalIdentifiable, Comparable<ExternalId>, Serializable {
  static {
    OpenGammaFudgeContext.getInstance().getTypeDictionary().registerClassRename("com.opengamma.id.Identifier", ExternalId.class);
  }

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Fudge message key for the scheme.
   */
  public static final String SCHEME_FUDGE_FIELD_NAME = "Scheme";
  /**
   * Fudge message key for the value.
   */
  public static final String VALUE_FUDGE_FIELD_NAME = "Value";

  /**
   * The scheme that categorizes the identifier value.
   */
  private final ExternalScheme _scheme;
  /**
   * The identifier value within the scheme.
   */
  private final String _value;

  /**
   * Obtains an {@code ExternalId} from a scheme and value.
   * 
   * @param scheme  the scheme of the external identifier, not empty, not null
   * @param value  the value of the external identifier, not empty, not null
   * @return the external identifier, not null
   */
  public static ExternalId of(ExternalScheme scheme, String value) {
    return new ExternalId(scheme, value);
  }

  /**
   * Obtains an {@code ExternalId} from a scheme and value.
   * 
   * @param scheme  the scheme of the external identifier, not empty, not null
   * @param value  the value of the external identifier, not empty, not null
   * @return the external identifier, not null
   */
  public static ExternalId of(String scheme, String value) {
    return new ExternalId(ExternalScheme.of(scheme), value);
  }

  /**
   * Parses an {@code ExternalId} from a formatted scheme and value.
   * <p>
   * This parses the identifier from the form produced by {@code toString()}
   * which is {@code <SCHEME>~<VALUE>}.
   * 
   * @param str  the external identifier to parse, not null
   * @return the external identifier, not null
   * @throws IllegalArgumentException if the identifier cannot be parsed
   */
  public static ExternalId parse(String str) {
    ArgumentChecker.notEmpty(str, "str");
    str = StringUtils.replace(str, "::", "~");  // leniently parse old data
    int pos = str.indexOf("~");
    if (pos < 0) {
      throw new IllegalArgumentException("Invalid identifier format: " + str);
    }
    return new ExternalId(ExternalScheme.of(str.substring(0, pos)), str.substring(pos + 1));
  }

  /**
   * Creates an external identifier.
   * 
   * @param scheme  the scheme, not null
   * @param value  the value of the identifier, not empty, not null
   */
  private ExternalId(ExternalScheme scheme, String value) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notEmpty(value, "standaloneId");
    _scheme = scheme;
    _value = value;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme of the identifier.
   * <p>
   * This provides the universe within which the identifier value has meaning.
   * 
   * @return the scheme, not null
   */
  public ExternalScheme getScheme() {
    return _scheme;
  }

  /**
   * Gets the value of the identifier.
   * 
   * @return the value, not empty, not null
   */
  public String getValue() {
    return _value;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the scheme of this identifier equals the specified scheme.
   * 
   * @param scheme  the scheme to check for, null returns false
   * @return true if the schemes match
   */
  public boolean isScheme(ExternalScheme scheme) {
    return _scheme.equals(scheme);
  }

  /**
   * Checks if the scheme of this identifier equals the specified scheme.
   * 
   * @param scheme  the scheme to check for, null returns false
   * @return true if the schemes match
   */
  public boolean isScheme(String scheme) {
    return _scheme.getName().equals(scheme);
  }

  /**
   * Checks if the scheme of this identifier equals the specified scheme.
   * 
   * @param scheme  the scheme to check for, null returns true
   * @return true if the schemes are different
   */
  public boolean isNotScheme(ExternalScheme scheme) {
    return _scheme.equals(scheme) == false;
  }

  /**
   * Checks if the scheme of this identifier equals the specified scheme.
   * 
   * @param scheme  the scheme to check for, null returns true
   * @return true if the schemes are different
   */
  public boolean isNotScheme(String scheme) {
    return _scheme.getName().equals(scheme) == false;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the external identifier.
   * <p>
   * This method trivially returns {@code this}.
   * 
   * @return {@code this}, not null
   */
  @Override
  public ExternalId getExternalId() {
    return this;
  }

  /**
   * Converts this identifier to a bundle.
   * 
   * @return a bundle wrapping this identifier, not null
   */
  public ExternalIdBundle toBundle() {
    return ExternalIdBundle.of(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the external identifiers, sorting alphabetically by scheme followed by value.
   * 
   * @param other  the other external identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(ExternalId other) {
    int cmp = _scheme.compareTo(other._scheme);
    if (cmp != 0) {
      return cmp;
    }
    return _value.compareTo(other._value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ExternalId) {
      ExternalId other = (ExternalId) obj;
      return _scheme.equals(other._scheme) &&
              _value.equals(other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _scheme.hashCode() ^ _value.hashCode();
  }

  /**
   * Returns the identifier in the form {@code <SCHEME>~<VALUE>}.
   * 
   * @return a parsable representation of the identifier, not null
   */
  @Override
  public String toString() {
    return new StrBuilder().append(_scheme).append('~').append(_value).toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Serializes to a Fudge message.
   * This is used by the Fudge Serialization Framework and Fudge-Proto generated code to allow
   * external identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param factory  a message creator, not null
   * @param msg  the message to serialize into, not null
   * @return the serialized message
   */
  public MutableFudgeMsg toFudgeMsg(final FudgeMsgFactory factory, final MutableFudgeMsg msg) {
    ArgumentChecker.notNull(factory, "factory");
    ArgumentChecker.notNull(msg, "msg");
    msg.add(SCHEME_FUDGE_FIELD_NAME, _scheme.getName());
    msg.add(VALUE_FUDGE_FIELD_NAME, _value);
    return msg;
  }

  /**
   * Serializes to a Fudge message.
   * This is used by the Fudge Serialization Framework and Fudge-Proto generated code to allow
   * identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param factory  a message creator, not null
   * @return the serialized Fudge message, not null
   */
  public FudgeMsg toFudgeMsg(FudgeMsgFactory factory) {
    return toFudgeMsg(factory, factory.newMessage());
  }

  /**
   * Deserializes from a Fudge message.
   * This is used by the Fudge Serialization Framework and Fudge-Proto generated code to allow
   * identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param msg  the Fudge message, not null
   * @return the external identifier, not null
   */
  public static ExternalId fromFudgeMsg(FudgeMsg msg) {
    String scheme = msg.getString(SCHEME_FUDGE_FIELD_NAME);
    String value = msg.getString(VALUE_FUDGE_FIELD_NAME);
    return ExternalId.of(scheme, value);
  }

  /**
   * Deserializes from a Fudge message.
   * This is used by the Fudge Serialization Framework and Fudge-Proto generated code to allow
   * identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the external identifier, not null
   */
  public static ExternalId fromFudgeMsg(FudgeDeserializer fudgeContext, FudgeMsg msg) {
    return fromFudgeMsg(msg);
  }

}
