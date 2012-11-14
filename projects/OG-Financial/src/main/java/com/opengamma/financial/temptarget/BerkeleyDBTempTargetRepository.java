/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import java.io.File;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.cache.BerkeleyDBViewComputationCacheSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * {@link RollingTempTargetRepository} implementation based on Berkeley DB stores.
 */
public class BerkeleyDBTempTargetRepository extends RollingTempTargetRepository implements Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBTempTargetRepository.class);

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  private static final class Generation {

    private final Database _id2Target;

    private final Database _target2Id;

    private final Database _id2LastAccessed;

    public Generation(final Environment environment, final int generation) {
      final DatabaseConfig config = new DatabaseConfig();
      config.setAllowCreate(true);
      config.setTemporary(true);
      _id2Target = openTruncated(environment, config, "target" + generation);
      _target2Id = openTruncated(environment, config, "identifier" + generation);
      _id2LastAccessed = openTruncated(environment, config, "access" + generation);
    }

    private byte[] toByteArray(final TempTarget target) {
      final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
      final MutableFudgeMsg msg = serializer.newMessage();
      target.toFudgeMsg(serializer, msg);
      FudgeSerializer.addClassHeader(msg, target.getClass(), TempTarget.class);
      return s_fudgeContext.toByteArray(msg);
    }

    private TempTarget fromByteArray(final byte[] data) {
      final FudgeDeserializer deserializer = new FudgeDeserializer(s_fudgeContext);
      return deserializer.fudgeMsgToObject(TempTarget.class, s_fudgeContext.deserialize(data).getMessage());
    }

    public TempTarget get(final long uid) {
      final DatabaseEntry key = new DatabaseEntry();
      LongBinding.longToEntry(uid, key);
      final DatabaseEntry value = new DatabaseEntry();
      if (_id2Target.get(null, key, value, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
        final TempTarget target = fromByteArray(value.getData());
        LongBinding.longToEntry(System.nanoTime(), value);
        _id2LastAccessed.put(null, key, value);
        return target;
      } else {
        if (s_logger.isDebugEnabled()) {
          s_logger.debug("No record found for {} in {}", uid, _id2Target.getDatabaseName());
        }
        return null;
      }
    }

    public Long find(final TempTarget target) {
      final DatabaseEntry key = new DatabaseEntry();
      key.setData(toByteArray(target));
      final DatabaseEntry value = new DatabaseEntry();
      if (_target2Id.get(null, key, value, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
        final long result = LongBinding.entryToLong(value);
        LongBinding.longToEntry(System.nanoTime(), value);
        _id2LastAccessed.put(null, key, value);
        return result;
      } else {
        if (s_logger.isDebugEnabled()) {
          s_logger.debug("No record found for {} in {}", target, _target2Id.getDatabaseName());
        }
        return null;
      }
    }

    public long store(final long newId, final TempTarget target) {
      final DatabaseEntry idEntry = new DatabaseEntry();
      LongBinding.longToEntry(newId, idEntry);
      final DatabaseEntry targetEntry = new DatabaseEntry();
      targetEntry.setData(toByteArray(target));
      // Write the new identifier marker first in case the target2Id write is successful
      if (_id2Target.putNoOverwrite(null, idEntry, targetEntry) != OperationStatus.SUCCESS) {
        // Shouldn't happen - the new identifier must be unique at this point
        throw new OpenGammaRuntimeException("Internal error writing new record marker");
      }
      final OperationStatus status = _target2Id.putNoOverwrite(null, targetEntry, idEntry);
      if (status == OperationStatus.SUCCESS) {
        LongBinding.longToEntry(System.nanoTime(), targetEntry);
        _id2LastAccessed.put(null, idEntry, targetEntry);
        return newId;
      }
      // Write was unsuccessful; won't be using the new identifier so remove the marker
      _id2Target.delete(null, idEntry);
      if (status != OperationStatus.KEYEXIST) {
        throw new OpenGammaRuntimeException("Couldn't write to database");
      }
      if (_target2Id.get(null, targetEntry, idEntry, LockMode.READ_UNCOMMITTED) != OperationStatus.SUCCESS) {
        // Shouldn't happen - the identifier must be there since the previous put failed
        throw new OpenGammaRuntimeException("Internal error fetching existing record");
      }
      return LongBinding.entryToLong(idEntry);
    }

    public void delete() {
      // These are temporary databases; the close operation should delete them
      _id2Target.close();
      _target2Id.close();
      _id2LastAccessed.close();
    }

    public void copyLiveObjects(final long deadTime, final Generation next, final List<Long> deletes) {
      final DatabaseEntry identifier = new DatabaseEntry();
      final DatabaseEntry target = new DatabaseEntry();
      final DatabaseEntry accessed = new DatabaseEntry();
      final Cursor cursor = _id2LastAccessed.openCursor(null, null);
      while (cursor.getNext(identifier, accessed, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
        final long lastAccess = LongBinding.entryToLong(accessed);
        if (lastAccess - deadTime > 0) {
          if (_id2Target.get(null, identifier, target, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
            next._id2Target.put(null, identifier, target);
            next._target2Id.put(null, target, identifier);
            next._id2LastAccessed.put(null, identifier, accessed);
          } else {
            deletes.add(LongBinding.entryToLong(identifier));
          }
        } else {
          deletes.add(LongBinding.entryToLong(identifier));
        }
      }
      cursor.close();
    }

  }

  private static Database openTruncated(final Environment environment, final DatabaseConfig config, final String name) {
    s_logger.debug("Opening {}", name);
    Database handle = environment.openDatabase(null, name, config);
    if (handle.count() > 0) {
      handle.close();
      s_logger.info("Truncating existing {}", name);
      environment.truncateDatabase(null, name, false);
      handle = environment.openDatabase(null, name, config);
    }
    return handle;
  }

  private final File _dir;

  private Environment _environment;

  private int _generation;

  private volatile Generation _old;

  private volatile Generation _new;

  /**
   * Creates a new temporary target repository.
   *
   * @param dbDir the folder to use for the repository, it will be created if it doesn't exist. If it contains existing files they may be destroyed.
   */
  public BerkeleyDBTempTargetRepository(final File dbDir) {
    this(SCHEME, dbDir);
  }

  /**
   * Creates a new temporary target repository.
   *
   * @param scheme the scheme to use for unique identifiers allocated by this repository
   * @param dbDir the folder to use for the repository, it will be created if it doesn't exist. If it contains existing files they may be destroyed.
   */
  public BerkeleyDBTempTargetRepository(final String scheme, final File dbDir) {
    super(scheme);
    ArgumentChecker.notNull(dbDir, "dbDir");
    _dir = dbDir;
  }

  protected Generation getOrCreateNewGeneration() {
    if (_new == null) {
      synchronized (this) {
        if (_new == null) {
          final int identifier = _generation++;
          try {
            s_logger.info("Creating disk storage for generation {}", identifier);
            _new = new Generation(_environment, identifier);
          } catch (final RuntimeException e) {
            s_logger.error("Couldn't create generation {}", identifier);
            s_logger.warn("Caught exception", e);
            throw e;
          }
        }
      }
    }
    return _new;
  }

  // RollingTempTargetRepository

  @Override
  protected TempTarget getOldGeneration(final long uid) {
    final Generation gen = _old;
    if (gen != null) {
      return gen.get(uid);
    } else {
      s_logger.debug("No old generation to lookup {} in", uid);
      return null;
    }
  }

  @Override
  protected TempTarget getNewGeneration(final long uid) {
    final Generation gen = _new;
    if (gen != null) {
      return gen.get(uid);
    } else {
      s_logger.debug("No new generation to lookup {} in", uid);
      return null;
    }
  }

  @Override
  protected Long findOldGeneration(final TempTarget target) {
    final Generation gen = _old;
    if (gen != null) {
      return gen.find(target);
    } else {
      s_logger.debug("No old generation to lookup {} in", target);
      return null;
    }
  }

  @Override
  protected long findOrAddNewGeneration(final TempTarget target) {
    final Generation gen = getOrCreateNewGeneration();
    final Long uidObject = gen.find(target);
    if (uidObject != null) {
      s_logger.debug("New generation hit for {}", target);
      return uidObject.longValue();
    } else {
      s_logger.debug("New generation miss for {}", target);
    }
    return gen.store(allocIdentifier(), target);
  }

  @Override
  protected boolean copyOldToNewGeneration(final long deadTime, final List<Long> deletes) {
    final Generation newGen = _new;
    if (newGen == null) {
      s_logger.debug("No new generation to copy values to");
      return false;
    }
    final Generation oldGen = _old;
    if (oldGen != null) {
      oldGen.copyLiveObjects(deadTime, newGen, deletes);
    } else {
      s_logger.debug("No old generation to copy values from");
    }
    return true;
  }

  @Override
  protected void nextGeneration() {
    final Generation gen = _old;
    if (gen != null) {
      s_logger.debug("Deleting old generation");
      gen.delete();
    } else {
      s_logger.debug("No old generation to delete");
    }
    _old = _new;
    _new = null;
  }

  // Lifecycle

  @Override
  public synchronized void start() {
    if (_environment == null) {
      _environment = BerkeleyDBViewComputationCacheSource.constructDatabaseEnvironment(_dir, true);
    }
  }

  @Override
  public synchronized void stop() {
    if (_environment != null) {
      Generation gen = _old;
      if (gen != null) {
        gen.delete();
      }
      gen = _new;
      if (gen != null) {
        gen.delete();
      }
      for (final File file : _dir.listFiles()) {
        file.delete();
      }
      _dir.delete();
      _environment = null;
    }
  }

  @Override
  public synchronized boolean isRunning() {
    return _environment != null;
  }

}
