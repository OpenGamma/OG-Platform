/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.db.PagingRequest;

/**
 * An {@code ExchangeSource} implemented using an underlying {@code ExchangeMaster}.
 * <p>
 * The {@link ExchangeSource} interface provides exchanges to the application via a narrow API.
 * This class provides the source on top of a standard {@link ExchangeMaster}.
 */
@PublicSPI
public class MasterExchangeSource extends AbstractMasterSource<ExchangeDocument, ExchangeMaster> implements ExchangeSource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   */
  public MasterExchangeSource(final ExchangeMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   * 
   * @param master  the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterExchangeSource(final ExchangeMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableExchange getExchange(UniqueId uniqueId) {
    ExchangeDocument doc = getDocument(uniqueId);
    return (doc != null ? doc.getExchange() : null);
  }

  @Override
  public ManageableExchange getSingleExchange(Identifier identifier) {
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest(identifier);
    searchRequest.setPagingRequest(PagingRequest.ONE);
    searchRequest.setVersionCorrection(getVersionCorrection());
    return getMaster().search(searchRequest).getFirstExchange();
  }

  @Override
  public ManageableExchange getSingleExchange(IdentifierBundle identifiers) {
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest(identifiers);
    searchRequest.setPagingRequest(PagingRequest.ONE);
    searchRequest.setVersionCorrection(getVersionCorrection());
    return getMaster().search(searchRequest).getFirstExchange();
  }

}
