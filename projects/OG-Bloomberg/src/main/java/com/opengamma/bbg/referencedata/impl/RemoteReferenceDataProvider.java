/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote reference-data provider.
 * <p>
 * This is a client that connects to a reference-data provider at a remote URI.
 */
public class RemoteReferenceDataProvider extends AbstractRemoteClient implements ReferenceDataProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteReferenceDataProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  // delegate convenience methods to request/result method
  // code copied from AbstractReferenceDataProvider due to lack of multiple inheritance
  //-------------------------------------------------------------------------
  @Override
  public String getReferenceDataValue(String identifier, String dataField) {
    return getReferenceDataValues(Collections.singleton(identifier), dataField).get(identifier);
  }

  @Override
  public Map<String, String> getReferenceDataValues(String identifier, Iterable<String> dataFields) {
    Set<String> fields = ImmutableSet.copyOf(dataFields);  // copy to avoid implementation bugs
    Map<String, FudgeMsg> data = getReferenceData(Collections.singleton(identifier), dataFields);
    
    // extract field to value
    Map<String, String> map = Maps.newHashMap();
    FudgeMsg msg = data.get(identifier);
    if (msg != null) {
      for (String field : fields) {
        String value = msg.getString(field);
        if (value != null) {
          map.put(identifier, value);
        }
      }
    }
    return map;
  }

  @Override
  public Map<String, String> getReferenceDataValues(Iterable<String> identifiers, String dataField) {
    Map<String, FudgeMsg> data = getReferenceData(identifiers, Collections.singleton(dataField));
    
    // extract identifier to value
    Map<String, String> map = Maps.newHashMap();
    for (String identifier : data.keySet()) {
      String value = data.get(identifier).getString(dataField);
      if (value != null) {
        map.put(identifier, value);
      }
    }
    return map;
  }

  @Override
  public Map<String, FudgeMsg> getReferenceData(Iterable<String> identifiers, Iterable<String> dataFields) {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(identifiers, dataFields, true);
    return AbstractReferenceDataProvider.queryMap(request, this);
  }

  @Override
  public Map<String, FudgeMsg> getReferenceDataIgnoreCache(Iterable<String> identifiers, Iterable<String> dataFields) {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(identifiers, dataFields, false);
    return AbstractReferenceDataProvider.queryMap(request, this);
  }

  @Override
  public ReferenceDataProviderGetResult getReferenceData(ReferenceDataProviderGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    URI uri = DataReferenceDataProviderResource.uriGet(getBaseUri());
    return accessRemote(uri).post(ReferenceDataProviderGetResult.class, request);
  }

}
