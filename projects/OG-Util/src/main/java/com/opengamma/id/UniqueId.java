/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.PublicAPI;

/**
 * An immutable unique identifier for an item within the OpenGamma installation.
 * <p>
 * This identifier is used as a handle within the system to refer to an item uniquely.
 * All versions of the same object share an {@link ObjectId} with the
 * {@code UniqueId} referring to a single version.
 * <p>
 * Many external identifiers, represented by {@link ExternalId}, are not truly unique.
 * This {@code ObjectId} and {@code UniqueId} are unique within the OpenGamma instance.
 * <p>
 * The unique identifier is formed from three parts, the scheme, value and version.
 * The scheme defines a single way of identifying items, while the value is an identifier
 * within that scheme. A value from one scheme may refer to a completely different
 * real-world item than the same value from a different scheme.
 * The version allows the object being identifier to change over time.
 * If the version is null then the identifier refers to the latest version of the object.
 * Note that some data providers may not support versioning.
 * <p>
 * Real-world examples of {@code UniqueId} include instances of:
 * <ul>
 * <li>Database key - DbSec~123456~1</li>
 * <li>In memory key - MemSec~123456~234</li>
 * </ul>
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class UniqueId
    implements Comparable<UniqueId>, UniqueIdentifiable, ObjectIdentifiable, Serializable {

  /**
   * Identification scheme for the unique identifier.
   * This allows a unique identifier to be stored and passed using an {@code ExternalId}.
   */
  public static final ExternalScheme EXTERNAL_SCHEME = ExternalScheme.of("UID");

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

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
   * Obtains a {@code UniqueId} from a scheme and value indicating the latest version
   * of the identifier, also used for non-versioned identifiers.
   * 
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   * @return the unique identifier, not null
   */
  public static UniqueId of(String scheme, String value) {
    return of(scheme, value, null);
  }

  /**
   * Obtains a {@code UniqueId} from a scheme, value and version.
   * 
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   * @param version  the version of the identifier, empty treated as null, null treated as latest version
   * @return the unique identifier, not null
   */
  public static UniqueId of(String scheme, String value, String version) {
    return new UniqueId(scheme, value, version);
  }

  /**
   * Obtains a {@code UniqueId} from an {@code ObjectId} and a version.
   * 
   * @param objectId  the object identifier, not null
   * @param version  the version of the identifier, empty treated as null, null treated as latest version
   * @return the unique identifier, not null
   */
  public static UniqueId of(ObjectId objectId, String version) {
    ArgumentChecker.notNull(objectId, "objectId");
    return new UniqueId(objectId.getScheme(), objectId.getValue(), version);
  }

  /**
   * Obtains a {@code UniqueId} from an external identifier.
   * <p>
   * This allows a unique identifier that was previously packaged as an
   * {@link ExternalId} to be converted back. See {@link #toExternalId()}.
   * In general, this approach should be avoided.
   * 
   * @param externalId  the external identifier key, not null
   * @return the unique identifier, not null
   */
  public static UniqueId of(ExternalId externalId) {
    if (externalId.isNotScheme(EXTERNAL_SCHEME)) {
      throw new IllegalArgumentException("ExternalId is not a valid UniqueId");
    }
    return parse(externalId.getValue());
  }

  /**
   * Parses a {@code UniqueId} from a formatted scheme and value.
   * <p>
   * This parses the identifier from the form produced by {@code toString()}
   * which is {@code <SCHEME>~<VALUE>~<VERSION>}.
   * 
   * @param str  the unique identifier to parse, not null
   * @return the unique identifier, not null
   * @throws IllegalArgumentException if the identifier cannot be parsed
   */
  @FromString
  public static UniqueId parse(String str) {
    ArgumentChecker.notEmpty(str, "str");
    if (str.contains("~") == false) {
      str = StringUtils.replace(str, "::", "~");  // leniently parse old data
    }
    String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(str, "~");
    switch (split.length) {
      case 2:
        return UniqueId.of(split[0], split[1], null);
      case 3:
        return UniqueId.of(split[0], split[1], split[2]);
    }
    throw new IllegalArgumentException("Invalid identifier format: " + str);
  }

  /**
   * Creates a unique instance.
   * 
   * @param scheme  the scheme of the identifier, not empty, not null
   * @param value  the value of the identifier, not empty, not null
   * @param version  the version of the identifier, null if latest version
   */
  private UniqueId(String scheme, String value, String version) {
    ArgumentChecker.notEmpty(scheme, "scheme");
    ArgumentChecker.notEmpty(value, "value");
    _scheme = scheme;
    _value = value;
    _version = StringUtils.trimToNull(version);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme of the identifier.
   * This is the first part of the unique identifier.
   * <p>
   * This is not expected to be the same as {@link ExternalScheme}.
   * 
   * @return the scheme, not empty, not null
   */
  public String getScheme() {
    return _scheme;
  }

  /**
   * Gets the value of the identifier.
   * This is the second part of the unique identifier.
   * 
   * @return the value, not empty, not null
   */
  public String getValue() {
    return _value;
  }

  /**
   * Gets the version of the identifier.
   * This is the third part of the unique identifier.
   * 
   * @return the version, null if latest version
   */
  public String getVersion() {
    return _version;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this identifier with the specified scheme.
   * 
   * @param scheme  the new scheme of the identifier, not empty, not null
   * @return an {@link ObjectId} based on this identifier with the specified scheme, not null
   */
  public UniqueId withScheme(final String scheme) {
    return UniqueId.of(scheme, _value, _version);
  }

  /**
   * Returns a copy of this identifier with the specified value.
   * 
   * @param value  the new value of the identifier, not empty, not null
   * @return an {@link ObjectId} based on this identifier with the specified value, not null
   */
  public UniqueId withValue(final String value) {
    return UniqueId.of(_scheme, value, _version);
  }

  /**
   * Returns a copy of this identifier with the specified version.
   * 
   * @param version  the new version of the identifier, empty treated as null, null treated as latest version
   * @return the created identifier with the specified version, not null
   */
  public UniqueId withVersion(final String version) {
    if (ObjectUtils.equals(version, _version)) {
      return this;
    }
    return new UniqueId(_scheme, _value, version);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the object identifier.
   * <p>
   * All versions of the same object share the same object identifier.
   * 
   * @return the scheme, not empty, not null
   */
  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(_scheme, _value);
  }

  /**
   * Gets the unique identifier.
   * <p>
   * This method trivially returns {@code this}.
   * 
   * @return {@code this}, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return this;
  }

  //-------------------------------------------------------------------------
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
  public UniqueId toLatest() {
    if (isVersioned()) {
      return new UniqueId(_scheme, _value, null);
    } else {
      return this;
    }
  }

  /**
   * Converts this unique identifier to an external identifier.
   * <p>
   * This allows a unique identifier to be packaged and passed around
   * in the form of an external identifier. See {@link #of(ExternalId)}.
   * In general, this approach should be avoided.
   * 
   * @return the external identifier, not null
   */
  public ExternalId toExternalId() {
    return ExternalId.of(EXTERNAL_SCHEME, toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Compares this identifier to another based on the object identifier, ignoring the version.
   * <p>
   * This checks to see if two unique identifiers represent the same underlying object.
   * 
   * @param other  the other identifier, null returns false
   * @return true if the object identifier are equal, ignoring the version
   */
  public boolean equalObjectId(ObjectIdentifiable other) {
    if (other == null) {
      return false;
    }
    ObjectId objectId = other.getObjectId();
    return _scheme.equals(objectId.getScheme()) &&
        _value.equals(objectId.getValue());
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the unique identifiers, sorting alphabetically by scheme followed by value.
   * 
   * @param other  the other unique identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(UniqueId other) {
    int cmp = _scheme.compareTo(other._scheme);
    if (cmp != 0) {
      return cmp;
    }
    cmp = _value.compareTo(other._value);
    if (cmp != 0) {
      return cmp;
    }
    return CompareUtils.compareWithNullLow(_version, other._version);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof UniqueId) {
      UniqueId other = (UniqueId) obj;
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
   * Returns the identifier in the form {@code <SCHEME>~<VALUE>~<VERSION>}.
   * <p>
   * If the version is null, the identifier will omit the colons and version.
   * 
   * @return a parsable representation of the identifier, not null
   */
  @Override
  @ToString
  public String toString() {
    StrBuilder buf = new StrBuilder()
        .append(_scheme).append('~').append(_value);
    if (_version != null) {
      buf.append('~').append(_version);
    }
    return buf.toString();
  }

  //-------------------------------------------------------------------------
  /**
   * This is for more efficient code within the .proto representations of securities, allowing this class
   * to be used directly as a message type instead of through the serialization framework.
   * 
   * @param serializer  the serializer, not null
   * @param msg  the message to populate, not null
   * @deprecated Use builder
   */
  @Deprecated
  public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
    UniqueIdFudgeBuilder.toFudgeMsg(serializer, this, msg);
  }

  /**
   * This is for more efficient code within the .proto representations of securities, allowing this class
   * to be used directly as a message type instead of through the serialization framework.
   * 
   * @param deserializer  the deserializer, not null
   * @param msg  the message to decode, not null
   * @return the created object, not null
   * @deprecated Use builder
   */
  @Deprecated
  public static UniqueId fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return UniqueIdFudgeBuilder.fromFudgeMsg(deserializer, msg);
  }

}
