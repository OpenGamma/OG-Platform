/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * A master of Configs that uses the scheme of the unique identifier to determine which
 * underlying master should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 * <p>
 * Change events are aggregated from the different masters and presented through a single change manager.
 */
public class DelegatingConfigMaster extends UniqueIdSchemeDelegator<ConfigMaster> implements ConfigMaster {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultMaster the master to use when no scheme matches, not null
   */
  public DelegatingConfigMaster(ConfigMaster defaultMaster) {
    super(defaultMaster);
    _changeManager = defaultMaster.changeManager();
  }

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultMaster the master to use when no scheme matches, not null
   * @param schemePrefixToMasterMap  the map of masters by scheme to switch on, not null
   */
  public DelegatingConfigMaster(ConfigMaster defaultMaster, Map<String, ConfigMaster> schemePrefixToMasterMap) {
    super(defaultMaster, schemePrefixToMasterMap);
    AggregatingChangeManager changeManager = new AggregatingChangeManager();
    
    // REVIEW jonathan 2012-08-03 -- this assumes that the delegating master lasts for the lifetime of the engine as we
    // never detach from the underlying change managers.
    changeManager.addChangeManager(defaultMaster.changeManager());
    for (ConfigMaster master : schemePrefixToMasterMap.values()) {
      changeManager.addChangeManager(master.changeManager());
    }
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public ConfigDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getObjectId().getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public ConfigDocument add(ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    return getDefaultDelegate().add(document);
  }

  @Override
  public ConfigDocument update(ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).update(document);
  }

  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    chooseDelegate(objectIdentifiable.getObjectId().getScheme()).remove(objectIdentifiable);
  }

  @Override
  public ConfigDocument correct(ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).correct(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, ConfigDocument documentToAdd) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(documentToAdd, "documentToAdd");
    return chooseDelegate(objectId.getObjectId().getScheme()).addVersion(objectId, documentToAdd);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(uniqueId.getScheme()).replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(ConfigDocument replacementDocument) {
    ArgumentChecker.notNull(replacementDocument, "replacementDocument");
    ArgumentChecker.notNull(replacementDocument.getObjectId(), "replacementDocument.getObjectId");
    return chooseDelegate(replacementDocument.getObjectId().getScheme()).replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    chooseDelegate(uniqueId.getScheme()).removeVersion(uniqueId);
  }
  
  
  @Override
  public Map<UniqueId, ConfigDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, ConfigDocument> resultMap = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      ConfigDocument doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

  @Override
  public <R> ConfigSearchResult<R> search(final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    Iterable<ConfigSearchResult<R>> delegateResults = transform(getAllDelegates(), new Function<ConfigMaster, ConfigSearchResult<R>>() {
      @Override
      public ConfigSearchResult<R> apply(ConfigMaster input) {
        return input.search(request);
      }
    });
    ConfigSearchResult<R> result = new ConfigSearchResult<R>();
    for (ConfigSearchResult<R> delegateResult: delegateResults) {
      result.getDocuments().addAll(delegateResult.getDocuments());
    }
    return result;
  }

  @Override
  public <R> ConfigHistoryResult<R> history(ConfigHistoryRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    ObjectId objectId = request.getObjectId();
    ArgumentChecker.notNull(objectId, "objectId");
    return chooseDelegate(objectId.getScheme()).history(request);
  }

  @Override
  public ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    throw new UnsupportedOperationException("metaData() not supported on DelegatingConfigMaster");
  }
  
  

}
