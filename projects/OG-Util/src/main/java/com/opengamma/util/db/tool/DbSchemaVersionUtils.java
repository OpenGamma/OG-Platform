/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities around master database schema versions.
 */
public final class DbSchemaVersionUtils {
  
  private static final String FILE_SUFFIX = ".version";
  
  private static final Logger s_logger = LoggerFactory.getLogger(DbSchemaVersionUtils.class);
  
  private DbSchemaVersionUtils() {
  }
  
  public static String getSchemaVersionFileName(String schemaName) {
    return schemaName + FILE_SUFFIX;
  }
  
  public static Integer readVersion(String path, String schemaName) {
    InputStream schemaVersionStream = ClassLoader.getSystemResourceAsStream(path + "/" + getSchemaVersionFileName(schemaName));
    if (schemaVersionStream == null) {
      return null;
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(schemaVersionStream))) {
      String version = reader.readLine();
      return Integer.parseInt(version);
    } catch (Exception e) {
      s_logger.warn("Error reading version file for schema '" + schemaName + "'", e);
      return null;
    }
  }

}
