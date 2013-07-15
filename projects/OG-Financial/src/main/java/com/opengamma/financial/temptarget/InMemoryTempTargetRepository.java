/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;

/**
 * In-memory implementation of {@link TempTargetRepository}. This is for testing/debugging purposes only. It is not suitable for production use or large views as data will never be flushed from
 * memory.
 */
public class InMemoryTempTargetRepository implements TempTargetRepository {

  private final UniqueIdSupplier _uids = new UniqueIdSupplier("TmpMem");

  private final ConcurrentMap<UniqueId, TempTarget> _uid2object = new ConcurrentHashMap<UniqueId, TempTarget>();

  private final ConcurrentMap<TempTarget, UniqueId> _object2uid = new ConcurrentHashMap<TempTarget, UniqueId>();

  // TempTargetRepository

  @Override
  public TempTarget get(final UniqueId identifier) {
    return _uid2object.get(identifier);
  }

  @Override
  public UniqueId locateOrStore(final TempTarget target) {
    UniqueId uid = _object2uid.get(target);
    if (uid != null) {
      return uid;
    }
    uid = _uids.get();
    _uid2object.put(uid, target.withUniqueId(uid));
    final UniqueId existing = _object2uid.putIfAbsent(target, uid);
    if (existing != null) {
      _uid2object.remove(uid);
      return existing;
    }
    return uid;
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
