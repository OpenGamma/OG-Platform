/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link ViewComputationCache} which backs value storage on
 * a pair of {@link ValueSpecificationIdentifierSource} and {@link ValueSpecificationIdentifierBinaryDataStore}.
 */
public class StandardViewComputationCache implements ViewComputationCache {
  private final ValueSpecificationIdentifierSource _identifierSource;
  private final ValueSpecificationIdentifierBinaryDataStore _dataStore;
  private final FudgeContext _fudgeContext;
  
  public StandardViewComputationCache(
      ValueSpecificationIdentifierSource identifierSource,
      ValueSpecificationIdentifierBinaryDataStore dataStore,
      FudgeContext fudgeContext) {
    ArgumentChecker.notNull(identifierSource, "Identifier Source");
    ArgumentChecker.notNull(dataStore, "Data Store");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _identifierSource = identifierSource;
    _dataStore = dataStore;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the identifierSource field.
   * @return the identifierSource
   */
  public ValueSpecificationIdentifierSource getIdentifierSource() {
    return _identifierSource;
  }

  /**
   * Gets the dataStore field.
   * @return the dataStore
   */
  public ValueSpecificationIdentifierBinaryDataStore getDataStore() {
    return _dataStore;
  }

  /**
   * Gets the fudgeContext field.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public Object getValue(ValueSpecification specification) {
    ArgumentChecker.notNull(specification, "Specification");
    long identifier = getIdentifierSource().getIdentifier(specification);
    byte[] data = getDataStore().get(identifier);
    if (data == null) {
      return null;
    }
    // TODO kirk 2010-08-07 -- Deserialize it.
    return null;
  }

  @Override
  public void putValue(ComputedValue value) {
    ArgumentChecker.notNull(value, "Computed value");
    long identifier = getIdentifierSource().getIdentifier(value.getSpecification());
    // TODO kirk 2010-08-07 -- Fix this with proper serialization.
    byte[] data = new byte[0];
    getDataStore().put(identifier, data);
  }

}
