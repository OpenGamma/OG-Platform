/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.opengamma.DataDuplicationException;
import com.opengamma.DataNotFoundException;
import com.opengamma.DataVersionException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.user.HistoryEvent;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract base providing a simple, in-memory master.
 * <p>
 * This master does not support versioning.
 */
abstract class AbstractInMemoryMaster<T extends UniqueIdentifiable & MutableUniqueIdentifiable> {
  // see UserMaster and RoleMaster for method descriptions

  /**
   * The map of object identifier by name.
   */
  private final ConcurrentMap<String, ObjectId> _names = new ConcurrentHashMap<>();
  /**
   * The main map of objects.
   */
  private final ConcurrentMap<ObjectId, T> _objects = new ConcurrentHashMap<>();
  /**
   * The object description for errors.
   */
  private final String _objectDescription;
  /**
   * The removed marker object.
   */
  private final T _removed;
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectId> _objectIdSupplier;
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param objectDescription  the object description for error messages, not null
   * @param removed  the placeholder object for removing, not null
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  AbstractInMemoryMaster(String objectDescription, T removed, Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectDescription, "objectDescription");
    ArgumentChecker.notNull(removed, "removed");
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectDescription = objectDescription;
    _removed = removed;
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the stored values, not to be altered.
   * 
   * @return the values, not null
   */
  Collection<T> getStoredValues() {
    return _objects.values();
  }

  /**
   * Gets the change manager.
   * 
   * @return the change manager, not null
   */
  ChangeManager getChangeManager() {
    return _changeManager;
  }

  /**
   * Extracts the name from the object.
   * 
   * @param object  the object, not null
   * @return the name, not null
   */
  abstract String extractName(T object);

  //-------------------------------------------------------------------------
  String caseInsensitive(String name) {
    return name.toLowerCase(Locale.ROOT);
  }

  @SuppressWarnings("unchecked")
  T clone(final T original) {
    return (T) JodaBeanUtils.clone((Bean) original);
  }

  boolean nameExists(String name) {
    ArgumentChecker.notNull(name, "name");
    return _names.containsKey(caseInsensitive(name));
  }

  T getByName(String name) {
    ArgumentChecker.notNull(name, "name");
    T stored = getByName0(name);
    return clone(stored);
  }

  T getByName0(String name) {
    ObjectId objectId = _names.get(caseInsensitive(name));
    if (objectId == null) {
      throw new DataNotFoundException(_objectDescription + " name not found: " + name);
    }
    return getById0(objectId);
  }

  T getById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    final T stored = getById0(objectId);
    return clone(stored);
  }

  T getById0(ObjectId objectId) {
    final T stored = _objects.get(objectId);
    if (stored == null || stored == _removed) {
      throw new DataNotFoundException(_objectDescription + " identifier not found: " + objectId);
    }
    return stored;
  }

  //-------------------------------------------------------------------------
  UniqueId add(T object) {
    ArgumentChecker.notNull(object, "object");
    ObjectId objectId = _objectIdSupplier.get();
    UniqueId uniqueId = objectId.atVersion("1");
    object = clone(object);
    object.setUniqueId(uniqueId);
    String nameKey = caseInsensitive(extractName(object));
    synchronized (this) {
      if (_names.containsKey(nameKey)) {
        throw new DataDuplicationException(_objectDescription + " already exists: " + extractName(object));
      }
      _objects.put(objectId, object);
      _names.put(nameKey, objectId);
    }
    Instant now = Instant.now();
    _changeManager.entityChanged(ChangeType.ADDED, objectId, now, null, now);
    return uniqueId;
  }

  UniqueId update(T object) {
    ArgumentChecker.notNull(object, "object");
    ArgumentChecker.notNull(object.getUniqueId(), "object.uniqueId");
    ArgumentChecker.notNull(object.getUniqueId().getVersion(), "object.uniqueId.version");
    ObjectId objectId = object.getUniqueId().getObjectId();
    String oldVersion = object.getUniqueId().getVersion();
    UniqueId newUniqueId = objectId.atVersion(Integer.toString(Integer.parseInt(oldVersion) + 1));
    object = clone(object);
    object.setUniqueId(newUniqueId);
    String nameKey = caseInsensitive(extractName(object));
    synchronized (this) {
      final T stored = getById0(objectId);
      if (stored.getUniqueId().getVersion().equals(oldVersion) == false) {
        throw new DataVersionException("Invalid version, " + _objectDescription + " has already been updated: " + objectId);
      }
      if (nameKey.equals(caseInsensitive(extractName(stored)))) {
        _objects.put(objectId, object);  // replace
      } else {
        if (_names.containsKey(nameKey)) {
          throw new DataDuplicationException(_objectDescription + " cannot be renamed, new name already exists: " + extractName(object));
        }
        _objects.put(objectId, object);  // replace
        _names.put(nameKey, objectId);
        // leave old name to forward to same object
      }
    }
    Instant now = Instant.now();
    _changeManager.entityChanged(ChangeType.CHANGED, objectId, now, null, now);
    return newUniqueId;
  }

  UniqueId save(T object) {
    ArgumentChecker.notNull(object, "object");
    if (object.getUniqueId() != null) {
      return update(object);
    } else {
      return add(object);
    }
  }

  //-------------------------------------------------------------------------
  void removeByName(String name) {
    ArgumentChecker.notNull(name, "name");
    // no need to synchronize as names is append-only
    ObjectId objectId = _names.get(caseInsensitive(name));
    if (objectId == null) {
      throw new DataNotFoundException(_objectDescription + " name not found: " + name);
    }
    removeById(objectId);
  }

  void removeById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    synchronized (this) {
      final T stored = _objects.get(objectId);
      if (stored == null) {
        throw new DataNotFoundException(_objectDescription + " identifier not found: " + objectId);
      } else if (stored == _removed) {
        return;  // return quietly to be idempotent
      }
      _objects.put(objectId, _removed);  // replace
    }
    Instant now = Instant.now();
    _changeManager.entityChanged(ChangeType.REMOVED, objectId, now, null, now);
  }

  //-------------------------------------------------------------------------
  List<HistoryEvent> eventHistory(ObjectId objectId, String name) {
    if (objectId != null && _objects.containsKey(objectId) == false) {
      throw new DataNotFoundException(_objectDescription + " identifier not found: " + objectId);
    } else if (name != null && _names.containsKey(caseInsensitive(name)) == false) {
      throw new DataNotFoundException(_objectDescription + " name not found: " + name);
    }
    return ImmutableList.of();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return String.format("%s[size=%d]", getClass().getSimpleName(), _objects.size());
  }

}
