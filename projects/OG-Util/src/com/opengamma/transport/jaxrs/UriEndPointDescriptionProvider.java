/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.Client;

/**
 * An implementation of {@link EndPointDescriptionProvider} that describes URLs
 */
public class UriEndPointDescriptionProvider implements EndPointDescriptionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(UriEndPointDescriptionProvider.class);

  /**
   * Type of connection. Always {@link #TYPE_VALUE}.
   */
  public static final String TYPE_KEY = "type";
  /**
   * Value of the type of connection.
   */
  public static final String TYPE_VALUE = "Uri";
  /**
   * URL.
   */
  public static final String URI_KEY = "uri";

  private final List<String> _uris;

  public UriEndPointDescriptionProvider(final String uri) {
    _uris = Collections.singletonList(uri);
  }

  public UriEndPointDescriptionProvider(final List<String> uris) {
    _uris = new ArrayList<String>(uris);
  }

  @Override
  public FudgeFieldContainer getEndPointDescription(final FudgeContext fudgeContext) {
    final MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    msg.add(TYPE_KEY, TYPE_VALUE);
    for (String url : _uris) {
      msg.add(URI_KEY, url);
    }
    return msg;
  }

  /**
   * Default message production allows simple use in a configuration resource.
   * 
   * @param fudgeContext the Fudge context
   * @return the end point description message, as returned by {@link #getEndPointDescription}
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeContext fudgeContext) {
    return getEndPointDescription(fudgeContext);
  }

  public static URI getAccessibleURI(final FudgeFieldContainer endPoint) {
    ArgumentChecker.notNull(endPoint, "endPoint");
    if (!TYPE_VALUE.equals(endPoint.getString(TYPE_KEY))) {
      throw new IllegalArgumentException("End point is not a REST target - " + endPoint);
    }
    final Client client = Client.create();
    client.setReadTimeout(5000);
    final ExecutorService executor = Executors.newCachedThreadPool();
    final BlockingQueue<URI> result = new LinkedBlockingQueue<URI>();
    for (final FudgeField uriField : endPoint.getAllByName(URI_KEY)) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            final URI uri = new URI(endPoint.getFieldValue(String.class, uriField));
            final int status = client.resource(uri).head().getStatus();
            s_logger.debug("{} returned {}", uri, status);
            switch (status) {
              case 200:
              case 405:
                result.add(uri);
                return;
            }
          } catch (Exception ex) {
            s_logger.warn("URI {} not accessible", uriField);
            s_logger.debug("Exception caught", ex);
          }
        }
      });
    }
    final URI uri;
    try {
      uri = result.poll(5000, TimeUnit.MILLISECONDS);
      executor.shutdownNow();
    } catch (InterruptedException ex) {
      throw new OpenGammaRuntimeException("Interrupted", ex);
    }
    if (uri == null) {
      s_logger.error("No accessible URIs found in {}", endPoint);
    } else {
      s_logger.info("Using {}", uri);
    }
    return uri;
  }

  public static URI getAccessibleURI(final FudgeContext fudgeContext, final EndPointDescriptionProvider endPointProvider) {
    ArgumentChecker.notNull(endPointProvider, "endPointProvider");
    return getAccessibleURI(endPointProvider.getEndPointDescription(fudgeContext));
  }

}
