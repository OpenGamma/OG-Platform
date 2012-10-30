/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.IndicatorType;

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

  private final boolean _useSharedProcess;
  private final String _clientName;
  private final ViewClientDescriptor _clientDescriptor;

  public ViewClientKey(final ViewClientDescriptor clientDescriptor, final boolean useSharedProcess) {
    this(clientDescriptor, useSharedProcess, DEFAULT_CLIENT_NAME);
  }

  public ViewClientKey(final ViewClientDescriptor clientDescriptor, final boolean useSharedProcess, final String clientName) {
    ArgumentChecker.notNull(clientDescriptor, "clientDescriptor");
    ArgumentChecker.notNull(clientName, "clientName");
    _clientDescriptor = clientDescriptor;
    _useSharedProcess = useSharedProcess;
    _clientName = clientName;
  }

  public boolean isSharedProcess() {
    return _useSharedProcess;
  }

  public ViewClientDescriptor getClientDescriptor() {
    return _clientDescriptor;
  }

  public String getClientName() {
    return _clientName;
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc += (hc << 4) + _clientDescriptor.hashCode();
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

  private static final String DESCRIPTOR_FIELD = "viewClient";
  private static final String USE_SHARED_PROCESS_FIELD = "useSharedProcess";
  private static final String CLIENT_NAME_FIELD = "clientName";

  /**
   * Produces the Fudge message encoding of the ViewClientKey:
   * <pre>
   * message ViewClientKey {
   *   required ViewClientDescriptor viewClient;
   *   optional indicator useSharedProcess;
   *   optional string clientName;
   * }
   * </pre>
   * 
   * @param fudgeSerializer the Fudge serialization service
   * @return the Fudge message representation
   */
  public FudgeMsg toFudgeMsg(final FudgeSerializer fudgeSerializer) {
    final MutableFudgeMsg msg = fudgeSerializer.newMessage();
    msg.add(DESCRIPTOR_FIELD, _clientDescriptor.toFudgeMsg(fudgeSerializer));
    if (_useSharedProcess) {
      msg.add(USE_SHARED_PROCESS_FIELD, IndicatorType.INSTANCE);
    }
    if (!DEFAULT_CLIENT_NAME.equals(_clientName)) {
      msg.add(CLIENT_NAME_FIELD, _clientName);
    }
    return msg;
  }

  public static ViewClientKey fromFudgeMsg(final FudgeDeserializer fudgeDeserializer, final FudgeMsg msg) {
    final ViewClientDescriptor descriptor = ViewClientDescriptor.fromFudgeMsg(fudgeDeserializer, msg.getMessage(DESCRIPTOR_FIELD));
    final boolean useSharedProcess = msg.hasField(USE_SHARED_PROCESS_FIELD);
    final String clientName = msg.getString(CLIENT_NAME_FIELD);
    if (clientName != null) {
      return new ViewClientKey(descriptor, useSharedProcess, clientName);
    } else {
      return new ViewClientKey(descriptor, useSharedProcess);
    }
  }

}
