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
public final class SchemaVersionUtils {
  
  private static final Logger s_logger = LoggerFactory.getLogger(SchemaVersionUtils.class);
  
  private SchemaVersionUtils() {
  }
  
  public static Integer readSchemaVersion(String path, String schemaName) {
    InputStream schemaVersionStream = ClassLoader.getSystemResourceAsStream(path + "/" + schemaName);
    if (schemaVersionStream == null) {
      return null;
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(schemaVersionStream));
    try {
      String version = reader.readLine();
      return Integer.parseInt(version);
    } catch (Exception e) {
      s_logger.warn("Error reading schema version '" + schemaName + "'", e);
      return null;
    }
  }

}
