/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.permission;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.Duration;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Event.EventType;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.EventQueue;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergPermissions;
import com.opengamma.bbg.SessionProvider;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.provider.permission.impl.AbstractPermissionCheckProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Bloomberg B-PIPE permission/EID check provider.
 * <p>
 * This provider provides the service of checking whether a user has permission
 * to access a piece of Bloomberg reference data.
 * <p>
 * In order to check permissions, Bloomberg requires an {@code Identity} object.
 * The {@code Identity} is directly connected to Bloomberg and is updated as entitlements change.
 * The provider request must contain the EMRS user id and IP address of a logged on BPS terminal.
 * These are checked by Bloomberg when creating an {@code Identity}.
 * Failure at this stage returns a result with an authentication error.
 * <p>
 * Once an {@code Identity} is obtained, the requested permissions are checked against it.
 * Only EID permissions in the request are checked, other permissions are returned as denied.
 * The EID is created and extracted using {@link BloombergPermissions}.
 * Failure at this stage returns a result with an authorization error.
 */
public final class BloombergBpipePermissionCheckProvider
    extends AbstractPermissionCheckProvider
    implements PermissionCheckProvider, Lifecycle {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergBpipePermissionCheckProvider.class);

  private static final Name AUTHORIZATION_SUCCESS = Name.getName("AuthorizationSuccess");
  private static final Name AUTHORIZATION_FAILURE = Name.getName("AuthorizationFailure");
  private static final Name AUTHORIZATION_REVOKED = Name.getName("AuthorizationRevoked");
  private static final Name ENTITITLEMENT_CHANGED = Name.getName("EntitlementChanged");
  private static final int WAIT_TIME_MS = 10 * 1000; // 10 seconds
  private static final Duration DEFAULT_IDENTITY_EXPIRY = Duration.ofHours(24);

  private final LoadingCache<IdentityCacheKey, Identity> _userIdentityCache;
  private final BloombergConnector _bloombergConnector;
  private final AtomicBoolean _isRunning = new AtomicBoolean(false);
  private volatile Session _session;
  private volatile Service _apiAuthSvc;
  private volatile Service _apiRefDataSvc;
  /** Creates and manages the Bloomberg session and service. */
  private final SessionProvider _sessionProvider;

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
  public BloombergBpipePermissionCheckProvider(BloombergConnector bloombergConnector, Duration identityExpiry) {
    ArgumentChecker.notNull(bloombergConnector, "bloombergConnector");
    ArgumentChecker.notNull(bloombergConnector.getSessionOptions(), "bloombergConnector.sessionOptions");
    ArgumentChecker.isTrue(identityExpiry.getSeconds() > 0, "identityExpiry must be positive");
    ArgumentChecker.isTrue(bloombergConnector.requiresAuthentication(), "authentication options must be set");
    
    _userIdentityCache = createUserIdentityCache(identityExpiry);
    _bloombergConnector = bloombergConnector;

    List<String> serviceNames = Lists.newArrayList(
        BloombergConstants.AUTH_SVC_NAME, BloombergConstants.REF_DATA_SVC_NAME);
    SessionEventHandler eventHandler = new SessionEventHandler();
    _sessionProvider = new SessionProvider(_bloombergConnector, serviceNames, eventHandler);
  }

  //-------------------------------------------------------------------------
  @Override
  public PermissionCheckProviderResult isPermitted(PermissionCheckProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    // validate
    if (isRunning() == false) {
      return PermissionCheckProviderResult.ofAuthenticationError(
          "Bloomberg permission check found connection not running");
    }
    String emrsId = StringUtils.trimToNull(request.getUserIdBundle().getValue(ExternalSchemes.BLOOMBERG_EMRSID));
    if (emrsId == null) {
      return PermissionCheckProviderResult.ofAuthenticationError(
          "Bloomberg permission check request did not contain an EMRS user ID");
    }
    if (request.getNetworkAddress() == null) {
      return PermissionCheckProviderResult.ofAuthenticationError(
          "Bloomberg permission check request did not contain a network address");
    }
    // obtain user identity
    Identity userIdentity;
    try {
      userIdentity = _userIdentityCache.get(IdentityCacheKey.of(request.getNetworkAddress(), emrsId));
    } catch (ExecutionException | UncheckedExecutionException ex) {
      return processAuthenticationError(request, ex);
    }
    // check whether identity has the permissions
    return checkPermissions(request, userIdentity);
  }

  // checks the requested permissions against the identity object
  private PermissionCheckProviderResult checkPermissions(
      PermissionCheckProviderRequest request, Identity userIdentity) {
    
    try {
      // evaluate permissions one by one to meet our API
      Map<String, Boolean> result = new HashMap<>();
      for (String permission : request.getRequestedPermissions()) {
        if (BloombergPermissions.isEid(permission)) {
          int eid = BloombergPermissions.extractEid(permission);
          boolean permitted = userIdentity.hasEntitlements(new int[] {eid}, _apiRefDataSvc);
          result.put(permission, permitted);
        } else {
          // permissions other than EID permissions are returned as false without error
          result.put(permission, false);
        }
      }
      return PermissionCheckProviderResult.of(result);
      
    } catch (RuntimeException ex) {
      String msg = String.format("Bloomberg authorization failure for user: %s IpAddress: %s",
          request.getUserIdBundle(), request.getNetworkAddress());
      s_logger.warn(msg, ex);
      return PermissionCheckProviderResult.ofAuthorizationError(
          "Bloomberg authorization error: " + ex.getCause().getClass().getName() + ": " + ex.getMessage());
    }
  }

  // handles any errors during authentiction
  private PermissionCheckProviderResult processAuthenticationError(
      PermissionCheckProviderRequest request, Exception ex) {
    
    String msg = String.format("Bloomberg authentication failure for user: %s IpAddress: %s",
        request.getUserIdBundle(), request.getNetworkAddress());
    if (ex.getCause() == null) {
      s_logger.warn(msg, ex);
      return PermissionCheckProviderResult.ofAuthenticationError(
          "Bloomberg authentication error: Unknown cause: " + ex.getMessage());
    } else if (ex.getCause() instanceof UnauthenticatedException) {
      s_logger.debug(msg);
      return PermissionCheckProviderResult.ofAuthenticationError(
          "Bloomberg authentication failed: " + ex.getCause().getMessage());
    } else {
      s_logger.warn(msg, ex.getCause());
      return PermissionCheckProviderResult.ofAuthenticationError(
          "Bloomberg authentication error: " + ex.getCause().getClass().getName() + ": " + ex.getMessage());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the loading cache of user identities.
   * <p>
   * The user identities are loaded by the cache when an entry is found to be missing.
   * See {@link #loadUserIdentity(IdentityCacheKey)}.
   * 
   * @param identityExpiry  the duration before the identity expires
   * @return the cache
   */
  private LoadingCache<IdentityCacheKey, Identity> createUserIdentityCache(Duration identityExpiry) {
    // called from constructor - must not use instance variables in this method
    return CacheBuilder.newBuilder()
      .expireAfterWrite(identityExpiry.getSeconds(), TimeUnit.SECONDS)
      .build(new CacheLoader<IdentityCacheKey, Identity>() {
        @Override
        public Identity load(IdentityCacheKey userCredential) throws Exception {
          return loadUserIdentity(userCredential);
        }
      });
  }

  // called from the cache to load user identities
  private Identity loadUserIdentity(IdentityCacheKey userInfo) throws IOException, InterruptedException {
    Request authRequest = _apiAuthSvc.createAuthorizationRequest();
    authRequest.set("emrsId", userInfo.getUserId());
    authRequest.set("ipAddress", userInfo.getIpAddress());
    Identity userIdentity = _session.createIdentity();

    s_logger.debug("Sending {}", authRequest);
    EventQueue eventQueue = new EventQueue();
    _session.sendAuthorizationRequest(authRequest, userIdentity, eventQueue, new CorrelationID(userInfo));
    Event event = eventQueue.nextEvent(WAIT_TIME_MS);
    // handle known responses to loading an identity ignoring other events
    switch (event.eventType().intValue()) {
      case EventType.Constants.RESPONSE:
      case EventType.Constants.REQUEST_STATUS: {
        for (Message message : event) {
          if (AUTHORIZATION_SUCCESS.equals(message.messageType())) {
            return userIdentity;
          } 
          if (AUTHORIZATION_FAILURE.equals(message.messageType())) {
            String failureMsg = "Unknown";
            Element reasonElem = message.getElement("reason");
            if (reasonElem != null) {
              failureMsg = reasonElem.getElementAsString("message");
              String failureCode = StringUtils.stripToNull(reasonElem.getElementAsString("code"));
              if (failureCode != null) {
                failureMsg = failureMsg + " (code " + failureCode + ")";
              }
            }
            throw new UnauthenticatedException(
                String.format("User: %s IpAddress: %s Reason: %s",
                    userInfo.getUserId(), userInfo.getIpAddress(), failureMsg));
          }
        }
      }
    }
    throw new UnauthenticatedException(
        String.format("User: %s IpAddress: %s Reason: Unexpected response to authorization request",
            userInfo.getUserId(), userInfo.getIpAddress()));
  }

  //-------------------------------------------------------------------------
  /**
   * Handler for events sent about users.
   */
  private class SessionEventHandler implements EventHandler {
    public void processEvent(Event event, Session session) {
      switch (event.eventType().intValue()) {
        case EventType.Constants.AUTHORIZATION_STATUS:
          processAuthorizationEvent(event);
          break;
      }
    }
  }

  /**
   * Processes events indicating changes in authorization.
   * 
   * @param event  the event, not null
   */
  private void processAuthorizationEvent(Event event) {
    for (Message msg : event) {
      CorrelationID correlationId = msg.correlationID();
      IdentityCacheKey userCredential = (IdentityCacheKey) correlationId.object();
      if (AUTHORIZATION_REVOKED.equals(msg.messageType())) {
        // the current Identity object has been revoked
        // documentation says that this is the only reason to destroy the current cached Identity object
        Element errorinfo = msg.getElement("reason");
        int code = errorinfo.getElementAsInt32("code");
        String reason = errorinfo.getElementAsString("message");
        s_logger.debug("Authorization revoked for emrsid: {} with code: {} and reason {}",
            userCredential.getUserId(), code, reason);
        _userIdentityCache.invalidate(userCredential);
        
      } else if (ENTITITLEMENT_CHANGED.equals(msg.messageType())) {
        // the current Identity object will have been updated with new entitlements
        // no need to replace the identity as any caching is internal to Identity
        // if there are client side caches, they should be cleared at this point
        s_logger.debug("Entitlements updated for emrsid: {}", userCredential.getUserId());
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized void start() {
    if (!isRunning()) {
      _sessionProvider.start();
      _session = _sessionProvider.getSession();
      _apiAuthSvc = _sessionProvider.getService(BloombergConstants.AUTH_SVC_NAME);
      _apiRefDataSvc = _sessionProvider.getService(BloombergConstants.REF_DATA_SVC_NAME);
      _isRunning.getAndSet(true);
    }
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

}
