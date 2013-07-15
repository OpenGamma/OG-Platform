/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.AbstractBerkeleyDBWorker.PoisonRequest;
import com.opengamma.util.ArgumentChecker;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * An implementation of {@link BinaryDataStore} which backs all data on a BerkeleyDB table.
 */
public class BerkeleyDBBinaryDataStore extends AbstractBerkeleyDBComponent implements BinaryDataStore {

  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBBinaryDataStore.class);

  private final class Worker extends AbstractBerkeleyDBWorker implements BinaryDataStore {

    private final DatabaseEntry _key = new DatabaseEntry();
    private final DatabaseEntry _value = new DatabaseEntry();

    public Worker(final BlockingQueue<Request> requests) {
      super(null, requests);
    }

    @Override
    public void delete() {
      getDatabase().close();
      // TODO kirk 2010-08-07 -- For batch operation, we'd have to explicitly remove the DB as well.
      // getDbEnvironment().removeDatabase(null, getDatabaseName());
      stop();
    }

    @Override
    public byte[] get(final long identifier) {
      LongBinding.longToEntry(identifier, _key);
      OperationStatus opStatus = getDatabase().get(getTransaction(), _key, _value, LockMode.READ_UNCOMMITTED);
      switch (opStatus) {
        case SUCCESS:
          return _value.getData();
        default:
          s_logger.debug("{} - No record available for identifier {} status {}", new Object[] {getDatabaseName(), identifier, opStatus });
          return null;
      }
    }

    @Override
    public void put(final long identifier, final byte[] data) {
      LongBinding.longToEntry(identifier, _key);
      _value.setData(data);
      OperationStatus opStatus = getDatabase().put(getTransaction(), _key, _value);
      switch (opStatus) {
        case SUCCESS:
          return;
        default:
          s_logger.warn("{} - Unable to write to identifier {} status {}", new Object[] {getDatabaseName(), identifier, opStatus });
      }
    }

    @Override
    public Map<Long, byte[]> get(final Collection<Long> identifiers) {
      return AbstractBinaryDataStore.get(this, identifiers);
    }

    @Override
    public void put(final Map<Long, byte[]> data) {
      AbstractBinaryDataStore.put(this, data);
    }

  }

  private BlockingQueue<Worker.Request> _requests;

  public BerkeleyDBBinaryDataStore(Environment dbEnvironment, String databaseName) {
    super(dbEnvironment, databaseName);
  }

  @Override
  public void start() {
    synchronized (this) {
      if (_requests == null) {
        _requests = new LinkedBlockingQueue<Worker.Request>();
        // TODO: We can have multiple worker threads -- will that be good or bad?
        final Thread worker = new Thread(new Worker(_requests));
        worker.setName("BerkeleyDBBinaryDataStore-Worker");
        worker.setDaemon(true);
        worker.start();
      }
    }
    super.start();
  }

  @Override
  public void stop() {
    synchronized (this) {
      if (_requests != null) {
        _requests.add(new PoisonRequest());
        _requests = null;
      }
    }
  }

  @Override
  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(false);
    // TODO kirk 2010-08-07 -- For Batch operation, this should be set to false probably.
    dbConfig.setTemporary(true);
    // TODO kirk 2010-08-07 -- For Batch operation, this should be set to true probably.
    dbConfig.setDeferredWrite(false);
    return dbConfig;
  }

  private static final class DeleteRequest extends Worker.Request {

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      ((Worker) worker).delete();
    }

    public void run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
    }

  }

  @Override
  public void delete() {
    new DeleteRequest().run(_requests);
  }

  private static final class GetRequest extends Worker.Request {

    private final long _identifier;
    private byte[] _result;

    public GetRequest(final long identifier) {
      _identifier = identifier;
    }

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      _result = ((Worker) worker).get(_identifier);
    }

    public byte[] run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
      return _result;
    }

  }

  @Override
  public byte[] get(long identifier) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    return new GetRequest(identifier).run(_requests);
  }

  private static final class PutRequest extends Worker.Request {

    private final long _identifier;
    private final byte[] _data;

    public PutRequest(final long identifier, final byte[] data) {
      _identifier = identifier;
      _data = data;
    }

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      ((Worker) worker).put(_identifier, _data);
    }

    public void run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
    }

  }

  @Override
  public void put(long identifier, byte[] data) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    ArgumentChecker.notNull(data, "data to store");
    new PutRequest(identifier, data).run(_requests);
  }

  private static final class BulkGetRequest extends Worker.Request {

    private final Collection<Long> _identifiers;
    private Map<Long, byte[]> _result;

    public BulkGetRequest(final Collection<Long> identifiers) {
      _identifiers = identifiers;
    }

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      _result = ((Worker) worker).get(_identifiers);
    }

    public Map<Long, byte[]> run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
      return _result;
    }

  }

  @Override
  public Map<Long, byte[]> get(final Collection<Long> identifiers) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    return new BulkGetRequest(identifiers).run(_requests);
  }

  private static final class BulkPutRequest extends Worker.Request {

    private final Map<Long, byte[]> _data;

    public BulkPutRequest(final Map<Long, byte[]> data) {
      _data = data;
    }

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      ((Worker) worker).put(_data);
    }

    public void run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
    }

  }

  @Override
  public void put(final Map<Long, byte[]> data) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    new BulkPutRequest(data).run(_requests);
  }

}
