/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget.rest;

import java.net.URI;

import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.id.UniqueId;

/**
 * Provides remote access to a {@link TempTargetRepository}. Repository use can be high during graph construction when it is used to collapse targets. A caching layer on top of this to match
 * previously registered targets will usually be beneficial.
 */
public class RemoteTempTargetRepository extends RemoteTempTargetSource implements TempTargetRepository {

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteTempTargetRepository(final URI baseUri) {
    super(baseUri);
  }

  // TempTargetRepository

  @Override
  public UniqueId locateOrStore(final TempTarget target) {
    final URI uri = DataTempTargetRepositoryResource.uriLocateOrStore(getBaseUri());
    return accessRemote(uri).get(UniqueId.class);
  }

}
