/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.equity;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

import org.joda.convert.FromString;
import org.joda.convert.ToString;

/**
 * Representation of a GICS code.
 * <p>
 * A Global Industry Classification Standard code (GICS) is an 8 digit code
 * used to identify the sectors and industries that a company operates in.
 * <p>
 * The 8 digits are divided into 4 digit-pairs representing a description hierarchy:
 * <ul>
 * <li>Sector
 * <li>Industry-group
 * <li>Industry
 * <li>Sub-Industry
 * </ul>
 * For example, "Highways and Railtracks" is defined as follows:
 * <ul>
 * <li>Sector - Industrial - code 20
 * <li>Industry group - Transportation - code 2030
 * <li>Industry - Transportation infrastructure - code 203050
 * <li>Sub-Industry - Highways and Railtracks - code 20305020
 * </ul>
 * <p>
 * GICSCode is immutable and thread-safe.
 */
public final class GICSCode implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /** Pattern for the code. */
  private static final Pattern FORMAT = Pattern.compile("([1-9][0-9]){1,4}");

  /**
   * The code.
   */
  private final String _code;

  /**
   * Obtains a {@code GICSCode} instance from the combined code.
   * <p>
   * The code specified must follow the GICS code standard, being a number
   * between 1 and 99999999 inclusive where no two digit part is 0.
   * The number is not validated against known values.
   * 
   * @param code  the value from 10 to 99999999 inclusive
   * @return the GICS instance, not null
   * @throws IllegalArgumentException if the value is invalid
   */
  @FromString
  public static GICSCode of(final String code) {
    if (FORMAT.matcher(code).matches() == false) {
      throw new IllegalArgumentException("Invalid code : " + code);
    }
    return new GICSCode(code);
  }

  /**
   * Obtains a {@code GICSCode} instance from the combined code.
   * <p>
   * The code specified must follow the GICS code standard.
   * The number must be between 10 and 99999999 inclusive where each digit-pair
   * must be a number from 10 to 99.
   * The number is not validated against known values in the standard.
   * 
   * @param code  the value from 1 to 99999999 inclusive
   * @return the GICS instance, not null
   * @throws IllegalArgumentException if the value is invalid
   */
  public static GICSCode of(final int code) {
    if ((code < 10) || (code > 99999999)) {
      throw new IllegalArgumentException("Code out of range: " + code);
    }
    return GICSCode.of(Integer.toString(code));
  }

  /**
   * Creates an instance with a specific code.
   * 
   * @param code  the GICS code, from 10 to 99999999
   */
  private GICSCode(final String code) {
    _code = code;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the full code.
   * <p>
   * The combined code will consist of the sector, group, industry and sub-industry parts.
   * The returned length will be 2, 4, 6, or 8 characters long.
   * For example, if the code represents only a sector then the value will be from 10 to 99.
   * 
   * @return the combined code, from 10 to 99999999 inclusive
   */
  @ToString
  public String getCode() {
    return _code;
  }

  /**
   * Gets the combined code as an {@code int}.
   * <p>
   * The combined code will consist of the sector, group, industry and sub-industry parts.
   * This is the equivalent of {@link #getCode()}.
   * 
   * @return the combined code, from 10 to 99999999 inclusive
   */
  public int getCodeInt() {
    return Integer.parseInt(getCode());
  }

  /**
   * Gets the best available description.
   * 
   * @return the description, or "Unknown" if not found
   */
  public String getDescription() {
    switch (getCode().length()) {
      case 2:
        return getSectorDescription();
      case 4:
        return getIndustryGroupDescription();
      case 6:
        return getIndustryDescription();
      case 8:
        return getSubIndustryDescription();
    }
    return "Unknown";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the sector code.
   * <p>
   * The sector code is the most important part of the classification.
   * It is the first two digits of the code.
   * 
   * @return the sector code, from 10 to 99
   */
  public String getSectorCode() {
    return getCode().length() >= 2 ? getCode().substring(0, 2) : "";
  }

  /**
   * Gets the sector code as an {@code int}.
   * <p>
   * The sector code is the most important part of the classification.
   * It is the first two digits of the code.
   * This is the equivalent of {@link #getSectorCode()}.
   * 
   * @return the sector code, from 10 to 99
   */
  public int getSectorCodeInt() {
    return Integer.parseInt(getSectorCode());
  }

  /**
   * Gets the sector description.
   * 
   * @return the description of the sector, or "Unknown" if not found
   */
  public String getSectorDescription() {
    String description = GICSCodeDescription.INSTANCE.getDescription(getSectorCode());
    return description != null ? description : "Unknown";
  }
  
  /**
   * Gets all the sectors.
   * 
   * @return a collection of all the sectors
   */
  public static Collection<String> getAllSectorDescriptions() {
    return GICSCodeDescription.INSTANCE.getAllSectorDescriptions();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the industry-group code.
   * <p>
   * The industry-group code is the second most important part of the classification.
   * It is the first four digits of the code.
   * 
   * @return the industry-group code, from 1010 to 9999, empty if no industry-group
   */
  public String getIndustryGroupCode() {
    return getCode().length() >= 4 ? getCode().substring(0, 4) : "";
  }

  /**
   * Gets the industry-group code as an {@code int}.
   * <p>
   * The industry-group code is the second most important part of the classification.
   * It is the first four digits of the code.
   * This is the equivalent of {@link #getIndustryGroupCode()}.
   * 
   * @return the industry-group code, from 1010 to 9999, 0 if no industry-group
   */
  public int getIndustryGroupCodeInt() {
    if (getCode().length() < 4) {
      return 0;
    }
    return Integer.parseInt(getIndustryGroupCode());
  }

  /**
   * Gets the industry-group description.
   * 
   * @return the description of the industry-group, "Unknown" if not found, empty if no industry-group
   */
  public String getIndustryGroupDescription() {
    if (getCode().length() < 4) {
      return "";
    }
    String description = GICSCodeDescription.INSTANCE.getDescription(getIndustryGroupCode());
    return description != null ? description : "Unknown";
  }
  
  /**
   * Gets all the industry group descriptions.
   * 
   * @return a collection of all the industry group descriptions
   */
  public static Collection<String> getAllIndustryGroupDescriptions() {
    return GICSCodeDescription.INSTANCE.getAllIndustryGroupDescriptions();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the industry code.
   * <p>
   * The industry code is the third most important part of the classification.
   * It is the first six digits of the code.
   * 
   * @return the industry code, from 101010 to 999999, empty if no industry
   */
  public String getIndustryCode() {
    return getCode().length() >= 6 ? getCode().substring(0, 6) : "";
  }

  /**
   * Gets the industry code as an {@code int}.
   * <p>
   * The industry code is the third most important part of the classification.
   * It is the first six digits of the code.
   * This is the equivalent of {@link #getIndustryCode()}.
   * 
   * @return the industry code, from 101010 to 999999, 0 if no industry
   */
  public int getIndustryCodeInt() {
    if (getCode().length() < 6) {
      return 0;
    }
    return Integer.parseInt(getIndustryCode());
  }

  /**
   * Gets the industry description.
   * 
   * @return the description of the industry, "Unknown" if not found, empty if no industry
   */
  public String getIndustryDescription() {
    if (getCode().length() < 6) {
      return "";
    }
    String description = GICSCodeDescription.INSTANCE.getDescription(getIndustryCode());
    return description != null ? description : "Unknown";
  }
  
  /**
   * Gets all the industry descriptions.
   * 
   * @return a collection of all the industry descriptions
   */
  public static Collection<String> getAllIndustryDescriptions() {
    return GICSCodeDescription.INSTANCE.getAllIndustryDescriptions();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the sub-industry code.
   * <p>
   * The group code is the least important part of the classification.
   * It is the first eight digits of the code.
   * 
   * @return the sub-industry code, from 10101010 to 99999999
   */
  public String getSubIndustryCode() {
    return getCode().length() == 8 ? getCode() : "";
  }

  /**
   * Gets the sub-industry code as an {@code int}.
   * <p>
   * The group code is the least important part of the classification.
   * It is the first eight digits of the code.
   * This is the equivalent of {@link #getSubIndustryCode()}.
   * 
   * @return the sub-industry code, from 10101010 to 99999999, 0 if no sub-industry
   */
  public int getSubIndustryCodeInt() {
    if (getCode().length() < 8) {
      return 0;
    }
    return Integer.parseInt(getSubIndustryCode());
  }

  /**
   * Gets the sub-industry description.
   * 
   * @return the description of the sub-industry, "Unknown" if not found, empty if no sub-industry
   */
  public String getSubIndustryDescription() {
    if (getCode().length() < 8) {
      return "";
    }
    String description = GICSCodeDescription.INSTANCE.getDescription(getSubIndustryCode());
    return description != null ? description : "Unknown";
  }
  
  /**
   * Gets all the sub-industry descriptions.
   * 
   * @return a collection of all the sub-industry descriptions
   */
  public static Collection<String> getAllSubIndustryDescriptions() {
    return GICSCodeDescription.INSTANCE.getAllSubIndustryDescriptions();
  }
  
  //-------------------------------------------------------------------------
  /**
   * Checks if the code is a complete 8 digit sub-industry code.
   * 
   * @return true if complete 8 digit code
   */
  public boolean isComplete() {
    return getCode().length() == 8;
  }

  /**
   * Checks if the code is a partial code of less than 8 digits.
   * 
   * @return true if less than the complete 8 digit code
   */
  public boolean isPartial() {
    return getCode().length() < 8;
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a code object for the sector only.
   * <p>
   * This returns a {@code GICSCode} representing just the sector.
   * 
   * @return the object representing the sector, not null
   */
  public GICSCode toSector() {
    return GICSCode.of(getSectorCode());
  }

  /**
   * Obtains a code object for the industry-group only.
   * <p>
   * This returns a {@code GICSCode} representing just the industry-group.
   * Null is returned if this object represents a sector.
   * 
   * @return the object representing the industry-group, null if no industry-group code
   */
  public GICSCode toIndustryGroup() {
    if (getCode().length() < 4) {
      return null;
    }
    return GICSCode.of(getIndustryGroupCode());
  }

  /**
   * Obtains a code object for the industry only.
   * <p>
   * This returns a {@code GICSCode} representing just the industry.
   * Null is returned if this object represents a sector or industry-group.
   * 
   * @return the object representing the industry, null if no industry code
   */
  public GICSCode toIndustry() {
    if (getCode().length() < 6) {
      return null;
    }
    return GICSCode.of(getIndustryCode());
  }

  /**
   * Obtains a code object for the sub-industry only.
   * <p>
   * This returns a {@code GICSCode} representing just the sub-industry.
   * Null is returned if this object represents a sector, industry-group or industry.
   * 
   * @return the object representing the industry, null if no industry code
   */
  public GICSCode toSubIndustry() {
    if (getCode().length() < 8) {
      return null;
    }
    return this;
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
      return getCode().equals(other.getCode());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getCode().hashCode();
  }

  /**
   * Returns a string description of the code, which includes the code and a description.
   * 
   * @return the string version of the code, not null
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getCode());
    sb.append(" ");
    sb.append(getSubIndustryDescription());
    return sb.toString();
  }

}
