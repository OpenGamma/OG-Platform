/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import static com.google.common.collect.Maps.newHashMap;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Source;
import com.opengamma.core.region.Region;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * An abstract source built on top of an underlying master.
 *
 * @param <D>  the type of the document
 * @param <M>  the type of the master
 */
@PublicSPI
public abstract class AbstractRemoteSource<T> extends AbstractRemoteClient implements Source<T> {


  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public AbstractRemoteSource(final URI baseUri) {
    super(baseUri);
  }

  @Override
  public Map<UniqueId, T> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, T> result = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      try {
        T object = get(uniqueId);
        result.put(uniqueId, object);
      } catch (DataNotFoundException ex) {
        // do nothing
      }
    }
    return result;
  }
}
