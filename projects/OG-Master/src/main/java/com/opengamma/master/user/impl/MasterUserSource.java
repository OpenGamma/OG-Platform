/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.core.ObjectChangeListener;
import com.opengamma.core.ObjectChangeListenerManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.UserSource;
import com.opengamma.id.ObjectId;
import com.opengamma.master.user.UserMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A {@code UserSource} implemented using an underlying {@code UserMaster}.
 * <p>
 * The {@link UserSource} interface is a minimal interface for accessing user accounts.
 * This class implements the source on top of a standard {@link UserMaster}.
 */
public class MasterUserSource implements UserSource, ObjectChangeListenerManager {

  /**
   * The master.
   */
  private final UserMaster _master;
  /**
   * The listeners.
   */
  private final ConcurrentHashMap<Pair<ObjectId, ObjectChangeListener>, ChangeListener> _registeredListeners = new ConcurrentHashMap<>();

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   */
  public MasterUserSource(final UserMaster master) {
    ArgumentChecker.notNull(master, "master");
    _master = master;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * 
   * @return the master, not null
   */
  public UserMaster getMaster() {
    return _master;
  }

  //-------------------------------------------------------------------------
  @Override
  public UserAccount getAccount(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    return getMaster().getAccount(userName);
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

  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getMaster() + "]";
  }

}
