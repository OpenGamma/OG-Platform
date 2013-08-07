/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import com.opengamma.util.ArgumentChecker;

/**
 * Wraps a {@link Pattern} and defines meaningful {@code equals()} and {@code hashCode()} methods. {@code Pattern}
 * inherits the default implementations from {@code Object} which aren't helpful for comparing logical equality.
 * This class removes the need to hand-code the {@code equals()} and {@code hashCode()} methods of any class that
 * holds a {@code Pattern}.
 */
public final class PatternWrapper implements Serializable {

  private final Pattern _pattern;

  /**
   * @param pattern The pattern to wrap, not null
   */
  private PatternWrapper(Pattern pattern) {
    ArgumentChecker.notNull(pattern, "p");
    _pattern = pattern;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatternWrapper that = (PatternWrapper) o;
    return Objects.equals(_pattern.pattern(), that._pattern.pattern()) &&
        Objects.equals(_pattern.flags(), that._pattern.flags());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_pattern.pattern(), _pattern.flags());
  }

  /**
   * @return The wrapped pattern, not null
   */
  public Pattern getPattern() {
    return _pattern;
  }

  /**
   * Wraps a {@link Pattern}
   * @param pattern The pattern to wrap, may be null
   * @return A wrapped pattern or null if the argument is null
   */
  public static PatternWrapper wrap(Pattern pattern) {
    if (pattern == null) {
      return null;
    }
    return new PatternWrapper(pattern);
  }

  @Override
  public String toString() {
    String pattern;
    Integer flags;
    if (_pattern == null) {
      pattern = null;
      flags = null;
    } else {
      pattern = _pattern.pattern();
      flags = _pattern.flags();
    }
    return "PatternWrapper [pattern=" + pattern + ", flags=" + flags + "]";
  }
}
