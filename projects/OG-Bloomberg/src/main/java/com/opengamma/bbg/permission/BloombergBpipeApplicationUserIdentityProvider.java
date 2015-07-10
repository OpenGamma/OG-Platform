/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.permission;

import java.io.IOException;

import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventQueue;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.SessionProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BloombergBpipeApplicationUserIdentityProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(BloombergBpipeApplicationUserIdentityProvider.class);

  private static final Name AUTHORIZATION_SUCCESS = Name.getName("AuthorizationSuccess");

  private static final Name TOKEN_SUCCESS = Name.getName("TokenGenerationSuccess");
  private static final Name TOKEN_FAILURE = Name.getName("TokenGenerationFailure");
  private static final Name TOKEN_ELEMENT = Name.getName("token");
  private static final int WAIT_TIME_MS = 10 * 1000; // 10 seconds

  private final SessionProvider _sessionProvider;

  public BloombergBpipeApplicationUserIdentityProvider(SessionProvider sessionProvider) {
    ArgumentChecker.notNull(sessionProvider, "sessionProvider");
    _sessionProvider = sessionProvider;
  }

  /**
   * Return an authorized identity for a bpipe application
   * 
   * @return the bloomberg identity for an application
   * @throws UnauthenticatedException if an authorized identity cannot be created from bloomberg
   */
  public Identity getIdentity() {

    Session session = _sessionProvider.getSession();
    BloombergConnector bloombergConnector = _sessionProvider.getConnector();

    s_logger.debug("Attempting to authorize application using authentication option: {}", bloombergConnector.getSessionOptions().authenticationOptions());
    try {
      EventQueue tokenEventQueue = new EventQueue();
      session.generateToken(new CorrelationID(), tokenEventQueue);
      String token = null;
      //Generate token responses will come on this dedicated queue. There would be no other messages on that queue.
      Event event = tokenEventQueue.nextEvent(WAIT_TIME_MS);
      if (Event.EventType.TOKEN_STATUS.equals(event.eventType()) || Event.EventType.REQUEST_STATUS.equals(event.eventType())) {
        for (Message msg : event) {
          if (TOKEN_SUCCESS.equals(msg.messageType())) {
            token = msg.getElementAsString(TOKEN_ELEMENT);
          }
          if (TOKEN_FAILURE.equals(msg.messageType())) {
            String description = "";
            Element reasonElem = msg.getElement("reason");
            if (reasonElem != null) {
              description = reasonElem.getElementAsString("description");
            }
            SessionOptions sessionOptions = bloombergConnector.getSessionOptions();
            String message = String.format("Failure to get application token from Host:%s port:%s authentication option:%s reason:%s", 
                sessionOptions.getServerHost(), sessionOptions.getServerPort(), sessionOptions.authenticationOptions(), description);
            throw new UnauthenticatedException(message);
          }
        }
      }
      s_logger.debug("Token: {} generated for application: {}", token, bloombergConnector.getSessionOptions().authenticationOptions());

      Service apiAuthSvc = _sessionProvider.getService(BloombergConstants.AUTH_SVC_NAME);
      Request authRequest = apiAuthSvc.createAuthorizationRequest();
      authRequest.set(TOKEN_ELEMENT, token);

      final Identity appIdentity = session.createIdentity();
      EventQueue authEventQueue = new EventQueue();
      session.sendAuthorizationRequest(authRequest, appIdentity, authEventQueue, new CorrelationID());

      event = authEventQueue.nextEvent(WAIT_TIME_MS);
      if (event.eventType().equals(Event.EventType.RESPONSE) || event.eventType().equals(Event.EventType.REQUEST_STATUS)) {
        for (Message msg : event) {
          if (msg.messageType().equals(AUTHORIZATION_SUCCESS)) {
            s_logger.debug("Application authorization SUCCESS");
            return appIdentity;
          }
        }
      }
    } catch (IOException | InterruptedException ex) {
      throw new UnauthenticatedException(String.format("Bloomberg authorization failed using authentication option: %s", bloombergConnector.getSessionOptions().authenticationOptions()));
    }
    throw new UnauthenticatedException(String.format("Bloomberg authorization failed using authentication option: %s", bloombergConnector.getSessionOptions().authenticationOptions()));
  }

}
