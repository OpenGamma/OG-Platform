/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link EndPointDescriptionProvider} that describes a socket address.
 */
public class SocketEndPointDescriptionProvider implements EndPointDescriptionProvider {

  /**
   * Type of connection. Always {@link #TYPE_VALUE}.
   */
  public static final String TYPE_KEY = "type";

  /**
   * Value of the type of connection.
   */
  public static final String TYPE_VALUE = "Socket";

  /**
   * Connection address.
   */
  public static final String ADDRESS_KEY = "address";

  /**
   * Connection port.
   */
  public static final String PORT_KEY = "port";

  /**
   * The address to connect to. Defaults to the local host.
   */
  private String _address = "127.0.0.1";

  /**
   * The port number to connect to.
   */
  private int _port;

  /**
   * Sets the connection address.
   * 
   * @param address the address to connect to, not null
   */
  public void setAddress(final String address) {
    ArgumentChecker.notNull(address, "address");
    _address = address;
  }

  /**
   * Returns the address that will be connected to.
   * 
   * @return the target address, not null
   */
  public String getAddress() {
    return _address;
  }

  /**
   * Sets the port to connect to.
   * 
   * @param port the port to connect to
   */
  public void setPort(final int port) {
    _port = port;
  }

  /**
   * Returns the port to connect to.
   * 
   * @return the target port
   */
  public int getPort() {
    return _port;
  }

  // EndPointDescriptionProvider

  @Override
  public FudgeMsg getEndPointDescription(final FudgeContext fudgeContext) {
    final MutableFudgeMsg msg = fudgeContext.newMessage();
    msg.add(TYPE_KEY, TYPE_VALUE);
    msg.add(ADDRESS_KEY, getAddress());
    msg.add(PORT_KEY, getPort());
    return msg;
  }

}
