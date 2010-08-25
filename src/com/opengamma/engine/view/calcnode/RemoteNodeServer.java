/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeInitMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeReadyMessage;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Server end to RemoteNodeClient to receive requests from remote calculation nodes and marshal
 * them into RemoteNodeJobInvokers that a JobDispatcher can then use.
 */
public class RemoteNodeServer implements FudgeConnectionReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeServer.class);

  private final JobInvokerRegister _jobInvokerRegister;
  private final IdentifierMap _identifierMap;
  private final ExecutorService _executorService = Executors.newCachedThreadPool();
  private final Set<Capability> _capabilitiesToAdd = new HashSet<Capability>();
  private final Set<Capability> _capabilitiesToRemove = new HashSet<Capability>();
  private Set<Capability> _capabilitiesOverride;

  public RemoteNodeServer(final JobInvokerRegister jobInvokerRegister, final IdentifierMap identifierMap) {
    _jobInvokerRegister = jobInvokerRegister;
    _identifierMap = identifierMap;
  }

  /**
   * Specify a capability to always add to those the remote node declares.
   * 
   * @param capability the capability to add, not {@code null}
   */
  public void addCapability(final Capability capability) {
    ArgumentChecker.notNull(capability, "capability");
    if (_capabilitiesOverride != null) {
      throw new IllegalStateException("Capability override already set");
    }
    _capabilitiesToAdd.add(capability);
    _capabilitiesToRemove.remove(capability);
  }

  /**
   * Specify a capability to remove from the remote node declaration if present.
   * 
   * @param capability the capability to remove, not {@code null}
   */
  public void removeCapability(final Capability capability) {
    ArgumentChecker.notNull(capability, "capability");
    if (_capabilitiesOverride != null) {
      throw new IllegalStateException("Capability override already set");
    }
    _capabilitiesToAdd.remove(capability);
    _capabilitiesToRemove.add(capability);
  }

  /**
   * Specify capabilities to always add to those the remote node declares.
   * 
   * @param capabilities the capabilities to add, not {@code null}
   */
  public void addCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    if (_capabilitiesOverride != null) {
      throw new IllegalStateException("Capability override already set");
    }
    _capabilitiesToAdd.addAll(capabilities);
    _capabilitiesToRemove.removeAll(capabilities);
  }

  /**
   * Specify capabilities to always remove from those the remote node declares.
   * 
   * @param capabilities the capabilities to remove, not {@code null}
   */
  public void removeCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    if (_capabilitiesOverride != null) {
      throw new IllegalStateException("Capability override already set");
    }
    _capabilitiesToAdd.removeAll(capabilities);
    _capabilitiesToRemove.addAll(capabilities);
  }

  /**
   * Specify capabilities to override those the remote node declares.
   * 
   * @param capabilities the capabilities to use, or {@code null} to cancel the override.
   */
  public void setCapabilities(final Collection<Capability> capabilities) {
    _capabilitiesToAdd.clear();
    _capabilitiesToRemove.clear();
    _capabilitiesOverride = new HashSet<Capability>(capabilities);
  }

  protected JobInvokerRegister getJobInvokerRegister() {
    return _jobInvokerRegister;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  protected IdentifierMap getIdentifierMap() {
    return _identifierMap;
  }

  @Override
  public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message, final FudgeConnection connection) {
    final FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
    final RemoteCalcNodeMessage remoteCalcNodeMessage = context.fudgeMsgToObject(RemoteCalcNodeMessage.class, message.getMessage());
    if (remoteCalcNodeMessage instanceof RemoteCalcNodeReadyMessage) {
      s_logger.info("Remote node connected - {}", connection);
      final FudgeSerializationContext scontext = new FudgeSerializationContext(fudgeContext);
      final RemoteCalcNodeInitMessage response = new RemoteCalcNodeInitMessage();
      connection.getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(response), RemoteCalcNodeInitMessage.class, RemoteCalcNodeMessage.class));
      final RemoteNodeJobInvoker invoker = new RemoteNodeJobInvoker(getExecutorService(), (RemoteCalcNodeReadyMessage) remoteCalcNodeMessage, connection, getIdentifierMap());
      if (_capabilitiesOverride != null) {
        invoker.setCapabilities(_capabilitiesOverride);
      } else {
        invoker.addCapabilities(_capabilitiesToAdd);
        invoker.removeCapabilities(_capabilitiesToRemove);
      }
      getJobInvokerRegister().registerJobInvoker(invoker);
    } else {
      s_logger.warn("Unexpected message {}", remoteCalcNodeMessage);
    }
  }
}
