/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.io.File;

import com.opengamma.util.SingletonFactoryBean;
import com.sleepycat.je.Environment;

/**
 * 
 */
public class BerkeleyDBBinaryDataStoreFactoryFactoryBean extends SingletonFactoryBean<BerkeleyDBBinaryDataStoreFactory> {

  private static final String DEFAULT_DATASTORE_FOLDER = "BerkeleyDBBinaryDataStore";

  private String _dataStoreBaseFolder;
  private String _dataStoreFolder;

  public BerkeleyDBBinaryDataStoreFactoryFactoryBean() {
    final String temp = System.getProperty("java.io.tmpdir");
    setDataStoreBaseFolder(temp);
    setDataStoreFolder(DEFAULT_DATASTORE_FOLDER);
  }

  public void setDataStoreBaseFolder(final String dataStoreBaseFolder) {
    _dataStoreBaseFolder = dataStoreBaseFolder;
  }

  public String getDataStoreBaseFolder() {
    return _dataStoreBaseFolder;
  }

  public void setDataStoreFolder(final String dataStoreFolder) {
    _dataStoreFolder = dataStoreFolder;
  }

  public String getDataStoreFolder() {
    return _dataStoreFolder;
  }

  private File getFolder(final String base, final String folder) {
    return new File(new File(base), folder);
  }

  protected Environment createDataStoreEnvironment() {
    final File dataStore = getFolder(getDataStoreBaseFolder(), getDataStoreFolder());
    final Environment dataStoreEnvironment = BerkeleyDBViewComputationCacheSource.constructDatabaseEnvironment(dataStore, false);
    return dataStoreEnvironment;
  }

  @Override
  protected BerkeleyDBBinaryDataStoreFactory createObject() {
    return new BerkeleyDBBinaryDataStoreFactory(createDataStoreEnvironment());
  }

}
