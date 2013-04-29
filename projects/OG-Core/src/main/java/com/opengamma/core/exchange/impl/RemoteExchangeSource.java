/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.exchange.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link ExchangeSource}.
 */
public class RemoteExchangeSource extends AbstractRemoteSource<Exchange> implements ExchangeSource {

  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteExchangeSource(final URI baseUri) {
    this(baseUri, DummyChangeManager.INSTANCE);
  }

  public RemoteExchangeSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Exchange get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    URI uri = DataExchangeSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Exchange.class);
  }

  @Override
  public Exchange get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataExchangeSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Exchange.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Exchange> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataExchangeSourceResource.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  //-------------------------------------------------------------------------
  @Override
  public Exchange getSingle(final ExternalId identifier) {
    try {
      return getSingle(ExternalIdBundle.of(identifier));
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Exchange getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    try {
      URI uri = DataExchangeSourceResource.uriSearchSingle(getBaseUri(), bundle);
      return accessRemote(uri).get(Exchange.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public Map<ExternalIdBundle, Collection<Exchange>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  @Override
  public Collection<Exchange> get(ExternalIdBundle bundle) {
    return AbstractSourceWithExternalBundle.get(this, bundle);
  }

  @Override
  public Exchange getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundle, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, Exchange> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

}
