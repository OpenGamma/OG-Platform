/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.exchange.impl;

import java.net.URI;
import java.util.Collection;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to an {@link ExchangeSource}.
 */
public class RemoteExchangeSource extends AbstractRemoteClient implements ExchangeSource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteExchangeSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public Exchange getExchange(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataExchangeSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Exchange.class);
  }

  @Override
  public Exchange getExchange(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    URI uri = DataExchangeSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Exchange.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<? extends Exchange> getExchanges(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    URI uri = DataExchangeSourceResource.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  //-------------------------------------------------------------------------
  @Override
  public Exchange getSingleExchange(final ExternalId identifier) {
    return getSingleExchange(ExternalIdBundle.of(identifier));
  }

  @Override
  public Exchange getSingleExchange(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    
    URI uri = DataExchangeSourceResource.uriSearchSingle(getBaseUri(), bundle);
    return accessRemote(uri).get(Exchange.class);
  }

}
