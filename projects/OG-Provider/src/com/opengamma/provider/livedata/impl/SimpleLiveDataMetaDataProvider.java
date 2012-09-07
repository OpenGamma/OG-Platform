/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.livedata.impl;

import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderRequest;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple live data provider that has a fixed set of meta data.
 * <p>
 * This provider has no access to the underlying server.
 */
public class SimpleLiveDataMetaDataProvider implements LiveDataMetaDataProvider {

  /**
   * The result to send back.
   */
  private final LiveDataMetaData _metaData;

  /**
   * Creates an instance.
   * 
   * @param metaData  the meta-data, not null
   */
  public SimpleLiveDataMetaDataProvider(final LiveDataMetaData metaData) {
    _metaData = metaData;
  }

  //-------------------------------------------------------------------------
  @Override
  public LiveDataMetaData metaData() {
    return _metaData;
  }

  @Override
  public LiveDataMetaDataProviderResult metaData(LiveDataMetaDataProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    return new LiveDataMetaDataProviderResult(_metaData);
  }

}
