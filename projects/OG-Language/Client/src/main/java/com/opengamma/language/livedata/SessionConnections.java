/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.livedata.Connection.Listener;

/**
 * Manages the connections for a {@link SessionContext}.
 */
public final class SessionConnections {

  private static final Logger s_logger = LoggerFactory.getLogger(SessionConnections.class);

  private static class ValueDispatcher implements Listener {

    private final int _identifier;
    private final SessionContext _context;

    public ValueDispatcher(final SessionContext context, final int identifier) {
      _context = context;
      _identifier = identifier;
    }

    @Override
    public void newValue(final Data value) {
      s_logger.debug("Value {} for connection {}", value, _identifier);
      _context.getGlobalContext().getLiveDataDispatcher().dispatchValue(_context, _identifier, value);
    }

  }

  private final SessionContext _context;
  private final AtomicInteger _nextIdentifier = new AtomicInteger();
  private final ConcurrentMap<Integer, Connection> _connections = new ConcurrentHashMap<Integer, Connection>();

  public SessionConnections(final SessionContext context) {
    _context = context;
  }

  /**
   * Adds a connection to the session.
   * 
   * @param identifier client allocated identifier
   * @param connection connection to manage
   */
  public void add(final int identifier, final Connection connection) {
    s_logger.info("Registering connection {}", identifier);
    final Connection previous = _connections.put(identifier, connection);
    if (previous != null) {
      s_logger.info("Cancelling previous connection on {}", identifier);
      previous.cancel();
      _context.getGlobalContext().getLiveDataDispatcher().dispatchValue(_context, identifier, null);
    }
    connection.setListener(new ValueDispatcher(_context, identifier));
  }

  /**
   * Adds a connection to the session.
   * 
   * @param connection connection to manage
   * @return allocated identifier
   */
  public int add(final Connection connection) {
    do {
      final int identifier = _nextIdentifier.getAndIncrement();
      if (_connections.putIfAbsent(identifier, connection) == null) {
        s_logger.info("Registering connection {}", identifier);
        connection.setListener(new ValueDispatcher(_context, identifier));
        return identifier;
      } else {
        s_logger.info("Collision on identifier {}", identifier);
      }
    } while (true);
  }

  /**
   * Cancels the connection referenced by the identifier.
   * 
   * @param identifier identifier of the connection to cancel
   */
  public void cancel(final int identifier) {
    s_logger.info("Canceling connection {}", identifier);
    final Connection connection = _connections.remove(identifier);
    if (connection != null) {
      connection.cancel();
      _context.getGlobalContext().getLiveDataDispatcher().dispatchValue(_context, identifier, null);
    } else {
      s_logger.warn("Invalid connection {} for cancelation", identifier);
    }
  }

  public void cancelAll() {
    s_logger.info("Canceling {} connections", _connections.size());
    final LiveDataDispatcher dispatcher = _context.getGlobalContext().getLiveDataDispatcher();
    for (Map.Entry<Integer, Connection> connection : _connections.entrySet()) {
      connection.getValue().cancel();
      dispatcher.dispatchValue(_context, connection.getKey(), null);
    }
  }

  public Result queryValue(final int identifier) {
    s_logger.info("Fetching value for {}", identifier);
    final Connection connection = _connections.get(identifier);
    if (connection != null) {
      return new Result(identifier, connection.getValue());
    } else {
      s_logger.warn("Invalid connection {} for query", identifier);
      return null;
    }
  }

}
