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
  
  private String _local;
  private int _port = 80;
  private int _securePort = 443;
  private boolean _secure;

  //-------------------------------------------------------------------------
  /**
   * Sets an absolute URI. 
   * 
   * @param uri the absolute URI, e.g. {@code http://hostname.domain:port/foo/bar}
   */
  public void setAbsolute(final String uri) {
    _uris.add(uri);
  }
  
  /**
   * Sets a local path using the default host and port.
   * 
   * @param local  the local path, e.g. {@code /foo/bar}
   */
  public void setLocal(final String local) {
    _local = local;
  }

  /**
   * Sets the default port
   * 
   * @param port  the default port
   */
  public void setPort(final int port) {
    _port = port;
  }
  
  public int getPort() {
    return _port;
  }

  public void setSecurePort(final int securePort) {
    _securePort = securePort;
  }
  
  public int getSecurePort() {
    return _securePort;
  }

  public void setSecure(final boolean isSecure) {
    _secure = isSecure;
  }

  public boolean isSecure() {
    return _secure;
  }

  //-------------------------------------------------------------------------
  @Override
  protected UriEndPointDescriptionProvider createObject() {
    if (_local != null) {
      if (_secure) {
        s_logger.warn("Secure local connections not available - using unsecured connections");
      }
      Collection<String> localAddresses = getLocalNetworkAddresses();
      for (String address : localAddresses) {
        String uri = "http://" + address + ":" + _port + _local;
        _uris.add(uri);
        s_logger.debug("Publishing {}", uri);
      }
    }
    
    return new UriEndPointDescriptionProvider(_uris);
  }
  
  //-------------------------------------------------------------------------
  private Collection<String> getLocalNetworkAddresses() {
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
    return addresses;
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

}
