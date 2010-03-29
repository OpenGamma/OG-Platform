/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.client.utils.URIUtils;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Composes a {@link URL} with an optional Fudge taxonomy ID for the transport.
 * 
 * @author Andrew Griffin
 */
public class RestTarget {
  
  private final URI _uri;
  private final int _taxonomyId;
  
  public RestTarget (final URI uri) {
    this (uri, 0);
  }
  
  private static URI createURI (final String uri) {
    try {
      return new URI (uri);
    } catch (URISyntaxException e) {
      throw new OpenGammaRuntimeException ("couldn't parse URI", e);
    }
  }
  
  public RestTarget (final String uri) {
    this (createURI (uri));
  }
  
  public RestTarget (final URI uri, final int taxonomyId) {
    if (uri == null) throw new NullPointerException ("uri cannot be null");
    if ((taxonomyId < Short.MIN_VALUE) || (taxonomyId > Short.MAX_VALUE)) throw new IllegalArgumentException ("taxonomyId must be 16-bit signed integer");
    _uri = uri;
    _taxonomyId = taxonomyId;
  }
  
  public RestTarget (final String uri, final int taxonomyId) {
    this (createURI (uri), taxonomyId);
  }
  
  public URI getURI () {
    return _uri;
  }
  
  public int getTaxonomyId () {
    return _taxonomyId;
  }
  
  protected String encodedSpec (final String spec) {
    try {
      return URLEncoder.encode (spec, "UTF-8").replace ("+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new OpenGammaRuntimeException ("internal error", e);
    }
  }
  
  protected RestTarget resolveInternal (final String encodedSpec) {
    return new RestTarget (URIUtils.resolve (getURI (), encodedSpec), getTaxonomyId ());
  }
  
  public RestTarget resolve (final String spec) {
    return resolveInternal (encodedSpec (spec));
  }
  
  public RestTarget resolveBase (final String spec) {
    return resolveInternal (encodedSpec (spec) + '/');
  }
  
  public RestTarget withTaxonomyId (final int taxonomyId) {
    return new RestTarget (getURI (), taxonomyId);
  }
  
  @Override
  public String toString () {
    return "{\"" + getURI () + "\", {Fudge taxonomy " + getTaxonomyId () + "}}";
  }
  
}