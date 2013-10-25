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
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import org.joda.beans.JodaBeanUtils;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * An in-memory implementation of a organization master.
 */
public class InMemoryOrganizationMaster extends SimpleAbstractInMemoryMaster<OrganizationDocument> implements OrganizationMaster {

  /**
   * The default scheme used for each {@link com.opengamma.id.UniqueId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemOrg";


  /**
   * Creates an instance.
   */
  public InMemoryOrganizationMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager the change manager, not null
   */
  public InMemoryOrganizationMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier the supplier of object identifiers, not null
   */
  public InMemoryOrganizationMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier the supplier of object identifiers, not null
   * @param changeManager    the change manager, not null
   */
  public InMemoryOrganizationMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void validateDocument(OrganizationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganization(), "document.organization");
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationDocument get(UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  @Override
  public OrganizationDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final OrganizationDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Organization not found: " + objectId);
    }
    return cloneOrganizationDocument(document);
  }

  private OrganizationDocument cloneOrganizationDocument(OrganizationDocument document) {
    if (isCloneResults()) {
      OrganizationDocument clone = JodaBeanUtils.clone(document);
      ManageableOrganization organizationClone = JodaBeanUtils.clone(document.getOrganization());
      clone.setOrganization(organizationClone);
      return clone;
    } else {
      return document;
    }
  }

  private ManageableOrganization cloneOrganization(ManageableOrganization organization) {
    if (isCloneResults()) {
      return JodaBeanUtils.clone(organization);
    } else {
      return organization;
    }
  }

  @Override
  public OrganizationDocument add(OrganizationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getOrganization(), "document.organization");

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();

    final OrganizationDocument clonedDoc = cloneOrganizationDocument(document);
    setDocumentId(document, clonedDoc, uniqueId);
    setVersionTimes(document, clonedDoc, now, null, now, null);
    _store.put(objectId, clonedDoc);

    _changeManager.entityChanged(ChangeType.ADDED, objectId, document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  private void setDocumentId(final OrganizationDocument document, final OrganizationDocument clonedDoc, final UniqueId uniqueId) {
    document.getOrganization().setUniqueId(uniqueId);
    clonedDoc.getOrganization().setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    clonedDoc.setUniqueId(uniqueId);
  }

  private void setVersionTimes(OrganizationDocument document, final OrganizationDocument clonedDoc,
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
  public OrganizationDocument update(OrganizationDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getOrganization(), "document.organization");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final OrganizationDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Organization not found: " + uniqueId);
    }

    final OrganizationDocument clonedDoc = cloneOrganizationDocument(document);


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
    OrganizationDocument storedDocument = _store.remove(objectIdentifiable.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Organization not found " + objectIdentifiable);
    }    
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  @Override
  public OrganizationDocument correct(OrganizationDocument document) {
    return update(document);
  }

  @Override
  public OrganizationSearchResult search(OrganizationSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<OrganizationDocument> list = new ArrayList<OrganizationDocument>();
    for (OrganizationDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(cloneOrganizationDocument(doc));
      }
    }
    final OrganizationSearchResult result = new OrganizationSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  @Override
  public OrganizationHistoryResult history(OrganizationHistoryRequest request) {
    throw new UnsupportedOperationException("History request not supported by " + getClass().getSimpleName());
  }

  @Override
  public ManageableOrganization getOrganization(UniqueId organizationId) {
    ArgumentChecker.notNull(organizationId, "organizationId");
    ManageableOrganization organization = _store.get(organizationId.getObjectId()).getOrganization();
    if (organization == null) {
      throw new DataNotFoundException("Organization not found: " + organizationId);
    }
    return cloneOrganization(organization);
  }
}
