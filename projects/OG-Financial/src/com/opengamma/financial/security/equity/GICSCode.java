/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.equity;

import java.io.Serializable;

/**
 * Representation of a GICS code.
 * <p>
 * A Global Industry Classification Standard code (GICS) is an 8 digit code
 * used to identify the sectors and industries that a company operates in.
 * <p>
 * The 8 digits are divided into 4 levels:
 * <ul>
 * <li>Sector
 * <li>Industry group
 * <li>Industry
 * <li>Sub-Industry
 * </ul>
 * For example, "Highways and Railtracks" is defined as follows:
 * <ul>
 * <li>Sector - Industrial - code 20
 * <li>Industry group - Transportation - code 30 (combined code 2030)
 * <li>Industry - Transportation infrastructure - code 50 (combined code 203050)
 * <li>Sub-Industry - Highways and Railtracks - code 20 (combined code 20305020)
 * </ul>
 * <p>
 * GICSCode is immutable and thread-safe.
 */
public final class GICSCode implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The integer version of the code.
   */
  private final int _code;

  /**
   * Obtains a {@code GICSCode} instance from the combined code.
   * <p>
   * The code specified must follow the GICS code standard, being a number
   * between 1 and 99999999 inclusive where no two digit part is 0.
   * The number is not validated against known values.
   * 
   * @param code  the value from 1 to 99999999 inclusive
   * @return the GICS instance, not null
   * @throws IllegalArgumentException if the value is invalid
   */
  public static GICSCode getInstance(final int code) {
    if ((code < 1) || (code > 99999999)) {
      throw new IllegalArgumentException("code out of range " + code);
    }
    int c = code;
    while (c >= 100) {
      if ((c % 100) == 0) {
        throw new IllegalArgumentException("invalid code " + code);
      }
      c /= 100;
    }
    return new GICSCode(code);
  }

  /**
   * Obtains a {@code GICSCode} instance from the combined code.
   * <p>
   * The code specified must follow the GICS code standard, being a number
   * between 1 and 99999999 inclusive where no two digit part is 0.
   * The number is not validated against known values.
   * 
   * @param code  the value from 1 to 99999999 inclusive
   * @return the GICS instance, not null
   * @throws IllegalArgumentException if the value is invalid
   */
  public static GICSCode getInstance(final String code) {
    try {
      return getInstance(Integer.parseInt(code));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("code is not valid", e);
    }
  }

  /**
   * Creates an instance with a specific code.
   * 
   * @param code  the GICS code, from 1 to 99999999
   */
  private GICSCode(final int code) {
    _code = code;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the combined code.
   * <p>
   * The combined code will consist of the sector, group, industry and sub-industry parts.
   * <p>
   * Note that if the code represents only a sector then the value will be from 1 to 99.
   * For example, a sector of 20 is returned as 20, not 20000000.
   * 
   * @return the combined code, from 1 to 99999999 inclusive
   */
  public int getCode() {
    return _code;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the sector code.
   * <p>
   * The sector code is the most important part of the classification.
   * It is the first two digits of the code.
   * 
   * @return the sector code, from 1 to 99
   */
  public int getSectorCode() {
    int c = getCode();
    while (c >= 100) {
      c /= 100;
    }
    return c;
  }

  /**
   * Gets the industry group code.
   * <p>
   * The group code is the second most important part of the classification.
   * It is the second two digits of the code.
   * 
   * @return the industry group code, from 1 to 99, or -1 if no group
   */
  public int getIndustryGroupCode() {
    int c = getCode();
    if (c < 100) {
      return -1;
    }
    while (c >= 10000) {
      c /= 100;
    }
    return c % 100;
  }

  /**
   * Gets the industry code.
   * <p>
   * The group code is the third most important part of the classification.
   * It is the third two digits of the code.
   * 
   * @return the industry code, from 1 to 99, or -1 if no industry
   */
  public int getIndustryCode() {
    int c = getCode();
    if (c < 10000) {
      return -1;
    }
    while (c >= 1000000) {
      c /= 100;
    }
    return c % 100;
  }

  /**
   * Gets the sub-industry code.
   * <p>
   * The group code is the least important part of the classification.
   * It is the fourth two digits of the code.
   * 
   * @return the sub-industry code, from 1 to 99, or -1 if no sub-industry
   */
  public int getSubIndustryCode() {
    int c = getCode();
    if (c < 1000000) {
      return -1;
    }
    return c % 100;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares this code to another based on the combined code.
   * 
   * @param obj  the other code, null returns false
   * @return true of equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof GICSCode) {
      GICSCode other = (GICSCode) obj;
      return getCode() == other.getCode();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getCode();
  }

  /**
   * Returns a string representation of the code, which is always an even number of digits.
   * 
   * @return the string version of the code, not null
   */
  @Override
  public String toString() {
    String str = Integer.toString(getCode());
    return str.length() % 2 == 0 ? str : "0" + str;
  }

}
