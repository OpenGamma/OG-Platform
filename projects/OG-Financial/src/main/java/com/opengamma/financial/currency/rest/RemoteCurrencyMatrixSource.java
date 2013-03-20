/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link CurrencyMatrixSource}.
 */
public class RemoteCurrencyMatrixSource extends AbstractRemoteClient implements CurrencyMatrixSource {

  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   * 
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteCurrencyMatrixSource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  public RemoteCurrencyMatrixSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    _changeManager = changeManager;
  }

  // CurrencyMatrixSource

  @Override
  public CurrencyMatrix getCurrencyMatrix(String name, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    try {
      URI uri = DataCurrencyMatrixSourceResource.uriGetMatrix(getBaseUri(), name, versionCorrection);
      return accessRemote(uri).get(CurrencyMatrix.class);
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  @Override
  public CurrencyMatrix get(UniqueId identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    URI uri = DataCurrencyMatrixSourceResource.uriGetMatrix(getBaseUri(), identifier);
    return accessRemote(uri).get(CurrencyMatrix.class);
  }

  @Override
  public CurrencyMatrix get(ObjectId identifier, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(identifier, "identifier");
    URI uri = DataCurrencyMatrixSourceResource.uriGetMatrix(getBaseUri(), identifier, versionCorrection);
    return accessRemote(uri).get(CurrencyMatrix.class);
  }

  @Override
  public Map<UniqueId, CurrencyMatrix> get(final Collection<UniqueId> identifiers) {
    return AbstractSource.get(this, identifiers);
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
