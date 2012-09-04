/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

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
 * This class does not perform any connecting.
 * Instead it acts as a data holder for connectivity.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class BloombergConnector implements Connector {

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
   * Creates an instance.
   * 
   * @param name  the configuration name, not null
   * @param sessionOptions  the Bloomberg session options, not null
   */
  public BloombergConnector(String name, SessionOptions sessionOptions) {
    this(name, sessionOptions, NullBloombergReferenceDataStatistics.INSTANCE);
  }

  /**
   * Creates an instance.
   * 
   * @param name  the configuration name, not null
   * @param sessionOptions  the Bloomberg session options, not null
   * @param statistics  the Bloomberg statistics, not null
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
   * @param name  the configuration name, not null
   * @param statistics  the Bloomberg statistics, not null
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

  //-------------------------------------------------------------------------
  /**
   * Creates and starts a new Bloomberg {@code Session}.
   * <p>
   * The session is started synchronously.
   * The connector does not retain the state of the session, thus the caller
   * is responsible for its lifecycle.
   * 
   * @return the started Bloomberg session, not null
   * @throws RuntimeException if an error occurs
   */
  public Session createOpenSession() {
    Session session = createSession();
    try {
      if (session.start() == false) {
        throw new OpenGammaRuntimeException("Bloomberg session failed to start: " + SessionOptionsUtils.toString(getSessionOptions()));
      }
    } catch (InterruptedException ex) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Bloomberg session failed to start: " + SessionOptionsUtils.toString(getSessionOptions()), ex);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Bloomberg session failed to start: " + SessionOptionsUtils.toString(getSessionOptions()), ex);
    }
    return session;
  }

  /**
   * Creates a Bloomberg session that uses the session options.
   * <p>
   * The session is not opened.
   * 
   * @return the Bloomberg session, not null
   */
  public Session createSession() {
    return new Session(getSessionOptions());
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

}
