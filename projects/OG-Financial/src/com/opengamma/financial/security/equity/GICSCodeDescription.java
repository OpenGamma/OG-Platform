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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Objects;

/**
 * The description of a GICS code.
 * <p>
 * This provides a description for {@link GICSCode}.
 * <p>
 * This is an effective singleton.
 */
final class GICSCodeDescription {

  /** Logger. */
  public static final GICSCodeDescription INSTANCE = new GICSCodeDescription();
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(GICSCodeDescription.class);

  /**
   * The descriptions.
   */
  private static final Map<String, String> DESCRIPTIONS = new HashMap<String, String>();
  static {
    InputStream sectorsStream = GICSCodeDescription.class.getClassLoader().getResourceAsStream("com/opengamma/financial/security/equity/sectors.csv");
    parseCSV("sectors.csv", sectorsStream, DESCRIPTIONS);
    InputStream industryGroupsStream = GICSCodeDescription.class.getClassLoader().getResourceAsStream("com/opengamma/financial/security/equity/industry-groups.csv");
    parseCSV("industry-groups.csv", industryGroupsStream, DESCRIPTIONS);
    InputStream industriesStream = GICSCodeDescription.class.getClassLoader().getResourceAsStream("com/opengamma/financial/security/equity/industries.csv");
    parseCSV("industries.csv", industriesStream, DESCRIPTIONS);
    InputStream subIndutriesStream = GICSCodeDescription.class.getClassLoader().getResourceAsStream("com/opengamma/financial/security/equity/sub-industries.csv");
    parseCSV("sub-industries.csv", subIndutriesStream, DESCRIPTIONS);
  }

  private static void parseCSV(String filename, InputStream is, Map<String, String> resultMap) {
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
          resultMap.put(codeStr, description);
        } catch (NumberFormatException ex) {
          s_logger.warn("Couldn't parse " + codeStr + " in file " + filename);
        }
      }
    } catch (IOException ex) {
      s_logger.error("Couldn't read gics file " + filename, ex);
    }
  }

  /**
   * Creates an instance.
   */
  private GICSCodeDescription() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the description for the code.
   * 
   * @param code  the code to lookup, not null
   * @return the description, "Unknown" if not found
   */
  String getDescription(String code) {
    String desc = DESCRIPTIONS.get(code);
    return Objects.firstNonNull(desc, "Unknown");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a simple string description for the class.
   * 
   * @return the string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
