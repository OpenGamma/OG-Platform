/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.joda.beans.Bean;

import com.opengamma.transport.jaxrs.FudgeResponse;

/**
 * Abstract base class for RESTful resources.
 */
public abstract class AbstractDataResource {

  /**
   * Creates the empty RESTful "ok" response object - 200.
   * <p>
   * This is normally used as a response to a ping.
   * 
   * @return the response, not null
   */
  protected Response responseOk() {
    return Response.ok().build();
  }

  /**
   * Creates the empty RESTful "no-content" response object - 204.
   * <p>
   * This is the correct form of response if there is no entity.
   * 
   * @return the response, not null
   */
  protected Response responseOkNoContent() {
    return Response.noContent().build();
  }

  /**
   * Creates the RESTful "created" response object.
   * 
   * @param uri  the URI that was created, may be null if value is null
   * @return the response, not null
   */
  protected Response responseCreated(final URI uri) {
    return Response.created(uri).build();
  }

  /**
   * Creates the RESTful "ok" response object, converting null to a 404.
   * <p>
   * The response will only go via Fudge if the value if a Fudge recognized type.
   * 
   * @param value  the value to contain in the response, or null to trigger a 404
   * @return the response, not null
   */
  protected Response responseOk(final Object value) {
    responseNullTo404(value);
    return Response.ok(value).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the RESTful "ok" response object using Fudge, converting null to a 404.
   * <p>
   * The response will be converted to XML or JSON formatted Fudge on demand.
   * 
   * @param value  the value to contain in the response, or null to trigger a 404
   * @return the response, not null
   */
  protected Response responseOkFudge(final Object value) {
    responseNullTo404(value);
    return Response.ok(encode(value)).build();
  }

  /**
   * Creates the RESTful "created" response object using Fudge, converting null to a 404.
   * <p>
   * The response will be converted to XML or JSON formatted Fudge on demand.
   * 
   * @param uri  the URI that was created, may be null if value is null
   * @param value  the value to contain in the response, or null to trigger a 404
   * @return the response, not null
   */
  protected Response responseCreatedFudge(final URI uri, final Object value) {
    responseNullTo404(value);
    return Response.created(uri).entity(encode(value)).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the value is null and throws a 404 exception.
   * 
   * @param value  the value to check
   * @throws WebApplicationException if the value is null
   */
  protected void responseNullTo404(final Object value) {
    if (value == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  private Object encode(Object value) {
    if (value instanceof FudgeMsgEnvelope || value instanceof FudgeMsg || value instanceof Bean) {
      return value;
    }
    return new FudgeResponse(value);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a HATAOES response for this object.
   * 
   * @param uriInfo the URI info, not null
   * @return the response, not null
   */
  protected Response hateoasResponse(final UriInfo uriInfo) {
    Class<? extends AbstractDataResource> cls = getClass();
    StringBuilder buf = new StringBuilder();
    buf.append("<p>").append(cls.getName()).append("</p>");
    buildHateoas(uriInfo, cls, buf, "");
    return Response.ok(buf.toString()).build();
  }

  private void buildHateoas(final UriInfo uriInfo, final Class<?> cls, final StringBuilder buf, final String basePath) {
    try {
      Method[] methods = cls.getDeclaredMethods();
      for (Method method : methods) {
        if (Modifier.isPublic(method.getModifiers())) {
          String path = basePath;
          if (method.isAnnotationPresent(Path.class)) {
            path += "/" + method.getAnnotation(Path.class).value();
          }
          if (method.isAnnotationPresent(GET.class)) {
            if (path.length() > 0) {
              buf.append("<p>GET ").append(uriInfo.getRequestUri().getPath() + path).append(" => \"").append(method.getName()).append("\"</p>");
            }
          } else if (method.isAnnotationPresent(POST.class)) {
            buf.append("<p>POST ").append(uriInfo.getRequestUri().getPath() + path).append(" => \"").append(method.getName()).append("\"</p>");
          } else if (method.isAnnotationPresent(PUT.class)) {
            buf.append("<p>PUT ").append(uriInfo.getRequestUri().getPath() + path).append(" => \"").append(method.getName()).append("\"</p>");
          } else if (method.isAnnotationPresent(DELETE.class)) {
            buf.append("<p>DELETE ").append(uriInfo.getRequestUri().getPath() + path).append(" => \"").append(method.getName()).append("\"</p>");
          } else if (AbstractDataResource.class.isAssignableFrom(method.getReturnType()) &&
              method.getReturnType() != cls && method.isAnnotationPresent(Path.class)) {
            buildHateoas(uriInfo, method.getReturnType(), buf, path);
          }
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

}
