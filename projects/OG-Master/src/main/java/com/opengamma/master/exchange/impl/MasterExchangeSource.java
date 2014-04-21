/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.paging.PagingRequest;

/**
 * An {@code ExchangeSource} implemented using an underlying {@code ExchangeMaster}.
 * <p>
 * The {@link ExchangeSource} interface provides exchanges to the application via a narrow API. This class provides the source on top of a standard {@link ExchangeMaster}.
 */
@PublicSPI
public class MasterExchangeSource extends AbstractMasterSource<Exchange, ExchangeDocument, ExchangeMaster> implements ExchangeSource {

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   */
  public MasterExchangeSource(final ExchangeMaster master) {
    super(master);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Collection<Exchange> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest(bundle);
    searchRequest.setVersionCorrection(versionCorrection);
    return (List) getMaster().search(searchRequest).getExchanges();
  }

  @Override
  public ManageableExchange getSingle(ExternalId identifier) {
    return getSingle(identifier.toBundle());
  }

  @Override
  public ManageableExchange getSingle(ExternalIdBundle identifiers) {
    return getSingle(identifiers, VersionCorrection.LATEST);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Exchange>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  @Override
  public Collection<Exchange> get(ExternalIdBundle bundle) {
    return get(bundle, VersionCorrection.LATEST);
  }

  @Override
  public ManageableExchange getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest(bundle);
    searchRequest.setPagingRequest(PagingRequest.ONE);
    searchRequest.setVersionCorrection(versionCorrection);
    return getMaster().search(searchRequest).getFirstExchange();
  }

  @Override
  public Map<ExternalIdBundle, Exchange> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

}
