/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

// REVIEW kirk 2010-05-13 -- This might belong outside the test hierarchy if we
// start using MongoDB for real.

/**
 * 
 *
 * @author kirk
 */
public class MongoDBConnectionSettings {
  private String _hostName;
  private int _port;
  private String _databaseName;
  private String _collectionName;
  /**
   * @return the hostName
   */
  public String getHostName() {
    return _hostName;
  }
  /**
   * @param hostName the hostName to set
   */
  public void setHostName(String hostName) {
    _hostName = hostName;
  }
  /**
   * @return the port
   */
  public int getPort() {
    return _port;
  }
  /**
   * @param port the port to set
   */
  public void setPort(int port) {
    _port = port;
  }
  /**
   * @return the databaseName
   */
  public String getDatabaseName() {
    return _databaseName;
  }
  /**
   * @param databaseName the databaseName to set
   */
  public void setDatabaseName(String databaseName) {
    _databaseName = databaseName;
  }
  /**
   * @return the collectionName
   */
  public String getCollectionName() {
    return _collectionName;
  }
  /**
   * @param connectionName the connectionName to set
   */
  public void setCollectionName(String connectionName) {
    _collectionName = connectionName;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
