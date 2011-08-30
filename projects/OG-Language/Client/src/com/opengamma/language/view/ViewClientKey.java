/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.ArgumentChecker;

/**
 * Uniquely identifies a {@link ViewClient} within the user's context. The key comprises the
 * view name, execution options and whether to use a shared or private process. These are
 * the connection parameters for attaching the identified client to the process.
 * <p>
 * An optional name can be specified to allow different clients to be distinguished, e.g. so that
 * different properties can be set on each.
 * <p>
 * The view name and execution options are encoded as a {@link ViewClientDescriptor} string.
 */
public final class ViewClientKey {

  private static final String DEFAULT_CLIENT_NAME = "Default";

  private final String _clientDescriptorString;
  private final boolean _useSharedProcess;
  private final String _clientName;
  private volatile ViewClientDescriptor _clientDescriptor;

  public ViewClientKey(final String clientDescriptor, final boolean useSharedProcess) {
    this(clientDescriptor, useSharedProcess, DEFAULT_CLIENT_NAME);
  }

  public ViewClientKey(final String clientDescriptor, final boolean useSharedProcess, final String clientName) {
    ArgumentChecker.notNull(clientDescriptor, "clientDescriptor");
    ArgumentChecker.notNull(clientName, "clientName");
    _clientDescriptorString = clientDescriptor;
    _useSharedProcess = useSharedProcess;
    _clientName = clientName;
  }

  public ViewClientKey(final ViewClientDescriptor clientDescriptor, final boolean useSharedProcess) {
    this(clientDescriptor.encode(), useSharedProcess, DEFAULT_CLIENT_NAME);
  }

  public ViewClientKey(final ViewClientDescriptor clientDescriptor, final boolean useSharedProcess, final String clientName) {
    this(clientDescriptor.encode(), useSharedProcess, clientName);
    _clientDescriptor = clientDescriptor;
  }

  public boolean isSharedProcess() {
    return _useSharedProcess;
  }

  public String getClientDescriptorString() {
    return _clientDescriptorString;
  }

  public ViewClientDescriptor getClientDescriptor() {
    if (_clientDescriptor == null) {
      _clientDescriptor = ViewClientDescriptor.decode(getClientDescriptorString());
    }
    return _clientDescriptor;
  }

  public String getClientName() {
    return _clientName;
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc += (hc << 4) + _clientDescriptorString.hashCode();
    hc += (hc << 4) + (_useSharedProcess ? 1 : 0);
    hc += (hc << 4) + _clientName.hashCode();
    return hc;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ViewClientKey)) {
      return false;
    }
    ViewClientKey other = (ViewClientKey) obj;
    if (!_clientDescriptor.equals(other._clientDescriptor)) {
      return false;
    }
    if (_useSharedProcess != other._useSharedProcess) {
      return false;
    }
    return _clientName.equals(other._clientName);
  }

  @Override
  public String toString() {
    return "ViewClientKey[objectDescriptor=" + _clientDescriptor + ", useSharedProcess=" + _useSharedProcess + "]";
  }

}
