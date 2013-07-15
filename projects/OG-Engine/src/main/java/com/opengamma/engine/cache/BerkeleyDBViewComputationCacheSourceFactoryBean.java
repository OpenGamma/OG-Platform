/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.SingletonFactoryBean;

/**
 * 
 */
public class BerkeleyDBViewComputationCacheSourceFactoryBean extends SingletonFactoryBean<BerkeleyDBViewComputationCacheSource> {
  
  private BerkeleyDBIdentifierMapFactoryBean _identifierMapFactory = new BerkeleyDBIdentifierMapFactoryBean();
  private BerkeleyDBBinaryDataStoreFactoryFactoryBean _dataStoreFactoryFactory = new BerkeleyDBBinaryDataStoreFactoryFactoryBean();

  public void setIdentifierMapFactory(final BerkeleyDBIdentifierMapFactoryBean identifierMapFactory) {
    _identifierMapFactory = identifierMapFactory;
  }
  
  public BerkeleyDBIdentifierMapFactoryBean getIdentifierMapFactory() {
    return _identifierMapFactory;
  }

  public void setDataStoreFactoryFactory(final BerkeleyDBBinaryDataStoreFactoryFactoryBean dataStoreFactoryFactory) {
    _dataStoreFactoryFactory = dataStoreFactoryFactory;
  }

  public BerkeleyDBBinaryDataStoreFactoryFactoryBean getDataStoreFactoryFactory() {
    return _dataStoreFactoryFactory;
  }

  public void setIdentifierBaseFolder(final String identifierBaseFolder) {
    getIdentifierMapFactory().setIdentifierBaseFolder(identifierBaseFolder);
  }

  public String getIdentifierBaseFolder() {
    return getIdentifierMapFactory().getIdentifierBaseFolder();
  }

  public void setIdentifierFolder(final String identifierFolder) {
    getIdentifierMapFactory().setIdentifierFolder(identifierFolder);
  }

  public String getIdentifierFolder() {
    return getIdentifierMapFactory().getIdentifierFolder();
  }

  public void setDataStoreBaseFolder(final String dataStoreBaseFolder) {
    getDataStoreFactoryFactory().setDataStoreBaseFolder(dataStoreBaseFolder);
  }

  public String getDataStoreBaseFolder() {
    return getDataStoreFactoryFactory().getDataStoreBaseFolder();
  }

  public void setDataStoreFolder(final String dataStoreFolder) {
    getDataStoreFactoryFactory().setDataStoreFolder(dataStoreFolder);
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    getIdentifierMapFactory().setFudgeContext(fudgeContext);
  }

  public FudgeContext getFudgeContext() {
    return getIdentifierMapFactory().getFudgeContext();
  }

  public String getDataStoreFolder() {
    return getDataStoreFactoryFactory().getDataStoreFolder();
  }

  @Override
  protected BerkeleyDBViewComputationCacheSource createObject() {
    return new BerkeleyDBViewComputationCacheSource(getIdentifierMapFactory().createObject(), getDataStoreFactoryFactory().createDataStoreEnvironment(), getFudgeContext());
  }

}
