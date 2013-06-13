/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengamma.util.ArgumentChecker;

/**
 * Immutable representation of the version of a schema file used for
 * the xml portfolio import/export. A version number must be of the
 * form N.m where N represents the major version and n the minor.
 * Changes in minor version number are expected to be backwards compatible
 * whereas changes in major version are not expected to be.
 */
public class SchemaVersion implements Comparable<SchemaVersion> {

  /**
   * Regex with groups specifying:
   * - one or more digits (captured into group 1), followed by
   * - a period, followed by
   * - one or more digits (captured into group 2)
   */
  private static final Pattern s_pattern = Pattern.compile("(\\d+)\\.(\\d+)");

  /**
   * The major part of the version number.
   */
  private final int _majorVersion;

  /**
   * The minor part of the version number.
   */
  private final int _minorVersion;

  public SchemaVersion(String version) {

    ArgumentChecker.notNull(version, "version");

    Matcher matcher = s_pattern.matcher(version);
    ArgumentChecker.isTrue(matcher.matches(), "Version number must be of the form M.n");

    _majorVersion = Integer.parseInt(matcher.group(1));
    _minorVersion = Integer.parseInt(matcher.group(2));
  }

  @Override
  public int compareTo(SchemaVersion other) {
    return _majorVersion == other._majorVersion ?
        _minorVersion - other._minorVersion :
        _majorVersion - other._majorVersion;
  }

  @Override
  public int hashCode() {
    return 31 * _majorVersion + _minorVersion;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SchemaVersion that = (SchemaVersion) o;

    // Minor version is more likely to differ than major so compare it first
    return _minorVersion == that._minorVersion && _majorVersion == that._majorVersion;
  }

  @Override
  public String toString() {
    return _majorVersion + "." + _minorVersion;
  }
}
