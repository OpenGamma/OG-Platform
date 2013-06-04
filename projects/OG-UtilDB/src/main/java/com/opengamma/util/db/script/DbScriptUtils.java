/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Utilities around master database schema versions.
 */
public final class DbScriptUtils {

  private static final String METADATA_FILE = "ogdb-metadata.properties";
  private static final String METADATA_RESOURCE_PATH = "db/" + METADATA_FILE;

  private static final Logger s_logger = LoggerFactory.getLogger(DbScriptUtils.class);
  
  private static final Map<String, DbSchemaGroupMetadata> s_dbSchemaGroupMetadata;
  
  static {
    Map<String, DbSchemaGroupMetadata> schemaGroupMetadata = new HashMap<String, DbSchemaGroupMetadata>();
    ClassLoader classLoader = DbScriptUtils.class.getClassLoader();
    try {
      Enumeration<URL> metadataResourceUrls = classLoader.getResources(METADATA_RESOURCE_PATH);
      while (metadataResourceUrls.hasMoreElements()) {
        URL metadataResourceUrl = metadataResourceUrls.nextElement();
        String metadataResourceUrlString = metadataResourceUrl.toExternalForm();
        String baseResourceUrlString = metadataResourceUrlString.substring(0, metadataResourceUrlString.length() - METADATA_FILE.length() - 1);
        try {
          InputStream in = metadataResourceUrl.openStream();
          try {
            Properties properties = new Properties();
            properties.load(in);
            for (Map.Entry<Object, Object> metadata : properties.entrySet()) {
              String schemaGroupName = (String) metadata.getKey();
              if (schemaGroupMetadata.containsKey(schemaGroupName)) {
                continue;
              }
              int currentVersion = Integer.parseInt((String) metadata.getValue());
              schemaGroupMetadata.put(schemaGroupName, new DbSchemaGroupMetadata(schemaGroupName, baseResourceUrlString, currentVersion));
            }
          } catch (Exception e) {
            s_logger.error("Error reading database metadata resource at " + metadataResourceUrl, e);
          } finally {
            in.close();
          }
        } catch (IOException e) {
          s_logger.error("Error opening database metadata resource at " + metadataResourceUrl, e);
        }
      }
    } catch (IOException e) {
      s_logger.error("Error looking for database metadata resources", e);
    }

    s_dbSchemaGroupMetadata = ImmutableMap.copyOf(schemaGroupMetadata);
  }
  
  private DbScriptUtils() {
  }
  
  public static Integer getCurrentVersion(String schemaGroupName) {
    DbSchemaGroupMetadata metadata = getDbSchemaGroupMetadata(schemaGroupName);
    if (metadata == null) {
      return null;
    }
    return metadata.getCurrentVersion();
  }
  
  public static Set<String> getAllSchemaNames() {
    return s_dbSchemaGroupMetadata.keySet();
  }
  
  public static Collection<DbSchemaGroupMetadata> getAllSchemaGroupMetadata() {
    return s_dbSchemaGroupMetadata.values();
  }
  
  public static DbSchemaGroupMetadata getDbSchemaGroupMetadata(String schemaGroupName) {
    return getDbSchemaGroupMetadata().get(schemaGroupName);
  }
  
  private static Map<String, DbSchemaGroupMetadata> getDbSchemaGroupMetadata() {
    return s_dbSchemaGroupMetadata;
  }

}
