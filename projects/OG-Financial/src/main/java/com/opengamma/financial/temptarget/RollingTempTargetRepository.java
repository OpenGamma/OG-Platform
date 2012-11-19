/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link TempTargetRepository} based on rolling storage that support read/search, append and delete-all operations only.
 */
public abstract class RollingTempTargetRepository implements TempTargetRepository {

  private static final Logger s_logger = LoggerFactory.getLogger(RollingTempTargetRepository.class);

  private static final long TTL_PERIOD = 3600000L; // 1 Hour

  private static final long HOUSEKEEP_PERIOD = TTL_PERIOD / 3;

  // Note that the temp targets can be a bottleneck during graph construction if used heavily. If there are multiple nodes involved
  // in graph construction (e.g. multiple view processors) then it might make sense to have a repository on each one which gets used
  // for target construction (each with a different scheme), and a target resolver that can fork requests to the correct node based
  // on the scheme in the identifiers

  // The lifespan of a repository must be greater than any ComputationTargetSpecification instances. This includes graphs under construction
  // and graphs currently being executed. These are structures that only exist in memory at the moment. If serialized or persisted somewhere
  // then it makes little sense to write out the temporary reference - better at that point to resolve the reference into a real object,
  // store it somewhere more persistent such as the config database, and then hold a reference to that.

  /**
   * Default scheme name.
   */
  public static final String SCHEME = "Tmp";

  private final String _scheme;

  private final AtomicLong _nextNewIdentifier = new AtomicLong();

  private volatile long _lastOldIdentifier = -1;

  private final Lock _shared;

  private final Lock _exclusive;

  private final ChangeManager _changeManager = new BasicChangeManager();

  private final Timer _cleaner = new Timer();

  protected RollingTempTargetRepository() {
    this(SCHEME);
  }

  protected RollingTempTargetRepository(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _scheme = scheme;
    final ReadWriteLock rw = new ReentrantReadWriteLock();
    _shared = rw.readLock();
    _exclusive = rw.writeLock();
    _cleaner.schedule(new HousekeepTask(this), TTL_PERIOD, HOUSEKEEP_PERIOD);
  }

  private static final class HousekeepTask extends TimerTask {

    private final WeakReference<RollingTempTargetRepository> _owner;

    public HousekeepTask(final RollingTempTargetRepository owner) {
      _owner = new WeakReference<RollingTempTargetRepository>(owner);
    }

    @Override
    public void run() {
      final RollingTempTargetRepository owner = _owner.get();
      if (owner != null) {
        s_logger.info("Calling housekeep operation on {}", owner);
        owner.housekeep();
      } else {
        s_logger.debug("Owner repository has been garbage collected");
      }
    }

  }

  /**
   * Searches for a record in the "old" generation store.
   *
   * @param uid the identifier to search for
   * @return the record or null if not found
   */
  protected abstract TempTarget getOldGeneration(final long uid);

  /**
   * Searches for a record in the "new" generation store.
   *
   * @param uid the identifier to search for
   * @return the record or null if not found
   */
  protected abstract TempTarget getNewGeneration(final long uid);

  /**
   * Searches for a record in the "old" generation store.
   *
   * @param target the record to search for, not null
   * @return the identifier of the record or null if not found
   */
  protected abstract Long findOldGeneration(final TempTarget target);

  protected long allocIdentifier() {
    return _nextNewIdentifier.getAndIncrement();
  }

  protected UniqueId createIdentifier(final long uid) {
    return UniqueId.of(_scheme, Long.toString(uid));
  }

  /**
   * Searches for a record in the "new" generation store or adds one if none is present.
   * <p>
   * Any new record identifiers must be allocated by calling {@link #allocIdentifier}.
   *
   * @param target the record to search for, not null
   * @return the identifier of the matched record, or the new record identifier
   */
  protected abstract long findOrAddNewGeneration(final TempTarget target);

  /**
   * Copies all "live" records from the "old" to the "new" generation. Anything not copied because it hasn't been accessed for a while should be written to the {@code deletes} list.
   *
   * @param deadTime the {@link System#nanoTime} before which the record can be considered dead
   * @param deletes the delete notification list. This will be used to update anything subscribed to the change manager.
   * @return true if the copy was done, false if there was no copy and the old generation must be kept (for example nothing would be discarded)
   */
  protected abstract boolean copyOldToNewGeneration(final long deadTime, final List<Long> deletes);

  /**
   * Rolls the storage from "new" to "old" generation. The previous "old" generation may be discarded and what was the "new" generation is now the "old" generation.
   */
  protected abstract void nextGeneration();

  /**
   * Roll the files on disk, copying anything accessed recently from the old generation to the new generation.
   * <p>
   * This method may be called concurrently to the {@link TempTargetRepository} methods, but may only be called by a single thread.
   */
  protected void housekeep() {
    final List<Long> deletes = new LinkedList<Long>();
    _shared.lock();
    try {
      s_logger.info("Copying live objects to new generation");
      if (!copyOldToNewGeneration(System.nanoTime() - TTL_PERIOD * 1000000L, deletes)) {
        s_logger.info("Skipping housekeep operation");
        assert deletes.isEmpty();
        return;
      }
    } finally {
      _shared.unlock();
    }
    s_logger.info("Evicted {} dead objects", deletes.size());
    _exclusive.lock();
    try {
      s_logger.info("Creating new generation");
      nextGeneration();
      _lastOldIdentifier = _nextNewIdentifier.get() - 1;
    } finally {
      _exclusive.unlock();
    }
    s_logger.debug("Notifying subscribed listeners");
    final Instant now = Instant.now();
    for (final Long deleted : deletes) {
      _changeManager.entityChanged(ChangeType.REMOVED, ObjectId.of(_scheme, deleted.toString()), now, null, now);
    }
  }

  // TempTargetRepository

  @Override
  public TempTarget get(final UniqueId identifier) {
    if (!identifier.getScheme().equals(_scheme)) {
      return null;
    }
    final long uid = Long.parseLong(identifier.getValue());
    _shared.lock();
    try {
      if (uid <= _lastOldIdentifier) {
        return getOldGeneration(uid);
      } else {
        return getNewGeneration(uid);
      }
    } finally {
      _shared.unlock();
    }
  }

  @Override
  public UniqueId locateOrStore(final TempTarget target) {
    _shared.lock();
    try {
      final Long uidObject = findOldGeneration(target);
      if (uidObject != null) {
        return UniqueId.of(_scheme, uidObject.toString());
      }
      return createIdentifier(findOrAddNewGeneration(target));
    } finally {
      _shared.unlock();
    }
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
