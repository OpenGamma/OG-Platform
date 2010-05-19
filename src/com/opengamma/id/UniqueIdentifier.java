/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * An immutable unique identifier for an item.
 * <p>
 * This identifier is used as a handle within the system to refer to an item uniquely.
 * Many external identifiers, represented by {@link Identifier}, are not truly unique.
 * This identifier is deemed to be unique for at least the duration of a calculation.
 * <p>
 * The identifier is formed from three parts, the scheme, value and version.
 * The scheme defines a single way of identifying items, while the value is an identifier
 * within that scheme. A value from one scheme may refer to a completely different
 * real-world item than the same value from a different scheme.
 * <p>
 * The version allows the object being identifier to change over time.
 * If the version is null then the identifier refers to the latest version of the object.
 * Note that some data providers may not support versioning.
 * <p>
 * Real-world examples of {@code UniqueIdentifier} include instances of:
 * <ul>
 *   <li>Database key - SecurityDB::123456::1</li>
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
   * Fudge message key for the version.
   */
  public static final String VERSION_FUDGE_FIELD_NAME = "Version";

  /**
   * The scheme that categorizes the identifier value.
   */
  private final String _scheme;
  /**
   * The identifier value within the scheme.
   */
  private final String _value;
  /**
   * The version of the identifier, null if latest or not-versioned.
   */
  private final String _version;

  /**
   * Obtains an identifier from a scheme and value indicating the latest version
   * of the identifier, also used for non-versioned identifiers.
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   * @return the identifier, not null
   */
  public static UniqueIdentifier of(String scheme, String value) {
    return new UniqueIdentifier(scheme, value, null);
  }

  /**
   * Obtains an identifier from a scheme and value.
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   * @param version  the version of the identifier, empty treated as null, null treated as latest version
   * @return the identifier, not null
   */
  public static UniqueIdentifier of(String scheme, String value, String version) {
    return new UniqueIdentifier(scheme, value, version);
  }

  /**
   * Obtains an identifier from a formatted scheme and value.
   * <p>
   * This parses the identifier from the form produced by {@code toString()}
   * which is {@code <SCHEME>::<VALUE>::<VERSION>}.
   * @param str  the identifier to parse, not null
   * @return the identifier, not null
   * @throws IllegalArgumentException if the identifier cannot be parsed
   */
  public static UniqueIdentifier parse(String str) {
    ArgumentChecker.notEmpty(str, "version string");
    String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(str, "::");
    switch (split.length) {
      case 2:
        return new UniqueIdentifier(split[0], split[1], null);
      case 3:
        return new UniqueIdentifier(split[0], split[1], split[2]);
    }
    throw new IllegalArgumentException("Invalid identifier format: " + str);
  }

  /**
   * Constructor.
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   * @param version  the version of the identifier, null if latest version
   */
  private UniqueIdentifier(String scheme, String reference, String version) {
    ArgumentChecker.notEmpty(scheme, "scheme");
    ArgumentChecker.notEmpty(reference, "reference");
    _scheme = scheme;
    _value = reference;
    _version = StringUtils.trimToNull(version);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme of the identifier.
   * This is not expected to be the same as {@code IdentificationScheme}.
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

  /**
   * Gets the version of the identifier.
   * @return the version, null if latest version
   */
  public String getVersion() {
    return _version;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return this;
  }

  /**
   * Gets the scheme as an {@code IdentificationScheme}.
   * @return the scheme, not null
   */
  public IdentificationScheme getSchemeObject() {
    // TODO: this is probably an invalid conversion
    return new IdentificationScheme(getScheme());
  }

  /**
   * Checks if this represents the latest version of the item.
   * This simply checks if the version is null.
   * @return true if this is the latest version
   */
  public boolean isLatest() {
    return _version == null;
  }

  /**
   * Checks if this represents a versioned reference to the item.
   * This simply checks if the version is non null.
   * @return true if this is a versioned reference
   */
  public boolean isVersioned() {
    return _version != null;
  }

  /**
   * Returns a unique identifier based on this with the version set to null.
   * The returned identifier will represent the latest version of the item.
   * @return an identifier representing the latest version of the item, not null
   */
  public UniqueIdentifier toLatest() {
    return new UniqueIdentifier(_scheme, _value, null);
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
    if (_value.compareTo(other._value) != 0) {
      return _value.compareTo(other._value);
    }
    return CompareUtils.compareWithNull(_version, other._version);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof UniqueIdentifier) {
      UniqueIdentifier other = (UniqueIdentifier) obj;
      return _scheme.equals(other._scheme) &&
              _value.equals(other._value) &&
              ObjectUtils.equals(_version, other._version);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _scheme.hashCode() ^ _value.hashCode() ^ ObjectUtils.hashCode(_version);
  }

  /**
   * Returns the identifier in the form {@code <SCHEME>::<VALUE>}.
   * @return the identifier, not null
   */
  @Override
  public String toString() {
    StrBuilder buf = new StrBuilder()
      .append(_scheme).append(':').append(':').append(_value);
    if (_version != null) {
      buf.append(':').append(':').append(_version);
    }
    return buf.toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Serializes this pair to a Fudge message.
   * @param factory  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory factory) {
    ArgumentChecker.notNull(factory, "Fudge Context");
    MutableFudgeFieldContainer msg = factory.newMessage();
    msg.add(SCHEME_FUDGE_FIELD_NAME, getScheme());
    msg.add(VALUE_FUDGE_FIELD_NAME, getValue());
    if (getVersion() != null) {
      msg.add(VERSION_FUDGE_FIELD_NAME, getVersion());
    }
    return msg;
  }

  /**
   * Deserializes this pair from a Fudge message.
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static UniqueIdentifier fromFudgeMsg(FudgeFieldContainer msg) {
    String scheme = msg.getString(SCHEME_FUDGE_FIELD_NAME);
    String value = msg.getString(VALUE_FUDGE_FIELD_NAME);
    String version = msg.getString(VERSION_FUDGE_FIELD_NAME);
    return UniqueIdentifier.of(scheme, value, version);
  }

}
