/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.permission;

import static com.opengamma.bbg.BloombergConstants.AUTH_SVC_NAME;
import static com.opengamma.bbg.BloombergConstants.REF_DATA_SVC_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Event.EventType;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.EventQueue;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.provider.permission.impl.AbstractPermissionCheckProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Bloomberg bpipe permission/EID check provider.
 */
public class BloombergBpipePermissionCheckProvider extends AbstractPermissionCheckProvider implements PermissionCheckProvider, Lifecycle {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergBpipePermissionCheckProvider.class);
  private static final Name AUTHORIZATION_SUCCESS = Name.getName("AuthorizationSuccess");
  private static final Name AUTHORIZATION_REVOKED = Name.getName("AuthorizationRevoked");
  private static final Name ENTITITLEMENT_CHANGED = Name.getName("EntitlementChanged");
  private static final int WAIT_TIME_MS = 10 * 1000; // 10 seconds
  private static final long DEFAULT_IDENTITY_EXPIRY = 24;

  private final LoadingCache<IdentityCacheKey, Identity> _userIdentityCache;
  private final BloombergConnector _bloombergConnector;
  private final AtomicBoolean _isRunning = new AtomicBoolean(false);
  private volatile Session _session;
  private volatile Service _apiAuthSvc;
  private volatile Service _apiRefDataSvc;
  private final long _identityExpiry;

  /**
   * Creates a bloomberg permission check provider with default identity expiry
   * 
   * @param bloombergConnector the Bloomberg connector, not null
   */
  public BloombergBpipePermissionCheckProvider(BloombergConnector bloombergConnector) {
    this(bloombergConnector, DEFAULT_IDENTITY_EXPIRY);
  }

  /**
   * Creates a bloomberg permission check provider
   * 
   * @param bloombergConnector the Bloomberg connector, not null
   * @param identityExpiry the identity expiry in hours, not null
   */
  public BloombergBpipePermissionCheckProvider(BloombergConnector bloombergConnector, long identityExpiry) {
    ArgumentChecker.notNull(bloombergConnector, "bloombergConnector");
    ArgumentChecker.notNull(bloombergConnector.getSessionOptions(), "bloombergConnector.sessionOptions");
    ArgumentChecker.isTrue(identityExpiry > 0, "identityExpiry must be positive");
    
    _identityExpiry = identityExpiry;
    LoadingCache<IdentityCacheKey, Identity> identityCache = CacheBuilder.newBuilder()
        .expireAfterWrite(_identityExpiry, TimeUnit.HOURS)
        .build(new CacheLoader<IdentityCacheKey, Identity>() {

          @Override
          public Identity load(IdentityCacheKey userCredential) throws Exception {
            return loadUserIdentity(userCredential);
          }

        });
    _bloombergConnector = bloombergConnector;
    _userIdentityCache = identityCache;
  }


  private Identity loadUserIdentity(IdentityCacheKey userCredential) throws IOException, InterruptedException {

    Request authRequest = _apiAuthSvc.createAuthorizationRequest();
    authRequest.set("emrsId", userCredential.getUserId());
    authRequest.set("ipAddress", userCredential.getIpAddress());
    Identity userIdentity = _session.createIdentity();

    s_logger.debug("Sending {}", authRequest);
    EventQueue eventQueue = new EventQueue();
    _session.sendAuthorizationRequest(authRequest, userIdentity, eventQueue, new CorrelationID(userCredential));
    Event event = eventQueue.nextEvent(WAIT_TIME_MS);
    if (event.eventType() == Event.EventType.RESPONSE || event.eventType() == Event.EventType.REQUEST_STATUS) {
      for (Message message : event) {
        if (message.messageType().equals(AUTHORIZATION_SUCCESS)) {
          return userIdentity;
        } else {
          s_logger.warn("User: {} authorization failed", userCredential.getUserId());
        }
      }
    }
    throw new OpenGammaRuntimeException(String.format("User: %s IpAdress: %s authorization failed", userCredential.getUserId(), userCredential.getIpAddress()));
  }

  @Override
  public PermissionCheckProviderResult isPermitted(PermissionCheckProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getIpAddress(), "request.ipAddress");
    ArgumentChecker.notNull(request.getRequestedPermissions(), "request.rquestedPermissions");
    ArgumentChecker.notNull(request.getUserIdBundle(), "request.userIdBundle");

    final String emrsId = StringUtils.trimToNull(request.getUserIdBundle().getValue(ExternalSchemes.BLOOMBERG_EMRSID));
    ArgumentChecker.notNull(emrsId, "user emrsid scheme");

    final Map<String, Boolean> permissionResult = initializeResult(request.getRequestedPermissions());
    if (request.getRequestedPermissions().size() > 0) {
      try {
        Identity identity = _userIdentityCache.get(IdentityCacheKey.of(request.getIpAddress(), emrsId));
        for (String permission : request.getRequestedPermissions()) {
          int eid = Integer.parseInt(permission);
          if (identity.hasEntitlements(new int[] {eid }, _apiRefDataSvc)) {
            permissionResult.put(permission, true);
          } else {
            s_logger.warn("user: {} is missing entitlements: {}", request.getUserIdBundle(), eid);
          }
        }
      } catch (ExecutionException | UncheckedExecutionException ex) {
        s_logger.warn(String.format("Bloomberg authorization failure for user: %s ipAddress: %s", request.getUserIdBundle(), request.getIpAddress()), ex);
      }
    }
    return new PermissionCheckProviderResult(permissionResult);
  }

  private Map<String, Boolean> initializeResult(Set<String> requestedPermissions) {
    final Map<String, Boolean> result = new HashMap<>();
    for (String permission : requestedPermissions) {
      result.put(permission, false);
    }
    return result;
  }

  private void printEvent(Event event) throws Exception {
    s_logger.debug("EventType: {}", event.eventType());
    MessageIterator msgIter = event.messageIterator();
    while (msgIter.hasNext()) {
      Message msg = msgIter.next();
      CorrelationID correlationId = msg.correlationID();
      if (correlationId != null) {
        s_logger.debug("Correlator: {}", correlationId);
      }
      Service service = msg.service();
      if (service != null) {
        s_logger.debug("Service: {}", service.name());
      }
      s_logger.debug("{}", msg);
    }
  }

  private void processAuthorizationEvent(Event event) {
    for (Message msg : event) {
      CorrelationID correlationId = msg.correlationID();
      IdentityCacheKey userCredential = (IdentityCacheKey) correlationId.object();
      if (msg.messageType() == AUTHORIZATION_REVOKED) {
        Element errorinfo = msg.getElement("reason");
        int code = errorinfo.getElementAsInt32("code");
        String reason = errorinfo.getElementAsString("message");
        s_logger.warn("Authorization revoked for emrsid: {} with code: {} and reason\n\t{}", userCredential.getUserId(), code, reason);
        //Remove identity from cache
        _userIdentityCache.invalidate(userCredential);
      } else if (msg.messageType() == ENTITITLEMENT_CHANGED) {
        s_logger.warn("Entitlements updated for emrsid: {}", userCredential.getUserId());
      }
    }
  }

  @Override
  public synchronized void start() {
    if (!isRunning()) {
      createSession();
      openServices();
      _isRunning.getAndSet(true);
    }
  }

  private void createSession() {
    SessionOptions sessionOptions = _bloombergConnector.getSessionOptions();
    s_logger.info("Connecting to {}:{}", sessionOptions.getServerHost(), sessionOptions.getServerPort());
    _session = new Session(sessionOptions, new SessionEventHandler());
    boolean sessionStarted;
    try {
      sessionStarted = _session.start();
    } catch (IOException | InterruptedException ex) {
      throw new OpenGammaRuntimeException(String.format("Error opening session to %s:%s", sessionOptions.getServerHost(), sessionOptions.getServerPort()), ex);
    }
    if (!sessionStarted) {
      throw new OpenGammaRuntimeException(String.format("Failed to start session to %s:%s", sessionOptions.getServerHost(), sessionOptions.getServerPort()));
    }
  }

  private void openServices() {
    SessionOptions sessionOptions = _bloombergConnector.getSessionOptions();
    try {
      if (!_session.openService(AUTH_SVC_NAME)) {
        throw new OpenGammaRuntimeException(String.format("Failed to open service: %s to %s:%s", AUTH_SVC_NAME, sessionOptions.getServerHost(), sessionOptions.getServerPort()));
      }
      if (!_session.openService(REF_DATA_SVC_NAME)) {
        throw new OpenGammaRuntimeException(String.format("Failed to open service: %s to %s:%s", REF_DATA_SVC_NAME, sessionOptions.getServerHost(), sessionOptions.getServerPort()));
      }
    } catch (InterruptedException | IOException ex) {
      throw new OpenGammaRuntimeException(String.format("Failed to start session to %s:%s", sessionOptions.getServerHost(), sessionOptions.getServerPort()), ex);
    }
    _apiAuthSvc = _session.getService(AUTH_SVC_NAME);
    _apiRefDataSvc = _session.getService(REF_DATA_SVC_NAME);
  }

  @Override
  public void stop() {
    if (isRunning()) {
      try {
        _session.stop();
      } catch (InterruptedException ex) {
        Thread.interrupted();
        s_logger.warn("Thread interrupted while trying to shut down bloomberg session");
      }
    }
  }

  @Override
  public boolean isRunning() {
    return _isRunning.get();
  }

  private class SessionEventHandler implements EventHandler {

    public void processEvent(Event event, Session session) {
      try {
        switch (event.eventType().intValue()) {
        //          case EventType.Constants.SESSION_STATUS:
        //          case EventType.Constants.SERVICE_STATUS:
        //          case EventType.Constants.SUBSCRIPTION_STATUS:
        //          case EventType.Constants.REQUEST_STATUS:
        //          case EventType.Constants.RESPONSE:
          case EventType.Constants.AUTHORIZATION_STATUS:
            processAuthorizationEvent(event);
            break;
          case EventType.Constants.SUBSCRIPTION_DATA:
            printEvent(event);
            break;
        }
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Error processing bloomberg events", ex);
      }
    }
  }

}
