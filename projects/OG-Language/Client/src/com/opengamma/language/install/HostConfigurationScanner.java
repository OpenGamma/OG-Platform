/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

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
  private RestClient _restClient;
  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;

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

  public String getHost() {
    return _host;
  }

  public int getPort() {
    return _port;
  }

  public String getBase() {
    return _base;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setRestClient(final RestClient client) {
    _restClient = client;
  }

  public RestClient getRestClient() {
    return _restClient;
  }

  @Override
  public void run() {
    final RestTarget target = new RestTarget("http://" + getHost() + ":" + getPort() + getBase());
    try {
      RestClient client = getRestClient();
      if (client == null) {
        client = RestClient.getInstance(getFudgeContext(), null);
      }
      s_logger.debug("Fetching {}", target);
      FudgeMsg configurations = client.getMsg(target);
      if (configurations == null) {
        s_logger.info("No configuration document at {}", target);
        return;
      }
      final Set<Configuration> found = Sets.newHashSetWithExpectedSize(configurations.getNumFields());
      for (FudgeField field : configurations) {
        final FudgeMsg configuration = configurations.getFieldValue(FudgeMsg.class, field);
        final String description = configuration.getString(DESCRIPTION_FIELD);
        if (description != null) {
          s_logger.debug("Found {}/{}", field.getName(), description);
          found.add(new Configuration(target.resolveBase(field.getName()).getURI(), description));
        } else {
          s_logger.debug("Ignoring {} - no description", field.getName());
        }
      }
      addConfigurations(found);
    } catch (RuntimeException e) {
      s_logger.info("Couldn't fetch configuration from {}", target);
      s_logger.debug("Caught exception", e);
    } finally {
      complete();
    }
  }

}
