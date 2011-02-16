/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.SingletonFactoryBean;

/**
 * An implementation of {@link EndPointDescriptionProvider} that produces values from a local or remote URI.
 */
public class UriEndPointDescriptionProviderFactoryBean extends SingletonFactoryBean<UriEndPointDescriptionProvider> {

  private static final Logger s_logger = LoggerFactory.getLogger(UriEndPointDescriptionProviderFactoryBean.class);

  private static final boolean s_enableIPv4 = System.getProperty("com.opengamma.transport.jaxrs.UriEndPointDescriptionProviderFactoryBean.disableIPv4") == null;
  private static final boolean s_enableIPv6 = System.getProperty("com.opengamma.transport.jaxrs.UriEndPointDescriptionProviderFactoryBean.enableIPv6") != null;

  private final List<String> _uris = new LinkedList<String>();

  /**
   * Sets an absolute URI. 
   * 
   * @param uri the absolute URI, e.g. {@code http://hostname.domain:port/foo/bar}
   */
  public void setAbsolute(final String uri) {
    _uris.add(uri);
  }

  private void loadInterfaceAddress(final NetworkInterface iface, final Collection<String> addresses) {
    final Enumeration<NetworkInterface> ni = iface.getSubInterfaces();
    while (ni.hasMoreElements()) {
      loadInterfaceAddress(ni.nextElement(), addresses);
    }
    final Enumeration<InetAddress> ai = iface.getInetAddresses();
    while (ai.hasMoreElements()) {
      final InetAddress a = ai.nextElement();
      if (a.isLoopbackAddress()) {
        continue;
      }
      if (a instanceof Inet4Address) {
        if (s_enableIPv4) {
          addresses.add(a.getHostAddress());
        }
      } else if (a instanceof Inet6Address) {
        if (s_enableIPv6) {
          addresses.add("[" + a.getHostAddress() + "]");
        }
      }
    }
  }

  private Collection<String> getLocalHttpConnections() {
    // TODO: is this property a quirk of our installation; or should we check something different ?
    Object jettyPort = System.getProperty("jetty.port");
    if (jettyPort == null) {
      jettyPort = "80";
    }
    final List<String> addresses = new LinkedList<String>();
    try {
      Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
      while (ni.hasMoreElements()) {
        loadInterfaceAddress(ni.nextElement(), addresses);
      }
    } catch (IOException e) {
      s_logger.warn("Error resolving local addresses; no local connections available", e);
      return Collections.emptySet();
    }
    final List<String> connections = new ArrayList<String>(addresses.size());
    for (String address : addresses) {
      connections.add("http://" + address + ":" + jettyPort);
    }
    return connections;
  }

  private Collection<String> getLocalHttpsConnections() {
    // TODO test if the local Jetty context has SSL
    s_logger.warn("Secure local connections not available");
    return getLocalHttpConnections();
  }

  private void setLocal(final String path, final Collection<String> locals) {
    for (String local : locals) {
      final String uri = local + path;
      s_logger.debug("Publishing {}", uri);
      _uris.add(uri);
    }
  }

  /**
   * Sets a local URI using the default host and port from the containing context.
   * 
   * @param path the local path, e.g. {@code /foo/bar}
   */
  public void setLocal(final String path) {
    setLocal(path, getLocalHttpConnections());
  }

  /**
   * Sets a local URI using the default host and secure port from the containing context. If
   * no secure connection is available, acts the same as {@link #setLocalUrl}.
   * 
   * @param path the local path, e.g. {@code /foo/bar}
   */
  public void setLocalSecure(final String path) {
    setLocal(path, getLocalHttpsConnections());
  }

  @Override
  protected UriEndPointDescriptionProvider createObject() {
    return new UriEndPointDescriptionProvider(_uris);
  }

}
