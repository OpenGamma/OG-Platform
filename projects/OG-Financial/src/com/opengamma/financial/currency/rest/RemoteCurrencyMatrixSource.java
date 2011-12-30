/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import java.net.URI;

import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link CurrencyMatrixSource}.
 */
public class RemoteCurrencyMatrixSource extends AbstractRemoteClient implements CurrencyMatrixSource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteCurrencyMatrixSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public CurrencyMatrix getCurrencyMatrix(String name) {
    ArgumentChecker.notNull(name, "name");
    
    URI uri = DataCurrencyMatrixSourceResource.uriGetMatrix(getBaseUri(), name);
    return accessRemote(uri).get(CurrencyMatrix.class);
  }

}
