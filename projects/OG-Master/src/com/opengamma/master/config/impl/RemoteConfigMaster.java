/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.*;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link ConfigMaster}.
 */
public class RemoteConfigMaster extends AbstractRemoteMaster implements ConfigMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteConfigMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteConfigMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataConfigMasterResource.uriMetaData(getBaseUri(), request);
    return accessRemote(uri).get(ConfigMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigSearchResult<T> search(final ConfigSearchRequest<T> request) {
    ArgumentChecker.notNull(request, "request");

    URI uri = DataConfigMasterResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(ConfigSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<?> get(final UniqueId uniqueId) {
    return get(uniqueId, (Class<?>) null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(UniqueId uniqueId, Class<T> clazz) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      URI uri = DataConfigResource.uriVersion(getBaseUri(), uniqueId, clazz);
      return accessRemote(uri).get(ConfigDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST, null);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<?> get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return get(objectId, versionCorrection, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection, Class<T> clazz) {
    ArgumentChecker.notNull(objectId, "objectId");

    URI uri = DataConfigResource.uri(getBaseUri(), objectId, versionCorrection, clazz);
    return accessRemote(uri).get(ConfigDocument.class);
  }


  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> add(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getValue(), "document.config");

    URI uri = DataConfigMasterResource.uriAdd(getBaseUri());
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> update(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getValue(), "document.config");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = DataConfigResource.uri(getBaseUri(), document.getUniqueId(), null, null);
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    URI uri = DataConfigResource.uri(getBaseUri(), uniqueId, null, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigHistoryResult<T> history(final ConfigHistoryRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    URI uri = DataConfigResource.uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(ConfigHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> correct(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getValue(), "document.config");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    URI uri = DataConfigResource.uriVersion(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(ConfigDocument.class, document);
  }


  @Override
  public <T> List<UniqueId> replaceVersion(UniqueId uniqueId, List<ConfigDocument<T>> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConfigDocument<T> replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument.getName(), "document.name");
      ArgumentChecker.notNull(replacementDocument.getValue(), "document.value");
    }

    URI uri = DataConfigResource.uriVersion(getBaseUri(), uniqueId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public <T> List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<ConfigDocument<T>> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConfigDocument<T> replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument.getName(), "document.name");
      ArgumentChecker.notNull(replacementDocument.getValue(), "document.value");
    }

    URI uri = DataConfigResource.uriAll(getBaseUri(), objectId, null, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public <T> List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<ConfigDocument<T>> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (ConfigDocument<T> replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument.getName(), "document.name");
      ArgumentChecker.notNull(replacementDocument.getValue(), "document.value");
    }

    URI uri = DataConfigResource.uri(getBaseUri(), objectId, null, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public <T> UniqueId addVersion(ObjectIdentifiable objectId, ConfigDocument<T> documentToAdd) {
    List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }


  @Override
  public <T> UniqueId replaceVersion(ConfigDocument<T> replacementDocument) {
    List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.<ConfigDocument<T>>singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public <T> void removeVersion(UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<ConfigDocument<T>>emptyList());
  }
}
