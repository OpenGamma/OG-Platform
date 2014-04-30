/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractSource;
import com.opengamma.core.ObjectChangeListener;
import com.opengamma.core.ObjectChangeListenerManager;
import com.opengamma.core.Source;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * An abstract source built on top of an underlying master.
 * 
 * @param <V> the type of the stored value
 * @param <D> the type of the document
 * @param <M> the type of the master
 */
@PublicSPI
public abstract class AbstractMasterSource<V extends UniqueIdentifiable, D extends AbstractDocument, M extends AbstractChangeProvidingMaster<? extends D>>
    extends AbstractSource<V>
    implements Source<V>, ObjectChangeListenerManager {

  /**
   * The master.
   */
  private final M _master;
  /**
   * The listeners.
   */
  private final ConcurrentHashMap<Pair<ObjectId, ObjectChangeListener>, ChangeListener> _registeredListeners = new ConcurrentHashMap<Pair<ObjectId, ObjectChangeListener>, ChangeListener>();

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   */
  public AbstractMasterSource(final M master) {
    ArgumentChecker.notNull(master, "master");
    _master = master;
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

  //-------------------------------------------------------------------------
  /**
   * Gets a document from the master by unique identifier.
   * <p>
   * This overrides the version in the unique identifier if set to do so.
   * 
   * @param uniqueId the unique identifier, not null
   * @return the document, not null
   * @throws DataNotFoundException if the document could not be found
   */
  public D getDocument(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return (D) getMaster().get(uniqueId);
  }

  /**
   * Gets a document from the master by object identifier and version-correction.
   * <p>
   * The specified version-correction may be overridden if set to do so.
   * 
   * @param objectId the object identifier, not null
   * @param versionCorrection the version-correction, not null
   * @return the document, not null
   * @throws DataNotFoundException if the document could not be found
   */
  public D getDocument(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return (D) getMaster().get(objectId, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public V get(UniqueId uniqueId) {
    return (V) getDocument(uniqueId).getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public V get(ObjectId objectId, VersionCorrection versionCorrection) {
    return (V) getMaster().get(objectId, versionCorrection).getValue();
  }

  public V getFirstObject(Collection<? extends V> objects) {
    return objects.isEmpty() ? null : objects.iterator().next();
  }

  //-------------------------------------------------------------------------
  @Override
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
    _registeredListeners.put(Pairs.of(oid, listener), changeListener);
    changeManager().addChangeListener(changeListener);
  }

  @Override
  public void removeChangeListener(ObjectId oid, ObjectChangeListener listener) {
    ChangeListener changeListener = _registeredListeners.remove(Pairs.of(oid, listener));
    changeManager().removeChangeListener(changeListener);
  }

  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getMaster() + "]";
  }

}
