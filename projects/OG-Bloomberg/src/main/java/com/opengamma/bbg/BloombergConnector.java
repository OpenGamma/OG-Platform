/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.referencedata.statistics.NullBloombergReferenceDataStatistics;
import com.opengamma.bbg.util.SessionOptionsUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Connector;

/**
 * Connector used to access Bloomberg.
 * <p>
 * This class performs only minimal session connections; the caller must
 * configure them and attach them to Bloomberg services.
 * This is mainly a data holder for connectivity.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class BloombergConnector implements Connector {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergConnector.class);

  /**
   * Callback interface for listeners which wish to be notified when the Bloomberg connection is available.
   */
  public interface AvailabilityListener {

    void bloombergAvailable();

  }

  /**
   * The configuration name.
   */
  private final String _name;
  /**
   * The Bloomberg Session Options.
   */
  private final SessionOptions _sessionOptions;
  /**
   * The Bloomberg statistics.
   */
  private final BloombergReferenceDataStatistics _referenceDataStatistics;
  /**
   * The listeners that wish to be notified whenever Bloomberg is available.
   */
  private final Collection<AvailabilityListener> _listeners = new CopyOnWriteArrayList<AvailabilityListener>();

  /**
   * Creates an instance.
   * 
   * @param name the configuration name, not null
   * @param sessionOptions the Bloomberg session options, not null
   */
  public BloombergConnector(String name, SessionOptions sessionOptions) {
    this(name, sessionOptions, NullBloombergReferenceDataStatistics.INSTANCE);
  }

  /**
   * Creates an instance.
   * 
   * @param name the configuration name, not null
   * @param sessionOptions the Bloomberg session options, not null
   * @param statistics the Bloomberg statistics, not null
   */
  public BloombergConnector(String name, SessionOptions sessionOptions, BloombergReferenceDataStatistics statistics) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(sessionOptions, "sessionOptions");
    ArgumentChecker.notNull(statistics, "statistics");
    _name = name;
    _sessionOptions = sessionOptions;
    _referenceDataStatistics = statistics;
  }

  /**
   * Creates an instance.
   * <p>
   * Subclasses must override the session options getter.
   * 
   * @param name the configuration name, not null
   * @param statistics the Bloomberg statistics, not null
   */
  protected BloombergConnector(String name, BloombergReferenceDataStatistics statistics) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(statistics, "statistics");
    _name = name;
    _sessionOptions = null;
    _referenceDataStatistics = statistics;
  }

  //-------------------------------------------------------------------------
  @Override
  public final String getName() {
    return _name;
  }

  @Override
  public final Class<? extends Connector> getType() {
    return BloombergConnector.class;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Bloomberg session options.
   * <p>
   * DO NOT MODIFY this object.
   * 
   * @return the Bloomberg session options, not null
   */
  public SessionOptions getSessionOptions() {
    return _sessionOptions;
  }

  /**
   * Gets the Bloomberg reference data statistics.
   * <p>
   * This is used to capture statistics about Bloomberg use.
   * 
   * @return the Bloomberg statistics recorder, not null
   */
  public BloombergReferenceDataStatistics getReferenceDataStatistics() {
    return _referenceDataStatistics;
  }

  /**
   * Creates and starts a new Bloomberg {@code Session} in Synchronous mode.
   * <p>
   * 
   * @return the started Bloomberg session, not null
   * @throws RuntimeException if an error occurs
   */
  public Session createOpenSession() {
    return createOpenSession(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates and starts a new Bloomberg {@code Session}.
   * <p>
   * The connector does not retain the state of the session, thus the caller is responsible for its lifecycle.
   * If the specified eventHandler is not null then this Session will operate in asynchronous mode, otherwise the Session will operate in Synchronous mode.
   * 
   * @param eventHandler the event handler if provided
   * @return the started Bloomberg session, not null
   * @throws RuntimeException if an error occurs
   */
  public Session createOpenSession(final EventHandler eventHandler) {
    Session session = createSession(eventHandler);
    try {
      if (session.start() == false) {
        throw new OpenGammaRuntimeException("Bloomberg session failed to start: " + SessionOptionsUtils.toString(getSessionOptions()));
      }
    } catch (InterruptedException ex) {
      // Interruption may mean that threads have still been created which must be killed. See PLAT-5309.
      try {
        s_logger.debug("Attempting to stop session which was created but not started");
        session.stop();
      } catch (Exception e) {
        s_logger.error("Can't stop session", e);
      }
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Bloomberg session failed to start: " + SessionOptionsUtils.toString(getSessionOptions()), ex);
    } catch (Exception ex) {
      // Failure from "start" to connect may mean that threads have still been created which must be killed. See PLAT-5309.
      try {
        s_logger.debug("Attempting to stop session which was created but not started");
        session.stop();
      } catch (Exception e) {
        s_logger.error("Can't stop session", e);
      }
      throw new OpenGammaRuntimeException("Bloomberg session failed to start: " + SessionOptionsUtils.toString(getSessionOptions()), ex);
    }
    return session;
  }

  /**
   * Creates a Bloomberg session in Synchronous mode that uses the session options.
   * <p>
   * The session is not opened.
   * 
   * @return the Bloomberg session, not null
   */
  public Session createSession() {
    return createSession(null);
  }

  /**
   * Creates a Bloomberg session that uses the session options.
   * 
   * <p>
   * The session is not opened.
   * If the specified eventHandler is not null then this Session will operate in asynchronous mode, otherwise the Session will operate in Synchronous mode.
   * @param eventHandler the event handler if provided
   * 
   * @return the Bloomberg session, not null
   */
  public Session createSession(final EventHandler eventHandler) {
    return new Session(getSessionOptions(), eventHandler);
  }

  //-------------------------------------------------------------------------
  @Override
  public void close() {
    // no action, as the connector holds no closeable state
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a description of this object suitable for debugging.
   * 
   * @return the description, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _name + "]";
  }

  /**
   * Registers a callback to be notified when {@link #notifyAvailabilityListeners} gets called.
   * 
   * @param listener the callback to register, not null
   */
  public void addAvailabilityListener(final AvailabilityListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  /**
   * Removes a callback previously registered with {@link #addAvailabilityListener}.
   * 
   * @param listener the listener to remove, not null
   */
  public void removeAvailabilityListener(final AvailabilityListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Calls the {@link AvailabilityListener#bloombergAvailable()} method on all registered listeners.
   * <p>
   * These calls are made inline - callers should take care not to be holding locks that may cause potential deadlocks and be aware that they too will be called if they are also registered as a
   * listener.
   */
  public void notifyAvailabilityListeners() {
    for (AvailabilityListener listener : _listeners) {
      s_logger.debug("Notifying availability to {}", listener);
      listener.bloombergAvailable();
    }
  }

  /**
   * Checks if the session needs authentication
   * 
   * @return true if authentication options is set, otherwise false
   */
  public boolean requiresAuthentication() {
    if (getSessionOptions() != null) {
      String authenticationOptions = StringUtils.trimToNull(getSessionOptions().authenticationOptions());
      return authenticationOptions != null;
    }
    return false;
  }

  /**
   * Returns the application name if available, otherwise null.
   * 
   * @return the application name if available.
   */
  public String getApplicationName() {
    String applicationName = null;
    if (getSessionOptions() != null && getSessionOptions().authenticationOptions() != null) {
      if (getSessionOptions().authenticationOptions().startsWith(BloombergConstants.AUTH_APP_PREFIX)) {
        applicationName = getSessionOptions().authenticationOptions().substring(BloombergConstants.AUTH_APP_PREFIX.length());
      }
    }
    return applicationName;
  }

}
