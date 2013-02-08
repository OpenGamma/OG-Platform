/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.orgs.ManageableOrganisation;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationHistoryRequest;
import com.opengamma.master.orgs.OrganisationHistoryResult;
import com.opengamma.master.orgs.OrganisationMaster;
import com.opengamma.master.orgs.OrganisationSearchRequest;
import com.opengamma.master.orgs.OrganisationSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import org.joda.beans.JodaBeanUtils;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * An in-memory implementation of a organisation master.
 */
public class InMemoryOrganisationMaster extends SimpleAbstractInMemoryMaster<OrganisationDocument> implements OrganisationMaster {

  /**
   * The default scheme used for each {@link com.opengamma.id.UniqueId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemOrg";


  /**
   * Creates an instance.
   */
  public InMemoryOrganisationMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager the change manager, not null
   */
  public InMemoryOrganisationMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier the supplier of object identifiers, not null
   */
  public InMemoryOrganisationMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier the supplier of object identifiers, not null
   * @param changeManager    the change manager, not null
   */
  public InMemoryOrganisationMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void validateDocument(OrganisationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganisation(), "document.organisation");
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationDocument get(UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  @Override
  public OrganisationDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final OrganisationDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Organisation not found: " + objectId);
    }
    return cloneOrganisationDocument(document);
  }

  private OrganisationDocument cloneOrganisationDocument(OrganisationDocument document) {
    OrganisationDocument clone = JodaBeanUtils.clone(document);
    ManageableOrganisation organisationClone = JodaBeanUtils.clone(document.getOrganisation());
    clone.setOrganisation(organisationClone);
    return clone;
  }

  private ManageableOrganisation cloneOrganisation(ManageableOrganisation organisation) {
    return JodaBeanUtils.clone(organisation);
  }

  @Override
  public OrganisationDocument add(OrganisationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganisation(), "document.organisation");

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();

    final OrganisationDocument clonedDoc = cloneOrganisationDocument(document);
    setDocumentId(document, clonedDoc, uniqueId);
    setVersionTimes(document, clonedDoc, now, null, now, null);
    _store.put(objectId, clonedDoc);

    _changeManager.entityChanged(ChangeType.ADDED, objectId, document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  private void setDocumentId(final OrganisationDocument document, final OrganisationDocument clonedDoc, final UniqueId uniqueId) {
    document.getOrganisation().setUniqueId(uniqueId);
    clonedDoc.getOrganisation().setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    clonedDoc.setUniqueId(uniqueId);
  }

  private void setVersionTimes(OrganisationDocument document, final OrganisationDocument clonedDoc,
                               final Instant versionFromInstant, final Instant versionToInstant, final Instant correctionFromInstant, final Instant correctionToInstant) {

    clonedDoc.setVersionFromInstant(versionFromInstant);
    document.setVersionFromInstant(versionFromInstant);

    clonedDoc.setVersionToInstant(versionToInstant);
    document.setVersionToInstant(versionToInstant);

    clonedDoc.setCorrectionFromInstant(correctionFromInstant);
    document.setCorrectionFromInstant(correctionFromInstant);

    clonedDoc.setCorrectionToInstant(correctionToInstant);
    document.setCorrectionToInstant(correctionToInstant);
  }

  @Override
  public OrganisationDocument update(OrganisationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getOrganisation(), "document.organisation");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final OrganisationDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Organisation not found: " + uniqueId);
    }

    final OrganisationDocument clonedDoc = cloneOrganisationDocument(document);


    setVersionTimes(document, clonedDoc, now, null, now, null);

    if (_store.replace(uniqueId.getObjectId(), storedDocument, clonedDoc) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }


  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    OrganisationDocument storedDocument = _store.remove(objectIdentifiable.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Organisation not found " + objectIdentifiable);
    }    
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  @Override
  public OrganisationDocument correct(OrganisationDocument document) {
    return update(document);
  }

  @Override
  public OrganisationSearchResult search(OrganisationSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<OrganisationDocument> list = new ArrayList<OrganisationDocument>();
    for (OrganisationDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(cloneOrganisationDocument(doc));
      }
    }
    final OrganisationSearchResult result = new OrganisationSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  @Override
  public OrganisationHistoryResult history(OrganisationHistoryRequest request) {
    throw new UnsupportedOperationException("History request not supported by " + getClass().getSimpleName());
  }

  @Override
  public ManageableOrganisation getOrganisation(UniqueId organisationId) {
    ArgumentChecker.notNull(organisationId, "organisationId");
    ManageableOrganisation organisation = _store.get(organisationId.getObjectId()).getOrganisation();
    if (organisation == null) {
      throw new DataNotFoundException("Organisation not found: " + organisationId);
    }
    return cloneOrganisation(organisation);
  }
}
