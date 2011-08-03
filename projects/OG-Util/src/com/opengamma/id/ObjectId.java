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

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An immutable object identifier for an item within the OpenGamma instance.
 * <p>
 * This identifier is used as a handle within the system to refer to an item uniquely over time.
 * All versions of the same object share an object identifier.
 * A {@link UniqueId} refers to a single version of an object identifier.
 * <p>
 * Many external identifiers, represented by {@link Identifier}, are not truly unique.
 * This {@code ObjectId} and {@code UniqueId} are unique within the OpenGamma instance.
 * <p>
 * The object identifier is formed from two parts, the scheme and value.
 * The scheme defines a single way of identifying items, while the value is an identifier
 * within that scheme. A value from one scheme may refer to a completely different
 * real-world item than the same value from a different scheme.
 * <p>
 * Real-world examples of {@code ObjectId} include instances of:
 * <ul>
 * <li>Database key - DbSec~123456</li>
 * <li>In memory key - MemSec~123456</li>
 * </ul>
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class ObjectId
    implements Comparable<ObjectId>, ObjectIdentifiable, Serializable {
  static {
    OpenGammaFudgeContext.getInstance().getTypeDictionary().registerClassRename("com.opengamma.id.ObjectIdentifier", ObjectId.class);
  }

  /**
   * Identification scheme for the OID.
   * This allows a unique identifier to be stored and passed using the weaker {@code Identifier}.
   */
  public static final IdentificationScheme EXTERNAL_SCHEME = IdentificationScheme.of("OID");

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
   * Obtains an identifier from a scheme and value indicating the latest version
   * of the identifier, also used for non-versioned identifiers.
   * 
   * @param scheme  the scheme of the object identifier, not empty, not null
   * @param value  the value of the object identifier, not empty, not null
   * @return the object identifier, not null
   */
  public static ObjectId of(String scheme, String value) {
    return new ObjectId(scheme, value);
  }

  /**
   * Obtains an identifier from a formatted scheme and value.
   * <p>
   * This parses the identifier from the form produced by {@code toString()}
   * which is {@code <SCHEME>~<VALUE>}.
   * 
   * @param objectIdStr  the object identifier to parse, not null
   * @return the object identifier, not null
   * @throws IllegalArgumentException if the identifier cannot be parsed
   */
  public static ObjectId parse(String objectIdStr) {
    ArgumentChecker.notEmpty(objectIdStr, "objectIdStr");
    objectIdStr = StringUtils.replace(objectIdStr, "::", "~");  // leniently parse old data
    String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(objectIdStr, "~");
    switch (split.length) {
      case 2:
        return ObjectId.of(split[0], split[1]);
    }
    throw new IllegalArgumentException("Invalid identifier format: " + objectIdStr);
  }

  /**
   * Creates an object identifier.
   * 
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   */
  private ObjectId(String scheme, String reference) {
    ArgumentChecker.notEmpty(scheme, "scheme");
    ArgumentChecker.notEmpty(reference, "reference");
    _scheme = scheme;
    _value = reference;
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

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this identifier with the specified scheme.
   * 
   * @param scheme  the new scheme of the identifier, not empty, not null
   * @return an {@link ObjectId} based on this identifier with the specified scheme, not null
   */
  public ObjectId withScheme(final String scheme) {
    return ObjectId.of(scheme, _value);
  }

  /**
   * Returns a copy of this identifier with the specified value.
   * 
   * @param value  the new value of the identifier, not empty, not null
   * @return an {@link ObjectId} based on this identifier with the specified value, not null
   */
  public ObjectId withValue(final String value) {
    return ObjectId.of(_scheme, value);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a unique identifier with the latest version.
   * <p>
   * This creates a new unique identifier based on this object identifier marked
   * to retrieve the latest version.
   * 
   * @return a {@link UniqueId} based on this identifier at the latest version, not null
   */
  public UniqueId atLatestVersion() {
    return UniqueId.of(_scheme, _value, null);
  }

  /**
   * Creates a unique identifier with the specified version.
   * <p>
   * This creates a new unique identifier based on this object identifier using
   * the specified version.
   * 
   * @param version  the new version of the identifier, empty treated as null, null treated as latest version
   * @return a {@link UniqueId} based on this identifier at the specified version, not null
   */
  public UniqueId atVersion(final String version) {
    return UniqueId.of(_scheme, _value, version);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the object identifier.
   * <p>
   * This method trivially returns {@code this}.
   * 
   * @return {@code this}, not null
   */
  @Override
  public ObjectId getObjectId() {
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the identifiers, sorting alphabetically by scheme followed by value.
   * 
   * @param other  the other identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(ObjectId other) {
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
    if (obj instanceof ObjectId) {
      ObjectId other = (ObjectId) obj;
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
   * @return the identifier, not null
   */
  @Override
  public String toString() {
    return new StrBuilder().append(_scheme).append('~').append(_value).toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Serializes this object identifier to a Fudge message.
   * This is used by the Fudge Serialization Framework and Fudge-Proto generated code to allow
   * object identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param factory  a message creator, not null
   * @param msg  the message to serialize into, not null
   * @return the serialized message
   */
  public MutableFudgeMsg toFudgeMsg(final FudgeMsgFactory factory, final MutableFudgeMsg msg) {
    ArgumentChecker.notNull(factory, "factory");
    ArgumentChecker.notNull(msg, "msg");
    msg.add(SCHEME_FUDGE_FIELD_NAME, _scheme);
    msg.add(VALUE_FUDGE_FIELD_NAME, _value);
    return msg;
  }

  /**
   * Serializes this object identifier to a Fudge message.
   * This is used by the Fudge Serialization Framework and Fudge-Proto generated code to allow
   * object identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param factory  a message creator, not null
   * @return the serialized Fudge message, not null
   */
  public FudgeMsg toFudgeMsg(FudgeMsgFactory factory) {
    return toFudgeMsg(factory, factory.newMessage());
  }

  /**
   * Deserializes an object identifier from a Fudge message.
   * This is used by the Fudge Serialization Framework and Fudge-Proto generated code to allow
   * object identifiers to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param msg  the Fudge message, not null
   * @return the object identifier, not null
   */
  public static ObjectId fromFudgeMsg(FudgeMsg msg) {
    String scheme = msg.getString(SCHEME_FUDGE_FIELD_NAME);
    String value = msg.getString(VALUE_FUDGE_FIELD_NAME);
    return ObjectId.of(scheme, value);
  }

}
