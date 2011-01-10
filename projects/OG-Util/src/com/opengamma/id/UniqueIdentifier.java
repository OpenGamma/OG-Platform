/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import com.opengamma.util.PublicAPI;

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
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
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
   * 
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   * @return the identifier, not null
   */
  public static UniqueIdentifier of(String scheme, String value) {
    return new UniqueIdentifier(scheme, value, null);
  }

  /**
   * Obtains an identifier from a scheme and value.
   * 
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
   * 
   * @param uidStr  the identifier to parse, not null
   * @return the identifier, not null
   * @throws IllegalArgumentException if the identifier cannot be parsed
   */
  public static UniqueIdentifier parse(String uidStr) {
    ArgumentChecker.notEmpty(uidStr, "uidStr");
    String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(uidStr, "::");
    switch (split.length) {
      case 2:
        return new UniqueIdentifier(split[0], split[1], null);
      case 3:
        return new UniqueIdentifier(split[0], split[1], split[2]);
    }
    throw new IllegalArgumentException("Invalid identifier format: " + uidStr);
  }

  /**
   * Creates an instance.
   * 
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
   * <p>
   * This is not expected to be the same as {@code IdentificationScheme}.
   * 
   * @return the scheme, not empty, not null
   */
  public String getScheme() {
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

  /**
   * Gets the version of the identifier.
   * 
   * @return the version, null if latest version
   */
  public String getVersion() {
    return _version;
  }

  /**
   * Returns a copy of this identifier with the specified version.
   * 
   * @param version  the new version of the identifier, empty treated as null, null treated as latest version
   * @return the created identifier with the specified version, never null
   */
  public UniqueIdentifier withVersion(final String version) {
    if (ObjectUtils.equals(version, _version)) {
      return this;
    }
    return new UniqueIdentifier(_scheme, _value, version);
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueIdentifier getUniqueId() {
    return this;
  }

  /**
   * Gets the scheme as an {@code IdentificationScheme}.
   * 
   * @return the scheme, not null
   * @deprecated this is an invalid conversion
   */
  @Deprecated
  public IdentificationScheme getSchemeObject() {
    return IdentificationScheme.of(getScheme());
  }

  /**
   * Returns a generic identifier representing the same scheme and value as this unique identifier.
   * 
   * @return the identifier, not null
   * @deprecated this is an invalid conversion
   */
  @Deprecated
  public Identifier toIdentifier() {
    return Identifier.of(getSchemeObject(), getValue());
  }

  /**
   * Checks if this represents the latest version of the item.
   * <p>
   * This simply checks if the version is null.
   * 
   * @return true if this is the latest version
   */
  public boolean isLatest() {
    return _version == null;
  }

  /**
   * Checks if this represents a versioned reference to the item.
   * <p>
   * This simply checks if the version is non null.
   * 
   * @return true if this is a versioned reference
   */
  public boolean isVersioned() {
    return _version != null;
  }

  /**
   * Returns a unique identifier based on this with the version set to null.
   * <p>
   * The returned identifier will represent the latest version of the item.
   * 
   * @return an identifier representing the latest version of the item, not null
   */
  public UniqueIdentifier toLatest() {
    if (isVersioned()) {
      return new UniqueIdentifier(_scheme, _value, null);
    } else {
      return this;
    }
  }

  /**
   * Compares this identifier to another based on the scheme and value, ignoring the version.
   * 
   * @param other  the other identifier, null returns false
   * @return true if equal ignoring the version
   */
  public boolean equalsIgnoringVersion(UniqueIdentifier other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    return _scheme.equals(other._scheme) &&
            _value.equals(other._value);
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the identifiers, sorting alphabetically by scheme followed by value.
   * 
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
   * 
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
   * Serializes this unique identifier to a Fudge message. This is used by the Fudge Serialization Framework and Fudge-Proto generated
   * code to allow unique identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param factory a message creator, not {@code null}
   * @param msg the message to serialize into, not {@code null}
   * @return the serialized message
   */
  public MutableFudgeFieldContainer toFudgeMsg(final FudgeMessageFactory factory, final MutableFudgeFieldContainer msg) {
    ArgumentChecker.notNull(factory, "factory");
    ArgumentChecker.notNull(msg, "msg");
    msg.add(SCHEME_FUDGE_FIELD_NAME, _scheme);
    msg.add(VALUE_FUDGE_FIELD_NAME, _value);
    if (_version != null) {
      msg.add(VERSION_FUDGE_FIELD_NAME, _version);
    }
    return msg;
  }

  /**
   * Serializes this unique identifier to a Fudge message. This is used by the Fudge Serialization Framework and Fudge-Proto generated
   * code to allow unique identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param factory a message creator, not {@code null}
   * @return the serialized Fudge message
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory factory) {
    return toFudgeMsg(factory, factory.newMessage());
  }

  /**
   * Deserializes an unique identifier from a Fudge message. Thsi is used by the Fudge Serialization Framework and Fudge-Proto generated
   * code to allow unique identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param msg the Fudge message, not {@code null}
   * @return the unique identifier
   */
  public static UniqueIdentifier fromFudgeMsg(FudgeFieldContainer msg) {
    String scheme = msg.getString(SCHEME_FUDGE_FIELD_NAME);
    String value = msg.getString(VALUE_FUDGE_FIELD_NAME);
    String version = msg.getString(VERSION_FUDGE_FIELD_NAME);
    return UniqueIdentifier.of(scheme, value, version);
  }

}
