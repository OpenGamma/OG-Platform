/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

/**
 * Uniquely identifies a {@link ViewClient} within the user's context. The key comprises the
 * view name, execution options and whether to use a shared or private process. These are
 * the connection parameters for attaching the identified client to the process.
 * <p>
 * The view name and execution options are encoded as a {@link ViewClientDescriptor} string.
 */
public final class ViewClientKey {
  // TODO: this is only public while OG-Excel references it

  private final String _clientDescriptorString;
  private final boolean _useSharedProcess;
  private volatile ViewClientDescriptor _clientDescriptor;

  public ViewClientKey(final String clientDescriptor, final boolean useSharedProcess) {
    _clientDescriptorString = clientDescriptor;
    _useSharedProcess = useSharedProcess;
  }

  public ViewClientKey(final ViewClientDescriptor clientDescriptor, final boolean useSharedProcess) {
    this(clientDescriptor.encode(), useSharedProcess);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _clientDescriptor.hashCode();
    result = prime * result + (_useSharedProcess ? 1231 : 1237);
    return result;
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
    return true;
  }

  @Override
  public String toString() {
    return "ViewClientKey[objectDescriptor=" + _clientDescriptor + ", useSharedProcess=" + _useSharedProcess + "]";
  }

}
