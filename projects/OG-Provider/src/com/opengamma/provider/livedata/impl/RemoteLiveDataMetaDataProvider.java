/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.livedata.impl;

import java.net.URI;

import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderRequest;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote live data provider.
 * <p>
 * This is a client that connects to a live data provider at a remote URI.
 */
public class RemoteLiveDataMetaDataProvider extends AbstractRemoteClient implements LiveDataMetaDataProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteLiveDataMetaDataProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public LiveDataMetaData metaData() {
    return metaData(new LiveDataMetaDataProviderRequest()).getMetaData();
  }

  @Override
  public LiveDataMetaDataProviderResult metaData(LiveDataMetaDataProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    URI uri = DataLiveDataMetaDataProviderResource.uriMetaData(getBaseUri(), request);
    return accessRemote(uri).get(LiveDataMetaDataProviderResult.class);
  }

}
