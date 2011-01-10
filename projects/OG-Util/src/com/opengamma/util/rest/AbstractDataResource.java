/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import org.apache.commons.codec.binary.Base64;
import org.joda.beans.Bean;

import com.opengamma.transport.jaxrs.FudgeRest;

/**
 * Abstract base class for RESTful resources.
 */
@Produces(FudgeRest.MEDIA)
public abstract class AbstractDataResource {

  /**
   * Decodes a bean from base-64 when passed in the URI.
   * <p>
   * This is used to pass a bean in the URI, such as when calling a GET method.
   * 
   * @param <T> the bean type
   * @param cls  the bean class to build, not null
   * @param providers  the providers, not null
   * @param msgBase64  the base-64 Fudge message, not null
   * @return the bean, not null
   */
  public <T extends Bean> T decodeBean(final Class<T> cls, final Providers providers, final String msgBase64) {
    if (msgBase64 == null) {
      try {
        return cls.newInstance();
      } catch (InstantiationException ex) {
        throw new RuntimeException(ex);
      } catch (IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    }
    byte[] msg = Base64.decodeBase64(msgBase64);
    InputStream in = new ByteArrayInputStream(msg);
    MessageBodyReader<T> mbr = providers.getMessageBodyReader(cls, cls, null, FudgeRest.MEDIA_TYPE);
    try {
      return mbr.readFrom(cls, cls, null, FudgeRest.MEDIA_TYPE, null, in);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

}
