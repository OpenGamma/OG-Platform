/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * An immutable unique identifier for an item.
 * <p>
 * This identifier is used as a handle within the system to refer to an item uniquely.
 * Many external identifiers, represented by {@link Identifier}, are not truly unique.
 * This identifier is deemed to be unique for at least the duration of a calculation.
 * <p>
 * The identifier is formed from two parts, the scheme and the value.
 * The scheme defines a single way of identifying items,
 * while the value is an identifier within that scheme.
 * A value from one scheme may refer to a completely different real-world item than
 * the same value from a different scheme.
 * <p>
 * Real-world examples of {@code UniqueIdentifier} include instances of:
 * <ul>
 *   <li>Database key</li>
 * </ul>
 */
public final class UniqueIdentifier implements Comparable<UniqueIdentifier>, Serializable, UniqueIdentifiable {

  /**
   * Fudge message key for the scheme.
   */
  public static final String SCHEME_FUDGE_FIELD_NAME = "Scheme";
  /**
   * Fudge message key for the value.
   */
  public static final String VALUE_FUDGE_FIELD_NAME = "Value";

  /**
   * The scheme that categorizes the value.
   */
  private final String _scheme;
  /**
   * The value (identifier) within the scheme.
   */
  private final String _value;

  /**
   * Obtains an identifier from a scheme and value.
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   * @return the identifier, not null
   */
  public static UniqueIdentifier of(String scheme, String value) {
    return new UniqueIdentifier(scheme, value);
  }

  /**
   * Obtains an identifier from a formatted scheme and value.
   * <p>
   * This parses the identifier from the form produced by {@code toString()}
   * which is {@code <SCHEME>::<VALUE>}.
   * @param str  the identifier to parse, not null
   * @return the identifier, not null
   * @throws IllegalArgumentException if the identifier cannot be parsed
   */
  public static UniqueIdentifier parse(String str) {
    int pos = str.indexOf("::");
    if (pos < 0) {
      throw new IllegalArgumentException("Invalid identifier format: " + str);
    }
    return new UniqueIdentifier(str.substring(0, pos), str.substring(pos + 2));
  }

  /**
   * Constructor.
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   */
  private UniqueIdentifier(String scheme, String reference) {
    ArgumentChecker.notEmpty(scheme, "scheme");
    ArgumentChecker.notEmpty(reference, "reference");
    _scheme = scheme;
    _value = reference;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme of the identifier.
   * @return the scheme, not empty, not null
   */
  public String getScheme() {
    return _scheme;
  }

  /**
   * Gets the value of the identifier.
   * @return the value, not empty, not null
   */
  public String getValue() {
    return _value;
  }
  
  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return this;
  }
  
  public IdentificationScheme getSchemeObject() {
    return new IdentificationScheme(getScheme());
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the identifiers, sorting alphabetically by scheme followed by value.
   * @param other  the other identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(UniqueIdentifier other) {
    if (_scheme.compareTo(other._scheme) != 0) {
      return _scheme.compareTo(other._scheme);
    }
    return _value.compareTo(other._value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof UniqueIdentifier) {
      UniqueIdentifier other = (UniqueIdentifier) obj;
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
   * Returns the identifier in the form {@code <SCHEME>::<VALUE>}.
   * @return the identifier, not null
   */
  @Override
  public String toString() {
    return _scheme + "::" + _value;
  }

  //-------------------------------------------------------------------------
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    ArgumentChecker.notNull(fudgeMessageFactory, "Fudge Context");
    MutableFudgeFieldContainer msg = fudgeMessageFactory.newMessage();
    msg.add(SCHEME_FUDGE_FIELD_NAME, getScheme());
    msg.add(VALUE_FUDGE_FIELD_NAME, getValue());
    return msg;
  }

  public static UniqueIdentifier fromFudgeMsg(FudgeFieldContainer fudgeMsg) {
    String scheme = fudgeMsg.getString(SCHEME_FUDGE_FIELD_NAME);
    String value = fudgeMsg.getString(VALUE_FUDGE_FIELD_NAME);
    return UniqueIdentifier.of(scheme, value);
  }

}
