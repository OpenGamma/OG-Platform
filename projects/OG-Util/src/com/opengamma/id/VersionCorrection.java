/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import javax.time.CalendricalException;
import javax.time.Instant;
import javax.time.InstantProvider;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.base.Objects;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.time.DateUtils;

/**
 * An immutable version-correction combination.
 * <p>
 * History can be stored in two dimensions and the version-correction provides the key.
 * The first historic dimension is the classic series of versions.
 * Each new version is stored in such a manor that previous versions can be accessed.
 * The second historic dimension is corrections.
 * A correction occurs when it is realized that the original data stored was incorrect.
 * <p>
 * A fully versioned object in an OpenGamma installation will have a single state for
 * any combination of version and correction. This state is assigned a version string
 * which is used as the third component in a {@link UniqueId}, where all versions
 * share the same {@link ObjectId}.
 * <p>
 * This class represents a single version-correction combination suitable for identifying
 * a single state. It is typically used to obtain an object, while the version string is
 * used in the response.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class VersionCorrection implements Comparable<VersionCorrection>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Version-correction instance representing the latest version and correction.
   */
  public static final VersionCorrection LATEST = new VersionCorrection(null, null);

  /**
   * The version instant.
   */
  private final Instant _versionAsOf;
  /**
   * The correction instant.
   */
  private final Instant _correctedTo;

  /**
   * Obtains a {@code VersionCorrection} from another version-correction,
   * defaulting the LATEST constant for null.
   * 
   * @param versionCorrection  the version-correction to check, null for latest
   * @return the version-correction combination, not null
   */
  public static VersionCorrection of(VersionCorrection versionCorrection) {
    return Objects.firstNonNull(versionCorrection, VersionCorrection.LATEST);
  }

  /**
   * Obtains a {@code VersionCorrection} from a version and correction instant.
   * 
   * @param versionAsOf  the version as of instant, null for latest
   * @param correctedTo  the corrected to instant, null for latest
   * @return the version-correction combination, not null
   */
  public static VersionCorrection of(InstantProvider versionAsOf, InstantProvider correctedTo) {
    if (versionAsOf == null && correctedTo == null) {
      return LATEST;
    }
    Instant v = (versionAsOf != null ? Instant.of(versionAsOf) : null);
    Instant c = (correctedTo != null ? Instant.of(correctedTo) : null);
    return new VersionCorrection(v, c);
  }

  /**
   * Obtains a {@code VersionCorrection} from a version instant and the latest
   * correction.
   * 
   * @param versionAsOf  the version as of instant, null for latest
   * @return the version-correction combination, not null
   */
  public static VersionCorrection ofVersionAsOf(InstantProvider versionAsOf) {
    return of(versionAsOf, null);
  }

  /**
   * Obtains a {@code VersionCorrection} from a correction instant and the latest
   * version.
   * 
   * @param correctedTo  the corrected to instant, null for latest
   * @return the version-correction combination, not null
   */
  public static VersionCorrection ofCorrectedTo(InstantProvider correctedTo) {
    return of(null, correctedTo);
  }

  /**
   * Parses a {@code VersionCorrection} from the standard string format.
   * <p>
   * This parses the version-correction from the form produced by {@code toString()}.
   * It consists of 'V' followed by the version, a dot, then 'C' followed by the correction,
   * such as {@code V2011-02-01T12:30:40Z.C2011-02-01T12:30:40Z}.
   * The text 'LATEST' is used in place of the instant for a latest version or correction.
   * 
   * @param str  the identifier to parse, not null
   * @return the version-correction combination, not null
   * @throws IllegalArgumentException if the version-correction cannot be parsed
   */
  public static VersionCorrection parse(String str) {
    ArgumentChecker.notEmpty(str, "str");
    int posC = str.indexOf(".C");
    if (str.charAt(0) != 'V' || posC < 0) {
      throw new IllegalArgumentException("Invalid identifier format: " + str);
    }
    String verStr = str.substring(1, posC);
    String corrStr = str.substring(posC + 2);
    Instant versionAsOf = parseInstantString(verStr);
    Instant correctedTo = parseInstantString(corrStr);
    return of(versionAsOf, correctedTo);
  }

  /**
   * Parses a {@code VersionCorrection} from standard string representations of the
   * version and correction.
   * <p>
   * This parses the version-correction from the forms produced by
   * {@link #getVersionAsOfString()} and {@link #getCorrectedToString()}.
   * 
   * @param versionAsOfString  the version as of string, null treated as latest
   * @param correctedToString  the corrected to string, null treated as latest
   * @return the version-correction combination, not null
   * @throws IllegalArgumentException if the version-correction cannot be parsed
   */
  public static VersionCorrection parse(String versionAsOfString, String correctedToString) {
    Instant versionAsOf = parseInstantString(versionAsOfString);
    Instant correctedTo = parseInstantString(correctedToString);
    return of(versionAsOf, correctedTo);
  }

  /**
   * Parses a version-correction {@code Instant} from a standard string representation.
   * <p>
   * The string representation must be either {@code LATEST} for null, or the ISO-8601
   * representation of the desired {@code Instant}.
   * 
   * @param instantStr  the instant string, null treated as latest
   * @return the instant, not null
   */
  private static Instant parseInstantString(String instantStr) {
    if (instantStr == null || instantStr.equals("LATEST")) {
      return null;
    } else {
      try {
        return Instant.parse(instantStr);
      } catch (CalendricalException ex) {
        throw new IllegalArgumentException(ex);
      }
    }
  }

  /**
   * Creates a version-correction combination.
   * 
   * @param versionAsOf  the version as of instant, null for latest
   * @param correctedTo  the corrected to instant, null for latest
   */
  private VersionCorrection(Instant versionAsOf, Instant correctedTo) {
    _versionAsOf = versionAsOf;
    _correctedTo = correctedTo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the version as of instant.
   * <p>
   * This will locate the version that was active at this instant.
   * 
   * @return the version as of instant, null for latest
   */
  public Instant getVersionAsOf() {
    return _versionAsOf;
  }

  /**
   * Gets the corrected to instant.
   * <p>
   * This will locate the correction that was active at this instant.
   * 
   * @return the corrected to instant, null for latest
   */
  public Instant getCorrectedTo() {
    return _correctedTo;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this object with the specified version as of instant.
   * <p>
   * This instance is immutable and unaffected by this method call.
   * 
   * @param versionAsOf  the version instant, null for latest
   * @return a version-correction based on this one with the version as of instant altered, not null
   */
  public VersionCorrection withVersionAsOf(InstantProvider versionAsOf) {
    Instant v = (versionAsOf != null ? Instant.of(versionAsOf) : null);
    if (ObjectUtils.equals(_versionAsOf, v)) {
      return this;
    }
    return new VersionCorrection(v, _correctedTo);
  }

  /**
   * Returns a copy of this object with the specified corrected to instant.
   * <p>
   * This instance is immutable and unaffected by this method call.
   * 
   * @param correctedTo  the corrected to instant, null for latest
   * @return a version-correction based on this one with the corrected to instant altered, not null
   */
  public VersionCorrection withCorrectedTo(InstantProvider correctedTo) {
    Instant c = (correctedTo != null ? Instant.of(correctedTo) : null);
    if (ObjectUtils.equals(_correctedTo, c)) {
      return this;
    }
    return new VersionCorrection(_versionAsOf, c);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks whether this object has either the version or correction instant
   * set to 'latest'.
   * 
   * @return true if either instant is 'latest'
   */
  public boolean containsLatest() {
    return (_versionAsOf == null || _correctedTo == null);
  }

  /**
   * Returns a copy of this object with any latest instant fixed to the specified instant.
   * <p>
   * This instance is immutable and unaffected by this method call.
   * 
   * @param now  the current instant, not null
   * @return a version-correction based on this one with the correction altered, not null
   */
  public VersionCorrection withLatestFixed(Instant now) {
    ArgumentChecker.notNull(now, "Now must not be null");
    if (containsLatest()) {
      return new VersionCorrection(Objects.firstNonNull(_versionAsOf, now), Objects.firstNonNull(_correctedTo, now));
    }
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a string representation of the version as of instant.
   * <p>
   * This is either the ISO-8601 representation of the version as of instant, such as
   * {@code 2011-02-01T12:30:40Z}, or {@code LATEST} for null.
   * 
   * @return the string version as of, not null
   */
  public String getVersionAsOfString() {
    return ObjectUtils.defaultIfNull(_versionAsOf, "LATEST").toString();
  }

  /**
   * Gets a string representation of the corrected to instant.
   * <p>
   * This is either the ISO-8601 representation of the corrected to instant, such as
   * {@code 2011-02-01T12:30:40Z}, or {@code LATEST} for null.
   * 
   * @return the string corrected to, not null
   */
  public String getCorrectedToString() {
    return ObjectUtils.defaultIfNull(_correctedTo, "LATEST").toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the version-corrections, sorting by version followed by correction.
   * 
   * @param other  the other identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(VersionCorrection other) {
    int cmp = CompareUtils.compareWithNullHigh(_versionAsOf, other._versionAsOf);
    if (cmp != 0) {
      return cmp;
    }
    return CompareUtils.compareWithNullHigh(_correctedTo, other._correctedTo);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof VersionCorrection) {
      VersionCorrection other = (VersionCorrection) obj;
      return Objects.equal(_versionAsOf, other._versionAsOf) &&
          Objects.equal(_correctedTo, other._correctedTo);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_versionAsOf, _correctedTo);
  }

  /**
   * Returns the version-correction instants.
   * <p>
   * This is a standard format that can be parsed.
   * It consists of 'V' followed by the version, a dot, then 'C' followed by the correction,
   * such as {@code V2011-02-01T12:30:40Z.C2011-02-01T12:30:40Z}.
   * The text 'LATEST' is used in place of the instant for a latest version or correction.
   * 
   * @return the string version-correction, not null
   */
  @Override
  public String toString() {
    return "V" + getVersionAsOfString() + ".C" + getCorrectedToString();
  }

}
