/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.io.File;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.SingletonFactoryBean;
import com.sleepycat.je.Environment;

/**
 * 
 */
public class BerkeleyDBIdentifierMapFactoryBean extends SingletonFactoryBean<BerkeleyDBIdentifierMap> {

  private static final String DEFAULT_IDENTIFIER_FOLDER = "BerkeleyDBIdentifierMap";

  private String _identifierBaseFolder;
  private String _identifierFolder;
  private FudgeContext _fudgeContext;

  public BerkeleyDBIdentifierMapFactoryBean() {
    final String temp = System.getProperty("java.io.tmpdir");
    setIdentifierBaseFolder(temp);
    setIdentifierFolder(DEFAULT_IDENTIFIER_FOLDER);
  }

  public void setIdentifierBaseFolder(final String identifierBaseFolder) {
    _identifierBaseFolder = identifierBaseFolder;
  }

  public String getIdentifierBaseFolder() {
    return _identifierBaseFolder;
  }

  public void setIdentifierFolder(final String identifierFolder) {
    _identifierFolder = identifierFolder;
  }

  public String getIdentifierFolder() {
    return _identifierFolder;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  private File getFolder(final String base, final String folder) {
    return new File(new File(base), folder);
  }

  @Override
  protected BerkeleyDBIdentifierMap createObject() {
    final File identifier = getFolder(getIdentifierBaseFolder(), getIdentifierFolder());
    final Environment identifierEnvironment = BerkeleyDBViewComputationCacheSource.constructDatabaseEnvironment(identifier, true);
    return new BerkeleyDBIdentifierMap(identifierEnvironment, getFudgeContext());
  }
}
