/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import static com.google.common.collect.Maps.newHashMap;
import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple, abstract in-memory master.
 * <p>
 * This master does not support versioning nor corrections.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 *
 * @param <D>  the type of the document
 */
public abstract class SimpleAbstractInMemoryMaster<D extends AbstractDocument>
    implements AbstractMaster<D>, ChangeProvider {

  /**
   * A cache of documents by identifier.
   */
  protected final ConcurrentMap<ObjectId, D> _store = new ConcurrentHashMap<ObjectId, D>();  // CSIGNORE
  /**
   * The supplied of identifiers.
   */
  protected final Supplier<ObjectId> _objectIdSupplier;  // CSIGNORE
  /**
   * The change manager.
   */
  protected final ChangeManager _changeManager;  // CSIGNORE

  /**
   * Whether all documents should be cloned on return. True by default.
   */
  private boolean _cloneResults = true;

  
  /**
   * Creates an instance.
   * 
   * @param defaultOidScheme  the default object identifier scheme, not null
   */
  public SimpleAbstractInMemoryMaster(String defaultOidScheme) {
    this(new ObjectIdSupplier(defaultOidScheme));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param defaultOidScheme  the default object identifier scheme, not null
   * @param changeManager  the change manager, not null
   */
  public SimpleAbstractInMemoryMaster(String defaultOidScheme, final ChangeManager changeManager) {
    this(new ObjectIdSupplier(defaultOidScheme), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public SimpleAbstractInMemoryMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public SimpleAbstractInMemoryMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  /**
   * Whether to clone all results when searching. True by default.
   * 
   * @return whether results are cloned.
   */
  public boolean isCloneResults() {
    return _cloneResults;
  }

  /**
   * Specify whether to clone all results when searching. True by default.
   *
   * @param cloneResults whether to clone results when searching.
   */
  public void setCloneResults(boolean cloneResults) {
    _cloneResults = cloneResults;
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the specified document.
   * 
   * @param document  the document to validate, null to be validated
   */
  protected abstract void validateDocument(D document);

  @Override
  public final ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public final List<UniqueId> replaceVersions(ObjectIdentifiable objectIdentifiable, List<D> replacementDocuments) {
    return replaceAllVersions(objectIdentifiable.getObjectId(), replacementDocuments);
  }

  @Override
  public final List<UniqueId> replaceVersion(UniqueId uniqueId, List<D> replacementDocuments) {
    return replaceAllVersions(uniqueId.getObjectId(), replacementDocuments);
  }

  @Override
  public final List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<D> replacementDocuments) {
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    ArgumentChecker.notNull(objectId, "objectId");
    for (D replacementDocument : replacementDocuments) {
      validateDocument(replacementDocument);
    }
    final Instant now = Instant.now();
    ArgumentChecker.isTrue(MasterUtils.checkUniqueVersionsFrom(replacementDocuments), "No two versioned documents may have the same \"version from\" instant");

    final D storedDocument = _store.get(objectId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Document not found: " + objectId.getObjectId());
    }

    if (replacementDocuments.isEmpty()) {
      _store.remove(objectId.getObjectId());
      _changeManager.entityChanged(ChangeType.REMOVED, objectId.getObjectId(), null, null, now);
      return Collections.emptyList();
      
    } else {
      Instant storedVersionFrom = storedDocument.getVersionFromInstant();
      Instant storedVersionTo = storedDocument.getVersionToInstant();
      
      List<D> orderedReplacementDocuments = MasterUtils.adjustVersionInstants(now, storedVersionFrom, storedVersionTo, replacementDocuments);
      D lastReplacementDocument = orderedReplacementDocuments.get(orderedReplacementDocuments.size() - 1);
      if (_store.replace(objectId.getObjectId(), storedDocument, lastReplacementDocument) == false) {
        throw new IllegalArgumentException("Concurrent modification");
      }
      
      Instant versionFromInstant = functional(orderedReplacementDocuments).first().getVersionFromInstant();
      Instant versionToInstant = functional(orderedReplacementDocuments).last().getVersionToInstant();
      changeManager().entityChanged(ChangeType.CHANGED, objectId.getObjectId(), versionFromInstant, versionToInstant, now);
      
      updateCaches(objectId, lastReplacementDocument);
      
      return ImmutableList.of(lastReplacementDocument.getUniqueId());
    }
  }
  
  /**
   * Subclasses that support additional caching should override this method.
   * 
   * @param replacedObject The version removed (possibly null)
   * @param updatedDocument  The version added (possibly null)
   */
  protected void updateCaches(ObjectIdentifiable replacedObject, D updatedDocument) {
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

  //-------------------------------------------------------------------------
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
