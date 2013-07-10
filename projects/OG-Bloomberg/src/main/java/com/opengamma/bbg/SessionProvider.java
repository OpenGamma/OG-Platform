/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.ConnectionUnavailableException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 * Supplies a Bloomberg session and service, creating the connection and managaing reconnection if necessary.
 * The main purpose of this class is to throttle the rate of attempts to reconnect. If Bloomberg is down and a
 * connection is attempted every time a request is made the Bloomberg API runs out of memory, possibly due to
 * an unconstrained thread pool.
 * <p>
 * This class creates a session on demand but if connection fails it won't retry until a delay has elapsed to avoid
 * overwhelming the Bloomberg API.
 */
public class SessionProvider {

  /** The logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SessionProvider.class);
  /** Default time in ms to wait before retrying after an unsuccessful attempt to connect. */
  private static final long DEFAULT_RETRY_DELAY = 30000;

  /** The connection details. */
  private final BloombergConnector _connector;
  /** Name of the Bloomberg service to create. */
  private final String _serviceName;
  /** Controls access to the session. */
  private final Object _lock = new Object();
  /** Duration to wait before retrying after an unsuccessful attempt to connect. */
  private final Duration _retryDuration;

  /** The session. */
  private Session _session;
  /** The time of the last connection attempt. */
  private Instant _lastRetry = Instant.EPOCH;

  /**
   * @param connector Bloomberg connection details
   * @param serviceName Name of the service to open
   */
  public SessionProvider(BloombergConnector connector, String serviceName) {
    this(connector, serviceName, DEFAULT_RETRY_DELAY);
  }

  /**
   * @param connector Bloomberg connection details
   * @param serviceName Name of the Bloomberg service to open
   * @param retryDelay Time to wait between unsuccessful connection attempts
   */
  public SessionProvider(BloombergConnector connector, String serviceName, long retryDelay) {
    ArgumentChecker.notNull(connector, "connector");
    ArgumentChecker.notEmpty(serviceName, "serviceName");
    _retryDuration = Duration.of(retryDelay, ChronoUnit.MILLIS);
    _connector = connector;
    _serviceName = serviceName;
  }

  /**
   * Returns a session, creating it if necessary. If this method is called after the provider is closed a new session
   * will be created.
   * @return The session, not null
   * @throws ConnectionUnavailableException If no connection is available
   */
  public Session getSession() {
    synchronized (_lock) {
      if (_session != null) {
        return _session;
      } else {
        _session = null;
        Instant now = OpenGammaClock.getInstance().instant();
        if (Duration.between(_lastRetry, now).compareTo(_retryDuration) < 0) {
          throw new ConnectionUnavailableException("No Bloomberg connection is available");
        }
        _lastRetry = now;
        s_logger.info("Bloomberg session being opened...");
        Session session;
        try {
          session = _connector.createOpenSession();
        } catch (OpenGammaRuntimeException e) {
          throw new ConnectionUnavailableException("Failed to open session", e);
        }
        s_logger.info("Bloomberg session open");
        s_logger.info("Bloomberg service being opened...");
        try {
          if (!session.openService(_serviceName)) {
            throw new ConnectionUnavailableException("Bloomberg service failed to start: " + _serviceName);
          }
        } catch (InterruptedException ex) {
          Thread.interrupted();
          throw new ConnectionUnavailableException("Bloomberg service failed to start: " + _serviceName, ex);
        } catch (Exception ex) {
          throw new ConnectionUnavailableException("Bloomberg service failed to start: " + _serviceName, ex);
        }
        s_logger.info("Bloomberg service open: {}", _serviceName);
        _session = session;
        return _session;
      }
    }
  }

  /**
   * @return The service, not null
   * @throws ConnectionUnavailableException If no connection is available
   */
  public Service getService() {
    return getSession().getService(_serviceName);
  }

  /**
   * @return true if there is an open connection to Bloomberg.
   */
  public boolean isConnected() {
    try {
      getSession();
      return true;
    } catch (ConnectionUnavailableException e) {
      return false;
    }
  }

  /**
   * Stops the session. If {@link #getSession()} is called after this method a new session will be created. Calling
   * this method when there is no active session does nothing.
   */
  public void invalidateSession() {
    synchronized (_lock) {
      if (_session != null) {
        try {
          s_logger.info("Bloomberg session being stopped...");
          _session.stop();
          s_logger.info("Bloomberg session stopped");
        } catch (InterruptedException e) {
          s_logger.warn("Interrupted closing session " + _session, e);
        } finally {
          _session = null;
        }
      }
    }
  }
}
