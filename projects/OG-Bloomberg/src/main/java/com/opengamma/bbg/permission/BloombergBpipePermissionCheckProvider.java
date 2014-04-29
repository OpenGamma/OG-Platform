/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergPermissions;
import com.opengamma.bbg.SessionProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.provider.permission.impl.AbstractPermissionCheckProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Bloomberg bpipe permission/EID check provider.
 */
public final class BloombergBpipePermissionCheckProvider extends AbstractPermissionCheckProvider implements PermissionCheckProvider, Lifecycle {
  
  private static final String NO_AUTH = "NO_AUTH";
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergBpipePermissionCheckProvider.class);
  private static final Name AUTHORIZATION_SUCCESS = Name.getName("AuthorizationSuccess");
  private static final Name AUTHORIZATION_FAILURE = Name.getName("AuthorizationFailure");
  private static final Name AUTHORIZATION_REVOKED = Name.getName("AuthorizationRevoked");
  private static final Name ENTITITLEMENT_CHANGED = Name.getName("EntitlementChanged");
  private static final Name SUBSCRIPTION_FAILURE = Name.getName("SubscriptionFailure");
  private static final Name CATEGORY = Name.getName("category");
  private static final Name DESCRIPTION = Name.getName("description");
  private static final Name REASON = Name.getName("reason");
  private static final Name EXCEPTIONS = Name.getName("exceptions");
  private static final Name FIELD_ID = Name.getName("fieldId");
  private static final Name LAST_PRICE = Name.getName("LAST_PRICE");
  private static final Name EID = Name.getName("EID");
  private static final int WAIT_TIME_MS = 10 * 1000; // 10 seconds
  private static final Duration DEFAULT_IDENTITY_EXPIRY = Duration.ofHours(24);

  private final LoadingCache<IdentityCacheKey, Identity> _userIdentityCache;
  private final LoadingCache<String, Set<String>> _liveDataPermissionCache;
  private final BloombergConnector _bloombergConnector;
  private final AtomicBoolean _isRunning = new AtomicBoolean(false);
  private volatile Session _session;
  private volatile Service _apiAuthSvc;
  private volatile Service _apiRefDataSvc;
  private volatile Service _apiMktDataSvc;
  private volatile Identity _applicationIdentity;
  private final Map<CorrelationID, SettableFuture<Set<String>>> _subscriptionResponse = new ConcurrentHashMap<>();
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
    ArgumentChecker.isTrue(bloombergConnector.requiresAuthorization(), "authentication options must be set");
    
    _userIdentityCache = createUserIdentityCache(identityExpiry);
    _liveDataPermissionCache = createLiveDataPermissionCache(identityExpiry);
    _bloombergConnector = bloombergConnector;

    List<String> serviceNames = Lists.newArrayList(BloombergConstants.AUTH_SVC_NAME, BloombergConstants.REF_DATA_SVC_NAME, BloombergConstants.MKT_DATA_SVC_NAME);
    SessionEventHandler eventHandler = new SessionEventHandler();
    _sessionProvider = new SessionProvider(_bloombergConnector, serviceNames, eventHandler);
  }

  private LoadingCache<IdentityCacheKey, Identity> createUserIdentityCache(Duration identityExpiry) {
    LoadingCache<IdentityCacheKey, Identity> identityCache = CacheBuilder.newBuilder()
        .expireAfterWrite(identityExpiry.getSeconds(), TimeUnit.SECONDS)
        .build(new CacheLoader<IdentityCacheKey, Identity>() {
          @Override
          public Identity load(IdentityCacheKey userCredential) throws Exception {
            return loadUserIdentity(userCredential);
          }
        });
    return identityCache;
  }

  private LoadingCache<String, Set<String>> createLiveDataPermissionCache(Duration identityExpiry) {
    final LoadingCache<String, Set<String>> liveDataPermissionCache = CacheBuilder.newBuilder()
        .expireAfterWrite(identityExpiry.getSeconds(), TimeUnit.SECONDS)
        .build(new CacheLoader<String, Set<String>>() {
          @Override
          public Set<String> load(String liveDataPermissionRequest) throws Exception {
            Map<String, Set<String>> all = loadAll(Collections.singleton(liveDataPermissionRequest));
            return all.get(liveDataPermissionRequest);
          }
          @Override
          public Map<String, Set<String>> loadAll(Iterable<? extends String> liveDataPermissionRequests) throws Exception {
            return loadLiveDataPermissions(liveDataPermissionRequests);
          }
        });
    return liveDataPermissionCache;
  }

  private Map<String, Set<String>> loadLiveDataPermissions(Iterable<? extends String> liveDataPermissionRequests) throws IOException {
    Map<String, Set<String>> result = initializeLiveDataEidResult(liveDataPermissionRequests);
    SubscriptionList subscriptions = new SubscriptionList();
    List<String> fields = new ArrayList<>(BloombergDataUtils.STANDARD_FIELDS_LIST);
    fields.add(EID.toString());

    List<CorrelationID> liveDataRequests = new ArrayList<>();
    for (String liveDataPermission : liveDataPermissionRequests) {
      String securityId = getBloombergIdentifier(liveDataPermission);
      CorrelationID subscriptionCid = new CorrelationID(liveDataPermission);
      subscriptions.add(new Subscription(securityId, fields, subscriptionCid));
      SettableFuture<Set<String>> response = SettableFuture.create();
      _subscriptionResponse.put(subscriptionCid, response);
      liveDataRequests.add(subscriptionCid);
    }
    _session.subscribe(subscriptions, _applicationIdentity);

    for (CorrelationID correlationID : liveDataRequests) {
      try {
        Set<String> permissions = _subscriptionResponse.get(correlationID).get(WAIT_TIME_MS, TimeUnit.MILLISECONDS);
        result.put((String) correlationID.object(), permissions);
      } catch (InterruptedException | TimeoutException | ExecutionException ex) {
        s_logger.warn("Error loading EIDS for {}", correlationID.toString());
      } finally {
        _subscriptionResponse.remove(correlationID);

      }
    }
    _session.unsubscribe(subscriptions);
    return result;
  }

  private Map<String, Set<String>> initializeLiveDataEidResult(Iterable<? extends String> liveDataPermissionRequests) {
    Map<String, Set<String>> result = new HashMap<>();
    for (String liveDataPermissionRequest : liveDataPermissionRequests) {
      result.put(liveDataPermissionRequest, Sets.newHashSet(NO_AUTH));
    }
    return result;
  }

  private String getBloombergIdentifier(String liveDataPermissionRequest) {
    int index = liveDataPermissionRequest.indexOf("LIVEDATA:");
    if (index != -1) {
      return liveDataPermissionRequest.substring(index + 9);
    }
    return liveDataPermissionRequest;
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
    if (Event.EventType.RESPONSE.equals(event.eventType()) || Event.EventType.REQUEST_STATUS.equals(event.eventType())) {
      for (Message message : event) {
        if (AUTHORIZATION_SUCCESS.equals(message.messageType())) {
          return userIdentity;
        } 
        if (AUTHORIZATION_FAILURE.equals(message.messageType())) {
          String failureMsg = "";
          Element reasonElem = message.getElement("reason");
          if (reasonElem != null) {
            failureMsg = reasonElem.getElementAsString("message");
          }
          throw new UnauthenticatedException(String.format("User:%s IpAdress:%s authorization failed, reason:%s",
              userCredential.getUserId(), userCredential.getIpAddress(), failureMsg));
        }
      }
    }
    throw new UnauthenticatedException(String.format("User: %s IpAdress: %s authorization failed", userCredential.getUserId(), userCredential.getIpAddress()));
  }

  @Override
  public PermissionCheckProviderResult isPermitted(PermissionCheckProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getNetworkAddress(), "request.networkAddress");
    ArgumentChecker.notNull(request.getRequestedPermissions(), "request.requestedPermissions");
    ArgumentChecker.notNull(request.getUserIdBundle(), "request.userIdBundle");

    final String emrsId = StringUtils.trimToNull(request.getUserIdBundle().getValue(ExternalSchemes.BLOOMBERG_EMRSID));
    ArgumentChecker.notNull(emrsId, "user emrsid scheme");

    final Map<String, Boolean> permissionResult = initializeResult(request.getRequestedPermissions());
    if (request.getRequestedPermissions().size() > 0) {
      try {
        Identity userIdentity = _userIdentityCache.get(IdentityCacheKey.of(request.getNetworkAddress(), emrsId));
        Set<String> liveDataPermissions = new HashSet<>();
        for (String permission : request.getRequestedPermissions()) {
          if (BloombergPermissions.isEid(permission)) {
            doReferenceDataEidPermissionCheck(request, permissionResult, userIdentity, permission);
          } else if (BloombergPermissions.isLiveDataId(permission)) {
            liveDataPermissions.add(permission);
          }
        }
        doLiveDataPermissionCheck(request, permissionResult, userIdentity, liveDataPermissions);
      } catch (ExecutionException | UncheckedExecutionException ex) {
        s_logger.warn(String.format("Bloomberg authorization failure for user: %s ipAddress: %s", request.getUserIdBundle(), request.getNetworkAddress()), ex.getCause());
        throw new UnauthenticatedException(ex.getCause());
      }
    }
    return new PermissionCheckProviderResult(permissionResult);
  }

  private void doLiveDataPermissionCheck(PermissionCheckProviderRequest request, Map<String, Boolean> permissionResult, Identity userIdentity, Set<String> liveDataPermissions) {
    try {
      ImmutableMap<String, Set<String>> eidMap = _liveDataPermissionCache.getAll(liveDataPermissions);
      for (String liveDataPermission : liveDataPermissions) {
        Set<String> permissions = eidMap.get(liveDataPermission);

        if (permissions.isEmpty()) {
          permissionResult.put(liveDataPermission, true);
          continue;
        }
        if (permissions.contains(NO_AUTH)) {
          continue;
        }

        List<Integer> failedEntitlements = new ArrayList<Integer>();
        int[] neededEids = new int[permissions.size()];
        int count = 0;
        for (String eid : permissions) {
          neededEids[count++] = Integer.parseInt(eid);
        }
        if (userIdentity.hasEntitlements(neededEids, _apiMktDataSvc, failedEntitlements)) {
          permissionResult.put(liveDataPermission, true);
        } else {
          if (!failedEntitlements.isEmpty()) {
            s_logger.warn("user: {} is missing entitlements: {}", request.getUserIdBundle(), failedEntitlements);
          }
        }
      }
    } catch (ExecutionException ex) {
      s_logger.warn(String.format("Error loading EIDs for bloomberg live data permission request: %s for user: %s", liveDataPermissions, request.getUserIdBundle()), ex);
    }
  }

  private void doReferenceDataEidPermissionCheck(PermissionCheckProviderRequest request, final Map<String, Boolean> permissionResult, Identity userIdentity, String permission) {
    int eid = BloombergPermissions.extractEid(permission);
    if (userIdentity.hasEntitlements(new int[] {eid }, _apiRefDataSvc)) {
      permissionResult.put(permission, true);
    } else {
      s_logger.warn("user: {} is missing entitlements: {}", request.getUserIdBundle(), eid);
    }
  }

  private Map<String, Boolean> initializeResult(Set<String> requestedPermissions) {
    final Map<String, Boolean> result = new HashMap<>();
    for (String permission : requestedPermissions) {
      result.put(permission, false);
    }
    return result;
  }

  private void processAuthorizationEvent(Event event) {
    for (Message msg : event) {
      CorrelationID correlationId = msg.correlationID();
      IdentityCacheKey userCredential = (IdentityCacheKey) correlationId.object();
      if (AUTHORIZATION_REVOKED.equals(msg.messageType())) {
        Element errorinfo = msg.getElement("reason");
        int code = errorinfo.getElementAsInt32("code");
        String reason = errorinfo.getElementAsString("message");
        s_logger.warn("Authorization revoked for emrsid: {} with code: {} and reason\n\t{}", userCredential.getUserId(), code, reason);
        //Remove identity from cache
        _userIdentityCache.invalidate(userCredential);
      } else if (ENTITITLEMENT_CHANGED.equals(msg.messageType())) {
        s_logger.warn("Entitlements updated for emrsid: {}", userCredential.getUserId());
      }
    }
  }

  @Override
  public synchronized void start() {
    if (!isRunning()) {
      _sessionProvider.start();
      _session = _sessionProvider.getSession();

      _apiAuthSvc = _sessionProvider.getService(BloombergConstants.AUTH_SVC_NAME);
      _apiRefDataSvc = _sessionProvider.getService(BloombergConstants.REF_DATA_SVC_NAME);
      _apiMktDataSvc = _sessionProvider.getService(BloombergConstants.MKT_DATA_SVC_NAME);
      BloombergBpipeApplicationUserIdentityProvider identityProvider = new BloombergBpipeApplicationUserIdentityProvider(_sessionProvider);
      _applicationIdentity = identityProvider.getIdentity();
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

  private class SessionEventHandler implements EventHandler {

    public void processEvent(Event event, Session session) {
      try {
        switch (event.eventType().intValue()) {
        //          case EventType.Constants.SESSION_STATUS:
        //          case EventType.Constants.SERVICE_STATUS:
          case EventType.Constants.SUBSCRIPTION_STATUS:
            processSubscriptionStatus(event);
            break;
        //          case EventType.Constants.REQUEST_STATUS:
        //          case EventType.Constants.RESPONSE:
          case EventType.Constants.AUTHORIZATION_STATUS:
            processAuthorizationEvent(event);
            break;
          case EventType.Constants.SUBSCRIPTION_DATA:
            processSubscriptionDataEvent(event);
            break;
        }
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Error processing bloomberg events", ex);
      }
    }
  }

  private void processSubscriptionDataEvent(Event event) {
    s_logger.debug("Processing SUBSCRIPTION_DATA");
    for (Message message : event) {
      CorrelationID correlationID = message.correlationID();
      s_logger.debug("Received subscription data for {} - {}\n{}", (String) correlationID.object(), message.messageType(), message.toString());
      if (!message.hasElement(LAST_PRICE, true)) {
        continue;
      }
      Element field = message.getElement(LAST_PRICE);
      if (field.isNull()) {
        continue;
      }
      Set<String> permissions = Sets.newHashSet();
      if (message.hasElement(EID)) {
        Element eidElem = message.getElement(EID);
        permissions.add(eidElem.getValueAsString());
      }
      SettableFuture<Set<String>> responseFuture = _subscriptionResponse.get(correlationID);
      responseFuture.set(permissions);
    }
  }

  private void processSubscriptionStatus(Event event) {
    for (Message message : event) {
      CorrelationID correlationID = message.correlationID();
      if (SUBSCRIPTION_FAILURE.equals(message.messageType())) {
        if (message.hasElement(REASON)) {
          Element reason = message.getElement(REASON);
          s_logger.warn("Subscription failure for {} {}: {}", correlationID.toString(), reason.getElement(CATEGORY).getValueAsString(), reason.getElement(DESCRIPTION).getValueAsString());
        }
        SettableFuture<Set<String>> responseFuture = _subscriptionResponse.get(correlationID);
        responseFuture.set(Sets.newHashSet(NO_AUTH));
      }
      if (message.hasElement(EXCEPTIONS)) {
        // This can occur on SubscriptionStarted if at least one field is good while the rest are bad.
        Element exceptions = message.getElement(EXCEPTIONS);
        for (int i = 0; i < exceptions.numValues(); ++i) {
          Element exInfo = exceptions.getValueAsElement(i);
          Element fieldId = exInfo.getElement(FIELD_ID);
          Element reason = exInfo.getElement(REASON);
          s_logger.info("Subscription exceptions for {} {}: {}", correlationID.toString(), fieldId.getValueAsString(), reason.getElement(CATEGORY).getValueAsString());
        }
      }
    }
  }
}
