/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;

/**
 * A source of {@link DefaultFudgeMessageStore} objects backed by those created by a
 * {@link BinaryDataStoreFactory}.
 */
public class DefaultFudgeMessageStoreFactory implements FudgeMessageStoreFactory {

  private final BinaryDataStoreFactory _binaryData;
  private final FudgeContext _fudgeContext;

  public DefaultFudgeMessageStoreFactory(final BinaryDataStoreFactory binaryData, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(binaryData, "binaryData");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _binaryData = binaryData;
    _fudgeContext = fudgeContext;
  }

  protected BinaryDataStoreFactory getBinaryData() {
    return _binaryData;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public FudgeMessageStore createMessageStore(ViewComputationCacheKey cacheKey) {
    return new DefaultFudgeMessageStore(getBinaryData().createDataStore(cacheKey), getFudgeContext());
  }

}
