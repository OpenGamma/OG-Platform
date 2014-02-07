/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;

/**
 * Abstract base class for remote masters.
 * <p>
 * A remote master provides a client-side view of a remote master over REST.
 * 
 * @param <D>  the type of the document
 */
public abstract class AbstractRemoteDocumentMaster<D extends AbstractDocument>
    extends AbstractRemoteMaster
    implements AbstractMaster<D> {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public AbstractRemoteDocumentMaster(final URI baseUri) {
    super(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public AbstractRemoteDocumentMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public final UniqueId addVersion(ObjectIdentifiable objectId, D documentToAdd) {
    List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public final void removeVersion(final UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<D>emptyList());
  }

  @Override
  public final UniqueId replaceVersion(D replacementDocument) {
    List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public Map<UniqueId, D> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, D> resultMap = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      D doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

}
