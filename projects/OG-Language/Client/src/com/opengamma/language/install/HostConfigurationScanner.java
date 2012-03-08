/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.install;

import java.net.URI;
import java.util.Collection;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.language.config.RemoteConfiguration;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * {@link ConfigurationScanner} that produces the configurations available from a given host.
 */
public class HostConfigurationScanner extends AbstractConfigurationScanner implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(HostConfigurationScanner.class);

  /**
   * Default host name.
   */
  public static final String DEFAULT_HOST = "localhost";
  /**
   * Default port.
   */
  public static final int DEFAULT_PORT = 8080;

  /**
   * Default base.
   */
  public static final String DEFAULT_BASE = "/jax/configuration/";

  private static final String DESCRIPTION_FIELD = "description";

  private final String _host;
  private final int _port;
  private final String _base;
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  public HostConfigurationScanner(final String host) {
    this(host, DEFAULT_PORT, DEFAULT_BASE);
  }

  public HostConfigurationScanner(final String host, final int port) {
    this(host, port, DEFAULT_BASE);
  }

  public HostConfigurationScanner(final String host, final String base) {
    this(host, DEFAULT_PORT, base);
  }

  public HostConfigurationScanner(final String host, final int port, final String base) {
    ArgumentChecker.notNull(host, "host");
    ArgumentChecker.notNull(base, "base");
    s_logger.debug("Created host configuration scanner for {}:{}{}", new Object[] {host, port, base });
    _host = host;
    _port = port;
    _base = base;
  }

  //-------------------------------------------------------------------------
  public String getHost() {
    return _host;
  }

  public int getPort() {
    return _port;
  }

  public String getBase() {
    return _base;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  public void run() {
    final URI uri = URI.create("http://" + getHost() + ":" + getPort() + getBase());
    try {
      FudgeMsg configurations = new RemoteConfiguration(uri).getConfigurationMsg();
      if (configurations == null) {
        s_logger.info("No configuration document at {}", uri);
        return;
      }
      final Collection<Configuration> found = Sets.newHashSetWithExpectedSize(configurations.getNumFields());
      for (FudgeField field : configurations) {
        final FudgeMsg configuration = configurations.getFieldValue(FudgeMsg.class, field);
        final String description = configuration.getString(DESCRIPTION_FIELD);
        if (description != null) {
          s_logger.debug("Found {}/{}", field.getName(), description);
          found.add(new Configuration(uri.resolve(field.getName()), description));
        } else {
          s_logger.debug("Ignoring {} - no description", field.getName());
        }
      }
      addConfigurations(found);
      
    } catch (RuntimeException e) {
      s_logger.info("Couldn't fetch configuration from {}", uri);
      s_logger.debug("Caught exception", e);
    } finally {
      complete();
    }
  }

}
