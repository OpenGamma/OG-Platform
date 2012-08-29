/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.schema;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public final class SchemaVersionUtils {
  
  private static final Logger s_logger = LoggerFactory.getLogger(SchemaVersionUtils.class);
  
  private static final String SCHEMA_VERSION_DIR = "com/opengamma/masterdb/schema"; 
  
  private SchemaVersionUtils() {
  }
  
  public static Integer readSchemaVersion(String schemaName) {
    InputStream schemaVersionStream = ClassLoader.getSystemResourceAsStream(SCHEMA_VERSION_DIR + "/" + schemaName);
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
  
  public static File getSchemaVersionDir(final String outputDir) {
    return new File(outputDir + File.separator + SCHEMA_VERSION_DIR);
  }

}
