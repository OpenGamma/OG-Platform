/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * An abstract source providing remote access.
 * 
 * @param <T> the type returned by the source
 */
@PublicSPI
public abstract class AbstractRemoteSource<T>
    extends AbstractRemoteClient
    implements Source<T> {

  /**
   * Creates an instance.
   * 
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public AbstractRemoteSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<UniqueId, T> get(Collection<UniqueId> uniqueIds) {
    return AbstractSource.get(this, uniqueIds);
  }

  @Override
  public Map<ObjectId, T> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return AbstractSource.get(this, objectIds, versionCorrection);
  }

}
