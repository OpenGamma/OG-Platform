/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.ws.rs.core.UriBuilder;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.financial.livedata.rest.RemoteLiveDataInjector;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link View}.
 */
public class RemoteView implements View {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  private final JmsTemplate _jmsTemplate;
  
  public RemoteView(URI baseUri, JmsTemplate jmsTemplate) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
    _jmsTemplate = jmsTemplate;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String getName() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_NAME);
    return _client.access(uri).accept(FudgeRest.MEDIA_TYPE).get(String.class);
  }
  
  @Override
  public ViewDefinition getDefinition() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_DEFINITION);
    return _client.access(uri).accept(FudgeRest.MEDIA_TYPE).get(ViewDefinition.class);
  }
  
  @Override
  public void init() {
    // NOTE jonathan 2011-01-31 -- using a RESTful call to kick off an asynchronous response via JMS since initialising
    // a view can be very slow. Ideally the status code would be used to indicate whether the view has already been
    // initialised, but this is not supported by the View interface and init() is being removed in any case.
    
    _jmsTemplate.execute(new SessionCallback<Object>() {

      @Override
      public Object doInJms(Session session) throws JMSException {
        TemporaryTopic replyTo = null;
        try {
          replyTo = session.createTemporaryTopic();
          MessageConsumer consumer = session.createConsumer(replyTo);
          
          // Make the RESTful call, indicating the reply topic
          URI uri = getUri(_baseUri, DataViewResource.PATH_INIT);
          _client.access(uri).post(replyTo.getTopicName());
          
          Message result = consumer.receive(_jmsTemplate.getReceiveTimeout());
          consumer.close();
          
          if (result != null && result.getBooleanProperty("init")) {
            return null;
          } else {
            throw new OpenGammaRuntimeException("Failed to initialize remote view");
          }
        } finally {
          if (replyTo != null) {
            replyTo.delete();
          }
        }
      }
      
    }, true);
  }
  
  @Override
  public Portfolio getPortfolio() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_PORTFOLIO);
    return _client.access(uri).accept(FudgeRest.MEDIA_TYPE).get(Portfolio.class);
  }
  
  @Override
  public ViewClient createClient(UserPrincipal credentials) {
    ClientResponse response = _client.access(DataViewResource.uriClients(_baseUri)).post(ClientResponse.class, credentials);
    URI clientLocation = response.getLocation();
    return new RemoteViewClient(this, clientLocation, OpenGammaFudgeContext.getInstance(), _jmsTemplate);
  }
  
  @Override
  public ViewClient getClient(UniqueIdentifier id) {
    URI clientUri = DataViewResource.uriClient(_baseUri, id);
    return new RemoteViewClient(this, clientUri, OpenGammaFudgeContext.getInstance(), _jmsTemplate);
  }
  
  @Override
  public void assertAccessToLiveDataRequirements(UserPrincipal user) {
    URI uri = getUri(_baseUri, DataViewResource.PATH_ACCESS_TO_LIVE_DATA);
    _client.access(uri)
        .queryParam(DataViewResource.PARAM_USER_IP, user.getIpAddress())
        .queryParam(DataViewResource.PARAM_USER_NAME, user.getUserName())
        .post();
  }
  
  @Override
  public ViewComputationResultModel getLatestResult() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_LATEST_RESULT);
    return _client.access(uri)
        .accept(FudgeRest.MEDIA_TYPE)
        .get(ViewComputationResultModel.class);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Set<ValueRequirement> getRequiredLiveData() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_REQUIRED_LIVE_DATA);
    return _client.access(uri).accept(FudgeRest.MEDIA_TYPE).get(Set.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getAllSecurityTypes() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_ALL_SECURITY_TYPES);
    return _client.access(uri).accept(FudgeRest.MEDIA_TYPE).get(Set.class);
  }
  
  @Override
  public boolean isLiveComputationRunning() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_LIVE_COMPUTATION_RUNNING);
    return _client.access(uri).accept(FudgeRest.MEDIA_TYPE).get(Boolean.class);
  }

  @Override
  public LiveDataInjector getLiveDataOverrideInjector() {
    URI uri = getUri(_baseUri, DataViewResource.PATH_LIVE_DATA_OVERRIDE_INJECTOR);
    return new RemoteLiveDataInjector(uri);
  }
  
  //-------------------------------------------------------------------------
  private static URI getUri(URI baseUri, String path) {
    return UriBuilder.fromUri(baseUri).path(path).build();
  }

}
