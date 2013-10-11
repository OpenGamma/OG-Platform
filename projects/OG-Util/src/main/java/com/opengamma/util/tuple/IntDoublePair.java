/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import org.apache.commons.lang.StringUtils;
import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.opengamma.util.ArgumentChecker;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

/**
 * An immutable pair consisting of an {@code int} and {@code double}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 * <p>
 * This class is immutable and thread-safe.
 */
public class IntDoublePair extends Pair<Integer, Double> implements Int2DoubleMap.Entry {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final int first; // CSIGNORE
  /** The second element. */
  public final double second; // CSIGNORE

  //-------------------------------------------------------------------------
  /**
   * Obtains an {@code IntDoublePair} from a {@code Pair}.
   * 
   * @param pair  the pair to convert, not null
   * @return a pair formed by extracting values from the pair, not null
   */
  public static IntDoublePair of(final Pair<Integer, Double> pair) {
    if (pair instanceof IntDoublePair) {
      return (IntDoublePair) pair;
    }
    ArgumentChecker.notNull(pair, "pair");
    ArgumentChecker.notNull(pair.getFirst(), "pair.first");
    ArgumentChecker.notNull(pair.getSecond(), "pair.second");
    return new IntDoublePair(pair.getFirst(), pair.getSecond());
  }

  /**
   * Obtains an {@code IntDoublePair} from two values.
   * 
   * @param first  the first element
   * @param second  the second element
   * @return a pair formed from the two parameters, not null
   */
  public static IntDoublePair of(final int first, final double second) {
    return new IntDoublePair(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Parses an {@code IntDoublePair} from the standard string format.
   * <p>
   * The standard format is '[$first, $second]'. Spaces around the values are trimmed.
   * 
   * @param pairStr  the text to parse, not null
   * @return the parsed pair, not null
   */
  @FromString
  public static IntDoublePair parse(final String pairStr) {
    ArgumentChecker.notNull(pairStr, "pairStr");
    if (pairStr.length() < 5) {
      throw new IllegalArgumentException("Invalid pair format, too short: " + pairStr);
    }
    if (pairStr.charAt(0) != '[') {
      throw new IllegalArgumentException("Invalid pair format, must start with [: " + pairStr);
    }
    if (pairStr.charAt(pairStr.length() - 1) != ']') {
      throw new IllegalArgumentException("Invalid pair format, must end with ]: " + pairStr);
    }
    String[] split = StringUtils.split(pairStr.substring(1, pairStr.length() - 1), ',');
    if (split.length != 2) {
      throw new IllegalArgumentException("Invalid pair format, must have two values: " + pairStr);
    }
    int first = Integer.parseInt(split[0].trim());
    double second = Double.parseDouble(split[1].trim());
    return new IntDoublePair(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a pair.
   * 
   * @param first  the first element
   * @param second  the second element
   * @deprecated Use public factory of(int,double)
   */
  @Deprecated
  public IntDoublePair(final int first, final double second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getFirst() {
    return first;
  }

  @Override
  public Double getSecond() {
    return second;
  }

  /**
   * Gets the first element as a primitive {@code int}.
   * 
   * @return the primitive
   */
  public int getFirstInt() {
    return first;
  }

  /**
   * Gets the second element as a primitive {@code double}.
   * 
   * @return the primitive
   */
  public double getSecondDouble() {
    return second;
  }

  //-------------------------------------------------------------------------
  @Override
  public int getIntKey() {
    return first;
  }

  @Override
  public double getDoubleValue() {
    return second;
  }

  @Override
  public double setValue(final double value) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IntDoublePair) {
      final IntDoublePair other = (IntDoublePair) obj;
      return this.first == other.first && this.second == other.second;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long d = Double.doubleToLongBits(second);
    return first ^ ((int) (d ^ (d >>> 32)));
  }

  /**
   * Gets the pair using a standard string format.
   * <p>
   * The standard format is '[$first, $second]'. Spaces around the values are trimmed.
   * 
   * @return the pair as a string, not null
   */
  @Override
  @ToString
  public String toString() {
    return new StringBuilder()
        .append("[")
        .append(first)
        .append(", ")
        .append(second)
        .append("]").toString();
  }

}
