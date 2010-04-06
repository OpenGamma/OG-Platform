/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.OpenGammaRuntimeException;

/**
 * HttpClient wrapper with some additional helper functions for Fudge based JAX-RS calls.
 * 
 * @author Andrew Griffin
 */
public class RestClient {
  
  private static final String FUDGE_MIME_TYPE = "application/vnd.fudgemsg";
  private static final Header s_accept = new BasicHeader ("Accept", FUDGE_MIME_TYPE);
  private static final Header s_contentType = new BasicHeader ("Content-Type", FUDGE_MIME_TYPE);
  
  private final FudgeContext _fudgeContext;
  private final HttpClient _httpClient;
  
  protected RestClient (final FudgeContext fudgeContext, final HttpClient underlyingClient) {
    _fudgeContext = fudgeContext;
    _httpClient = underlyingClient;
  }
  
  /**
   * Create a default instance with whatever security credentials/parameters etc... are relevant
   * to the user's context. This is here as a placeholder - passing NULL will give a default and
   * anonymous connection.
   */
  public static RestClient getInstance (FudgeContext fudgeContext, Map<String,String> securityCredentials) {
    return new RestClient (fudgeContext, new DefaultHttpClient ());
  }
  
  protected HttpClient getHttpClient () {
    return _httpClient;
  }
  
  public FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
  public FudgeSerializationContext getFudgeSerializationContext () {
    return new FudgeSerializationContext (getFudgeContext ());
  }
  
  public FudgeDeserializationContext getFudgeDeserializationContext () {
    return new FudgeDeserializationContext (getFudgeContext ());
  }
  
  protected <T extends AbstractHttpMessage> T setRequestHeaders (final T request) {
    request.addHeader (s_accept);
    return request;
  }
  
  protected <T extends HttpEntityEnclosingRequestBase> T addFudgePayload (final T request, final FudgeMsgEnvelope msg, final int taxonomyId) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    final FudgeMsgWriter fmw = getFudgeContext ().createMessageWriter (baos);
    fmw.writeMessageEnvelope (msg, taxonomyId);
    fmw.flush ();
    request.setEntity (new ByteArrayEntity (baos.toByteArray ()));
    request.addHeader (s_contentType);
    return request;
  }
  
  protected FudgeMsgEnvelope decodeResponse (final HttpResponse resp) throws IOException {
    final HttpEntity entity = resp.getEntity ();
    if (!FUDGE_MIME_TYPE.equals (entity.getContentType ().getValue ())) throw new OpenGammaRuntimeException ("Unexpected content type " + entity.getContentType ().getValue ());
    final InputStream content = entity.getContent ();
    try {
      return getFudgeContext ().deserialize (content);
    } finally {
      content.close ();
    }
  }
  
  protected void debugPrintResponse (final HttpResponse resp) throws IOException {
    final HttpEntity entity = resp.getEntity ();
    final InputStream is = entity.getContent ();
    try {
      int c;
      while ((c = is.read ()) != -1) {
        System.out.print (c);
      }
    } finally {
      is.close ();
    }
  }
  
  /**
   * Returns NULL for a 404, a message envelope for a 200 series (possibly empty), and throws an exception for anything else.  
   */
  public FudgeFieldContainer getMsg (final RestTarget target) {
    final FudgeMsgEnvelope fme = getMsgEnvelope (target);
    if (fme == null) return null;
    return fme.getMessage ();
  }
  
  /**
   * Returns NULL for a 404, a message envelope for a 200 series (possibly empty), and throws an exception for anything else.  
   */
  public FudgeMsgEnvelope getMsgEnvelope (final RestTarget target) {
    try {
      final HttpResponse resp = getHttpClient ().execute (setRequestHeaders (new HttpGet (target.getURI ())));
      final int sc = resp.getStatusLine ().getStatusCode ();
      if ((sc >= 200) && (sc < 300)) {
        if (sc == 204) {
          return FudgeContext.EMPTY_MESSAGE_ENVELOPE;
        } else {
          return decodeResponse (resp);
        }
      } else if (sc == 404) {
        return null;
      } else {
        throw new RestRuntimeException ("GET", target, sc, resp.getStatusLine ().getReasonPhrase ());
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException ("I/O exception during GET request", e);
    }
  }
  
  /**
   * Throws an exception for anything other than a 200 series. Returns {@code null} if there was no payload response.
   */
  public FudgeMsgEnvelope put (final RestTarget target, final FudgeFieldContainer msg) {
    return put (target, new FudgeMsgEnvelope (msg));
  }
  
  /**
   * Throws an exception for anything other than a 200 series. Returns {@code null} if there was no payload response
   */
  public FudgeMsgEnvelope put (final RestTarget target, final FudgeMsgEnvelope msgEnvelope) {
    try {
      final HttpResponse resp = getHttpClient ().execute (addFudgePayload (setRequestHeaders (new HttpPut (target.getURI ())), msgEnvelope, target.getTaxonomyId ()));
      final int sc = resp.getStatusLine ().getStatusCode ();
      if ((sc >= 200) && (sc < 300)) {
        if (sc == 204) {
          return null;
        } else {
          return decodeResponse (resp);
        }
      } else {
        throw new RestRuntimeException ("PUT", target, sc, resp.getStatusLine ().getReasonPhrase ());
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException ("I/O exception during PUT request", e);
    }
  }
  
  /**
   * Throws an exception for anything other than a 200 series. Returns {@code null} if there was no payload response
   */
  public FudgeMsgEnvelope post (final RestTarget target) {
    return post (target, (FudgeMsgEnvelope)null);
  }
  
  /**
   * Throws an exception for anything other than a 200 series. Returns {@code null} if there was no payload response
   */
  public FudgeMsgEnvelope post (final RestTarget target, final FudgeFieldContainer msg) {
    return post (target, new FudgeMsgEnvelope (msg));
  }

  /**
   * Throws an exception for anything other than a 200 series. Returns {@code null} if there was no payload response
   */
  public FudgeMsgEnvelope post (final RestTarget target, final FudgeMsgEnvelope msgEnvelope) {
    try {
      HttpPost req = setRequestHeaders (new HttpPost (target.getURI ()));
      if (msgEnvelope != null) {
        req = addFudgePayload (req, msgEnvelope, target.getTaxonomyId ());
      }
      final HttpResponse resp = getHttpClient ().execute (req);
      final int sc = resp.getStatusLine ().getStatusCode ();
      if ((sc >= 200) && (sc < 300)) {
        if (sc == 204) {
          return null;
        } else {
          return decodeResponse (resp);
        }
      } else {
        throw new RestRuntimeException ("POST", target, sc, resp.getStatusLine ().getReasonPhrase ());
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException ("I/O error during POST request", e);
    }
  }
  
  public <T> T getSingleValue (final Class<T> clazz, final RestTarget target, final String returnFieldName) {
    final FudgeFieldContainer msg = getMsg (target);
    if (msg == null) return null;
    final FudgeField field = msg.getByName (returnFieldName);
    if (field == null) return null;
    return getFudgeDeserializationContext ().fieldValueToObject (clazz, field);
  }
  
  public <T> T getSingleValue (final Class<T> clazz, final RestTarget target, final int returnFieldOrdinal) {
    final FudgeFieldContainer msg = getMsg (target);
    if (msg == null) return null;
    final FudgeField field = msg.getByOrdinal (returnFieldOrdinal);
    if (field == null) return null;
    return getFudgeDeserializationContext ().fieldValueToObject (clazz, field);
  }
  
  public <T> T getSingleValueNotNull (final Class<T> clazz, final RestTarget target, final String returnFieldName) {
    final FudgeFieldContainer msg = getMsg (target);
    if (msg == null) throw new RestRuntimeException ("GET", target, 404, "Not Found");
    final FudgeField field = msg.getByName (returnFieldName);
    if (field == null) throw new OpenGammaRuntimeException (target + " did not return a field '" + returnFieldName + "'");
    return getFudgeDeserializationContext ().fieldValueToObject (clazz, field);
  }
  
  public <T> T getSingleValueNotNull (final Class<T> clazz, final RestTarget target, final int returnFieldOrdinal) {
    final FudgeFieldContainer msg = getMsg (target);
    if (msg == null) throw new RestRuntimeException ("GET", target, 404, "Not Found");
    final FudgeField field = msg.getByOrdinal (returnFieldOrdinal);
    if (field == null) throw new OpenGammaRuntimeException (target + " did not return a field " + returnFieldOrdinal);
    return getFudgeDeserializationContext ().fieldValueToObject (clazz, field);
  }
  
  /**
   * Throws an exception for anything other than a 200 series. Returns {@code null} if there was no payload response.
   */
  public FudgeMsgEnvelope delete (final RestTarget target) {
    try {
      final HttpResponse resp = getHttpClient ().execute (setRequestHeaders (new HttpDelete (target.getURI ())));
      final int sc = resp.getStatusLine ().getStatusCode ();
      if ((sc >= 200) && (sc < 300)) {
        if (sc == 204) {
          return null;
        } else {
          return decodeResponse (resp);
        }
      } else {
        throw new RestRuntimeException ("DELETE", target, sc, resp.getStatusLine ().getReasonPhrase ());
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException ("I/O error during DELETE request", e);
    }
  }
  
}