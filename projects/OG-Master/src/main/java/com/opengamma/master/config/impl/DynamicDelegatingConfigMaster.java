/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

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
 * A Config master that uses the scheme of the unique identifier to determine which
 * underlying master should handle the request.
 * <p/>
 * The underlying masters, or delegates, can be registered or deregistered at run time.
 * By default there is an {@link InMemoryConfigMaster} that will be used if specific scheme/delegate
 * combinations have not been registered.
 * <p/>
 * Change events are aggregated from the different masters and presented through a single change manager.
 * <p/>
 * The {@link #register(String, ConfigMaster)}, {@link #deregister(String)} and
 * {@link #add(String, ConfigDocument)} methods are public API outside
 * of the normal Master interface. Therefore to properly use this class the caller must have
 * a concrete instance of this class and use these methods to properly initialize the delegates
 * as well as clean up resources when a delegate is no longer needed. But the engine itself will
 * be able to interact with the component via standard Master interface.
 */
public class DynamicDelegatingConfigMaster implements ConfigMaster {

  /** The change manager. Aggregates among all the delegates */
  private final AggregatingChangeManager _changeManager;

  /**
   * The default delegate. Should never have data in it. If user ask for data with an unregistered scheme,
   * this empty master will be used
   */
  private final InMemoryConfigMaster _defaultEmptyDelegate;

  /** Delegator for maintaining map from scheme to master */
  private final UniqueIdSchemeDelegator<ConfigMaster> _delegator;

  public DynamicDelegatingConfigMaster() {
    _changeManager = new AggregatingChangeManager();
    _defaultEmptyDelegate = new InMemoryConfigMaster();
    _delegator = new UniqueIdSchemeDelegator<ConfigMaster>(_defaultEmptyDelegate);
    _changeManager.addChangeManager(_defaultEmptyDelegate.changeManager());
  }

  /**
   * Registers a scheme and delegate pair.
   * <p/>
   * The caller is responsible for creating a delegate and registering it before making calls
   * to the DynamicDelegatingConfigMaster
   *
   * @param scheme the external scheme associated with this delegate master, not null
   * @param delegate the master to be used for this scheme, not null
   */
  public void register(final String scheme, final ConfigMaster delegate) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(delegate, "delegate");
    _changeManager.addChangeManager(delegate.changeManager());
    _delegator.registerDelegate(scheme, delegate);
  }

  /**
   * Deregisters a scheme and delegate pair.
   * <p/>
   * The caller is responsible for deregistering a delegate when it is no longer needed.
   * For example, if delegates are made up of InMemoryMasters and data is no longer needed,
   * call deregister will free up memory
   *
   * @param scheme the external scheme associated with the delegate master to be removed, not null
   */
  public void deregister(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _changeManager.removeChangeManager(chooseDelegate(scheme).changeManager());
    _delegator.removeDelegate(scheme);
  }

  public ConfigDocument add(final String scheme, final ConfigDocument document) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(scheme).add(document);
  }

  private ConfigMaster chooseDelegate(final String scheme) {
    return _delegator.chooseDelegate(scheme);
  }

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
  public Map<UniqueId, ConfigDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, ConfigDocument> resultMap = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      ConfigDocument doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

  @Override
  public ConfigDocument add(ConfigDocument document) {
    throw new UnsupportedOperationException("Cannot add document without explicitly specifying the scheme");
  }

  @Override
  public ConfigDocument update(ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    Validate.notNull(document.getUniqueId(), "document has no unique id");
    Validate.notNull(document.getObjectId(), "document has no object id");
    return chooseDelegate(document.getObjectId().getScheme()).update(document);
  }

  @Override
  public void remove(ObjectIdentifiable oid) {
    ArgumentChecker.notNull(oid, "objectIdentifiable");
    chooseDelegate(oid.getObjectId().getScheme()).remove(oid);
  }

  @Override
  public ConfigDocument correct(ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).correct(document);
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
  public UniqueId addVersion(ObjectIdentifiable objectId, ConfigDocument documentToAdd) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(documentToAdd, "documentToAdd");
    return chooseDelegate(objectId.getObjectId().getScheme()).addVersion(objectId, documentToAdd);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public <R> ConfigSearchResult<R> search(final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    Iterable<ConfigSearchResult<R>> delegateResults = transform(_delegator.getAllDelegates(), new Function<ConfigMaster, ConfigSearchResult<R>>() {
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
    throw new UnsupportedOperationException("metaData() not supported on DynamicDelegatingConfigMaster");
  }
}
