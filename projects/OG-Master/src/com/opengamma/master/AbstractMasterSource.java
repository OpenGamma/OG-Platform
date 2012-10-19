/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.ObjectChangeListener;
import com.opengamma.core.ObjectChangeListenerManager;
import com.opengamma.core.Source;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;

/**
 * An abstract source built on top of an underlying master.
 *
 * @param <D>  the type of the document
 * @param <M>  the type of the master
 */
@PublicSPI
public abstract class AbstractMasterSource<T extends UniqueIdentifiable, D extends AbstractDocument<? extends T>, M extends AbstractChangeProvidingMaster<? extends T, ? extends D>> implements Source<T>, VersionedSource, ObjectChangeListenerManager {

  /**
   * The master.
   */
  private final M _master;
  /**
   * The version-correction locator to search at, null to not override versions.
   */
  private volatile VersionCorrection _versionCorrection;

  /**
   * Creates an instance with an underlying master which does not override versions.
   *
   * @param master  the master, not null
   */
  public AbstractMasterSource(final M master) {
    this(master, null);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   *
   * @param master  the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public AbstractMasterSource(final M master, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(master, "master");
    _master = master;
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the underlying master.
   *
   * @return the master, not null
   */
  public M getMaster() {
    return _master;
  }

  /**
   * Gets the version-correction locator to search at.
   *
   * @return the version-correction locator to search at, null if not overriding versions
   */
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  /**
   * Sets the version-correction locator to search at.
   *
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  @Override
  public void setVersionCorrection(final VersionCorrection versionCorrection) {
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets a document from the master by unique identifier.
   * <p>
   * This overrides the version in the unique identifier if set to do so.
   *
   * @param uniqueId  the unique identifier, not null
   * @return the document, not null
   * @throws DataNotFoundException if the document could not be found
   */
  @SuppressWarnings({"unchecked"})
  public D getDocument(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final VersionCorrection vc = getVersionCorrection(); // lock against change
    if (vc != null) {
      return (D) getMaster().get(uniqueId.getObjectId(), vc);
    } else {
      return (D) getMaster().get(uniqueId);
    }
  }

  /**
   * Gets a document from the master by object identifier and version-correction.
   * <p>
   * The specified version-correction may be overridden if set to do so.
   *
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction, not null
   * @return the document, not null
   * @throws DataNotFoundException if the document could not be found
   */
  @SuppressWarnings({"unchecked"})
  public D getDocument(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    VersionCorrection overrideVersionCorrection = getVersionCorrection();
    return (D) getMaster().get(objectId, overrideVersionCorrection != null ? overrideVersionCorrection : versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = getClass().getSimpleName() + "[" + getMaster();
    if (getVersionCorrection() != null) {
      str += ",versionCorrection=" + getVersionCorrection();
    }
    return str + "]";
  }

  /**
   * The listeners.
   */
  private final ConcurrentHashMap<Pair<ObjectId, ObjectChangeListener>, ChangeListener> _registeredListeners = new ConcurrentHashMap<Pair<ObjectId, ObjectChangeListener>, ChangeListener>();

  public void addChangeListener(final ObjectId oid, final ObjectChangeListener listener) {
    ChangeListener changeListener = new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        ObjectId changedOid = event.getObjectId();        
        if (changedOid.equals(oid)) {
          listener.objectChanged(oid);
        }
      }
    };
    _registeredListeners.put(Pair.of(oid, listener), changeListener);
    getMaster().changeManager().addChangeListener(changeListener);
  }

  public void removeChangeListener(ObjectId oid, ObjectChangeListener listener) {
    ChangeListener changeListener = _registeredListeners.remove(Pair.of(oid, listener));
    getMaster().changeManager().removeChangeListener(changeListener);
  }

  @Override
  public Map<UniqueId, T> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, T> result = Maps.newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      try {
        T security = get(uniqueId);
        result.put(uniqueId, security);
      } catch (DataNotFoundException ex) {
        // do nothing
      }
    }
    return result;
  }

  public T getFirstObject(Collection<? extends T> objects) {
    return objects.isEmpty() ? null : objects.iterator().next();
  }

  @Override
  public T get(UniqueId uniqueId) {
    return getDocument(uniqueId).getObject();
  }
  
  @SuppressWarnings({"unchecked"})
  public T get(ObjectId objectId, VersionCorrection versionCorrection){
    D document = (D) getMaster().get(objectId, versionCorrection);
    return document.getObject();
  }
}
