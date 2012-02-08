/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.UriBuilder;

import org.apache.http.client.utils.URIUtils;
import org.fudgemsg.FudgeContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Composes a {@link URL} with an optional Fudge taxonomy ID for the transport.
 */
public class RestTarget {

  private final URI _uri;
  private final int _taxonomyId;

  public RestTarget(final URI uri) {
    this(uri, 0);
  }

  private static URI createURI(final String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new OpenGammaRuntimeException("couldn't parse URI", e);
    }
  }

  public RestTarget(final String uri) {
    this(createURI(uri));
  }

  public RestTarget(final String uri, final int taxonomyId) {
    this(createURI(uri), taxonomyId);
  }

  public RestTarget(final URI uri, final int taxonomyId) {
    ArgumentChecker.notNull(uri, "uri");
    if (taxonomyId < Short.MIN_VALUE || taxonomyId > Short.MAX_VALUE) {
      throw new IllegalArgumentException("taxonomyId must be 16-bit signed integer");
    }
    _uri = uri;
    _taxonomyId = taxonomyId;
  }

  public RestTarget(final ExecutorService executorService, final FudgeContext fudgeContext, final EndPointDescriptionProvider endPointProvider) {
    this(UriEndPointDescriptionProvider.getAccessibleURI(executorService, fudgeContext, endPointProvider));
  }

  public RestTarget(final ExecutorService executorService, final FudgeContext fudgeContext, final EndPointDescriptionProvider endPointProvider, final int taxonomyId) {
    this(UriEndPointDescriptionProvider.getAccessibleURI(executorService, fudgeContext, endPointProvider), taxonomyId);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the target URI.
   * @return the target URI, not null
   */
  public URI getURI() {
    return _uri;
  }

  /**
   * Gets the taxonomy id.
   * @return the taxonomy id
   */
  public int getTaxonomyId() {
    return _taxonomyId;
  }

  //-------------------------------------------------------------------------
  protected String encodedSpec(final String spec) {
    try {
      return URLEncoder.encode(spec, "UTF-8").replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new OpenGammaRuntimeException("internal error", e);
    }
  }

  protected RestTarget resolveInternal(final String encodedSpec) {
    return new RestTarget(URIUtils.resolve(getURI(), encodedSpec), getTaxonomyId());
  }

  public RestTarget resolve(final String spec) {
    return resolveInternal(encodedSpec(spec));
  }

  public RestTarget resolveBase(final String spec) {
    return resolveInternal(encodedSpec(spec) + '/');
  }

  /**
   * Returns a copy of this target with a query appended to the URI.
   * @param key  the query key, not null
   * @param values  the values associated with the key, not null
   * @return a target based on this with the query updated, not null
   */
  public RestTarget resolveQuery(String key, List<String> values) {
    URI uri = UriBuilder.fromUri(getURI()).queryParam(key, values.toArray()).build();
    return new RestTarget(uri, getTaxonomyId());
  }

  /**
   * Returns a copy of this target with a different taxonomy.
   * @param taxonomyId  the new taxonomy
   * @return a target based on this with the taxonomy changed, not null
   */
  public RestTarget withTaxonomyId(final int taxonomyId) {
    return new RestTarget(getURI(), taxonomyId);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "{\"" + getURI() + "\", {Fudge taxonomy " + getTaxonomyId() + "}}";
  }

}
