/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.reports.Report;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Writes {@link Report} instance into the body of an HTTP reponse.
 */
@Provider
public class ReportMessageBodyWriter implements MessageBodyWriter<Report> {

  /**
   * @param type Must be {@link Report}
   * @return {@code true} if {@code type} is {@link Report}
   */
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Report.class.equals(type);
  }

  /**
   * TODO is this a problem?
   * @return -1 (size not known)
   */
  @Override
  public long getSize(Report report, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  /**
   * Writes from {@link Report#getInputStream()} to {@code entityStream}.
   */
  @Override
  public void writeTo(Report report,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    InputStream inputStream = null;
    try {
      inputStream = report.getInputStream();
      IOUtils.copy(inputStream, entityStream);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }
}
