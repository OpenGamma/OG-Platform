/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * A security master that uses the scheme of the unique identifier to determine which
 * underlying master should handle the request.
 * <p/>
 * The underlying masters, or delegates, can be registered or deregistered at run time.
 * By default there is an {@link InMemorySecurityMaster} that will be used if specific scheme/delegate
 * combinations have not been registered.
 * <p/>
 * Change events are aggregated from the different masters and presented through a single change manager.
 * <p/>
 * The {@link #register(String, SecurityMaster)}, {@link #deregister(String)} and
 * {@link #add(String, SecurityDocument)} methods are public API outside
 * of the normal Master interface. Therefore to properly use this class the caller must have
 * a concrete instance of this class and use these methods to properly initialize the delegates
 * as well as clean up resources when a delegate is no longer needed. But the engine itself will
 * be able to interact with the component via standard Master interface.
 */
public class DynamicDelegatingSecurityMaster implements SecurityMaster {

  /** The change manager. Aggregates among all the delegates */
  private final AggregatingChangeManager _changeManager;

  /**
   * The default delegate. Should never have data in it. If user ask for data with an unregistered scheme,
   * this empty master will be used
   */
  private final InMemorySecurityMaster _defaultEmptyDelegate;

  /** Delegator for maintaining map from scheme to master */
  private final UniqueIdSchemeDelegator<SecurityMaster> _delegator;

  public DynamicDelegatingSecurityMaster() {
    _changeManager = new AggregatingChangeManager();
    _defaultEmptyDelegate = new InMemorySecurityMaster();
    _delegator = new UniqueIdSchemeDelegator<SecurityMaster>(_defaultEmptyDelegate);
    _changeManager.addChangeManager(_defaultEmptyDelegate.changeManager());
  }

  /**
   * Registers a scheme and delegate pair.
   * <p/>
   * The caller is responsible for creating a delegate and registering it before making calls
   * to the DynamicDelegatingSecurityMaster
   *
   * @param scheme the external scheme associated with this delegate master, not null
   * @param delegate the master to be used for this scheme, not null
   */
  public void register(final String scheme, final SecurityMaster delegate) {
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

  private SecurityMaster chooseDelegate(final String scheme) {
    return _delegator.chooseDelegate(scheme);
  }

  public SecurityDocument add(final String scheme, final SecurityDocument document) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(scheme).add(document);
  }

  @Override
  public SecurityMetaDataResult metaData(SecurityMetaDataRequest request) {
    // result the union of all discovered security types among all the delegates.
    // we never populate the schema version because that is a single value in the result
    // and there is no way to take union of multiple delegates
    List<String> securityTypes = new ArrayList<>();
    for (SecurityMaster delegate : _delegator.getDelegates().values()) {
      SecurityMetaDataResult meta = delegate.metaData(request);
      for (String securityType : meta.getSecurityTypes()) {
        if (!securityTypes.contains(securityType)) {
          securityTypes.add(securityType);
        }
      }
    }
    SecurityMetaDataResult result = new SecurityMetaDataResult();
    result.setSecurityTypes(securityTypes);
    return result;
  }

  @Override
  public SecuritySearchResult search(SecuritySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    Collection<ObjectId> ids = request.getObjectIds();
    return chooseDelegate(ids.iterator().next().getScheme()).search(request);
  }

  @Override
  public SecurityHistoryResult history(SecurityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    return chooseDelegate(request.getObjectId().getScheme()).history(request);
  }

  @Override
  public SecurityDocument get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public SecurityDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getObjectId().getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, SecurityDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, SecurityDocument> resultMap = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      SecurityDocument doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

  @Override
  public SecurityDocument add(SecurityDocument document) {
    throw new UnsupportedOperationException("Cannot add document without explicitly specifying the scheme");
  }

  @Override
  public SecurityDocument update(SecurityDocument document) {
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
  public SecurityDocument correct(SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(uniqueId.getScheme()).replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(SecurityDocument replacementDocument) {
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
  public UniqueId addVersion(ObjectIdentifiable objectId, SecurityDocument documentToAdd) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(documentToAdd, "documentToAdd");
    return chooseDelegate(objectId.getObjectId().getScheme()).addVersion(objectId, documentToAdd);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }
}
