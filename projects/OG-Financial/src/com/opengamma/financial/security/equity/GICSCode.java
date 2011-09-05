/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.equity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

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

  private static final Logger s_logger = LoggerFactory.getLogger(GICSCode.class);
  /**
   * The integer version of the code.
   */
  private final int _code;

  private static Map<Integer, String> s_sectors = new HashMap<Integer, String>();
  private static Map<Integer, String> s_industryGroups = new HashMap<Integer, String>();
  private static Map<Integer, String> s_industries = new HashMap<Integer, String>();
  private static Map<Integer, String> s_subIndustries = new HashMap<Integer, String>();

  static {
    InputStream sectorsStream = GICSCode.class.getClassLoader().getResourceAsStream("com/opengamma/financial/security/equity/sectors.csv");
    parseCSV("sectors.csv", sectorsStream, s_sectors);
    InputStream industryGroupsStream = GICSCode.class.getClassLoader().getResourceAsStream("com/opengamma/financial/security/equity/industry-groups.csv");
    parseCSV("industry-groups.csv", industryGroupsStream, s_industryGroups);
    InputStream industriesStream = GICSCode.class.getClassLoader().getResourceAsStream("com/opengamma/financial/security/equity/industries.csv");
    parseCSV("industries.csv", industriesStream, s_industries);
    InputStream subIndutriesStream = GICSCode.class.getClassLoader().getResourceAsStream("com/opengamma/financial/security/equity/sub-industries.csv");
    parseCSV("sub-industries.csv", subIndutriesStream, s_subIndustries);
  }

  private static void parseCSV(String filename, InputStream is, Map<Integer, String> resultMap) {
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(is)));
    try {
      List<String[]> rows = reader.readAll();
      for (String[] row : rows) {
        if (row.length != 2) {
          s_logger.warn("Row in " + filename + " has more or less than two items, aborting");
          break;
        }
        String codeStr = row[0];
        String description = row[1];
        try {
          Integer code = Integer.parseInt(codeStr);
          resultMap.put(code, description);
        } catch (NumberFormatException ex) {
          s_logger.warn("Couldn't parse " + codeStr + " in file " + filename);
        }
      }
    } catch (IOException ex) {
      s_logger.error("Couldn't read gics file " + filename, ex);
    }
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

  /**
   * Gets the combined code as a string.
   * <p>
   * The combined code will consist of the sector, group, industry and sub-industry parts.
   * <p>
   * Note that if the code represents only a sector then the value will be from 1 to 99.
   * For example, a sector of 20 is returned as 20, not 20000000.
   * 
   * @return the combined code, from 1 to 99999999 inclusive
   */
  public String getCodeString() {
    return Integer.toString(getCode());
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
    return getCode() / 1000000;
  }

  /**
   * Gets the sector description
   * @return the description of the sector, or "Unknown" if not found
   */
  public String getSectorDescription() {
    String description = s_sectors.get(getSectorCode());
    return description != null ? description : "Unknown"; 
  }

  /**
   * Gets the industry group code.
   * <p>
   * The group code is the second most important part of the classification.
   * It is the first four digits of the code.
   * 
   * @return the industry group code
   */
  public int getIndustryGroupCode() {
    return getCode() / 10000;
  }

  /**
   * Gets the industry group description
   * @return the description of the industry group, or "Unknown" if not found
   */
  public String getIndustryGroupDescription() {
    String description = s_industryGroups.get(getIndustryGroupCode());
    return description != null ? description : "Unknown"; 
  }

  /**
   * Gets the industry code.
   * <p>
   * The group code is the third most important part of the classification.
   * It is the first six digits of the code.
   * 
   * @return the industry code, from 1 to 99, or -1 if no industry
   */
  public int getIndustryCode() {
    return getCode() / 100;
  }

  /**
   * Gets the industry description
   * @return the description of the industry, or "Unknown" if not found
   */
  public String getIndustryDescription() {
    String description = s_industries.get(getIndustryCode());
    return description != null ? description : "Unknown"; 
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
    return getCode();
  }

  /**
   * Gets the sub-industry description
   * @return the description of the sub-industry, or "Unknown" if not found
   */
  public String getSubIndustryDescription() {
    String description = s_subIndustries.get(getSubIndustryCode());
    return description != null ? description : "Unknown"; 
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
   * Returns a string description of the code, which incldues the code and a description.
   * 
   * @return the string version of the code, not null
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getCodeString());
    sb.append(" ");
    sb.append(getSubIndustryDescription());
    return sb.toString();
  }

}
