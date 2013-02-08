/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

/**
 * Publishes configuration details about the ActiveMQ JMS environment. For example the broker URL.
 */
public class ActiveMQJmsConfiguration implements Supplier<String> {

  private static final Logger s_logger = LoggerFactory.getLogger(ActiveMQJmsConfiguration.class);

  private static final int DEFAULT_PORT = 61616;

  /**
   * The full URI to publish. If not set, the {@link #_brokerHost} and {@link #_brokerPort} attributes will be used.
   */
  private String _brokerURL;

  /**
   * The host to publish if {@link #_brokerURL} is not set. If not set, the local host address will be determined.
   */
  private String _brokerHost;

  /**
   * The port to publish if {@link #_brokerURL} is not set. IF not set, the default port will be used.
   */
  private Integer _brokerPort;

  /**
   * The timeout parameter
   */
  private Integer _timeout;

  private volatile String _generatedURL;

  public String getBrokerURL() {
    return _brokerURL;
  }

  public void setBrokerURL(final String brokerURL) {
    _brokerURL = brokerURL;
  }

  public String getBrokerHost() {
    return _brokerHost;
  }

  public void setBrokerHost(final String brokerHost) {
    _brokerHost = brokerHost;
  }

  public Integer getBrokerPort() {
    return _brokerPort;
  }

  public void setBrokerPort(final Integer brokerPort) {
    _brokerPort = brokerPort;
  }

  public Integer getTimeout() {
    return _timeout;
  }

  public void setTimeout(final Integer timeout) {
    _timeout = timeout;
  }

  private void getLocalHosts(final NetworkInterface iface, final Collection<String> hosts) {
    final Enumeration<NetworkInterface> ni = iface.getSubInterfaces();
    while (ni.hasMoreElements()) {
      getLocalHosts(ni.nextElement(), hosts);
    }
    final Enumeration<InetAddress> ai = iface.getInetAddresses();
    while (ai.hasMoreElements()) {
      final InetAddress a = ai.nextElement();
      if (a.isLoopbackAddress() || !(a instanceof Inet4Address)) {
        continue;
      }
      hosts.add(a.getHostAddress());
    }
  }

  private void getLocalHosts(final Collection<String> hosts) {
    try {
      final Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
      while (ni.hasMoreElements()) {
        getLocalHosts(ni.nextElement(), hosts);
      }
    } catch (final IOException e) {
      s_logger.warn("Error resolving local addresses", e);
    }
  }

  private String generateURL() {
    final Collection<String> hosts;
    if (getBrokerHost() != null) {
      hosts = Collections.singleton(getBrokerHost());
    } else {
      hosts = new ArrayList<String>();
      getLocalHosts(hosts);
    }
    final String port = Integer.toString((getBrokerPort() != null) ? getBrokerPort() : DEFAULT_PORT);
    final StringBuilder sb = new StringBuilder();
    sb.append("failover:(");
    boolean comma = false;
    for (final String host : hosts) {
      if (comma) {
        sb.append(',');
      } else {
        comma = true;
      }
      sb.append("tcp://").append(host).append(':').append(port).append("?daemon=true");
    }
    sb.append(')');
    if (getTimeout() != null) {
      sb.append("?timeout=").append(getTimeout());
    }
    return sb.toString();
  }

  // Supplier

  @Override
  public String get() {
    if (getBrokerURL() != null) {
      return getBrokerURL();
    }
    if (_generatedURL == null) {
      _generatedURL = generateURL();
    }
    return _generatedURL;
  }

}
