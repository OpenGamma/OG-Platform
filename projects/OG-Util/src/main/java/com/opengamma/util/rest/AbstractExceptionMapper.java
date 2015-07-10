/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * An abstract class to assist with writing JAX-RS exception mappers.
 * 
 * @param <T> the mapped exception type
 */
public abstract class AbstractExceptionMapper<T extends Throwable>
    implements ExceptionMapper<T> {

  /** Logger. */
  protected static final Logger s_logger = LoggerFactory.getLogger(AbstractExceptionMapper.class);

  /**
   * The RESTful request headers.
   */
  @Context
  private HttpHeaders _headers;
  /**
   * The servlet context.
   */
  @Context
  private ServletContext _servletContext;

  /**
   * Creates the mapper.
   */
  protected AbstractExceptionMapper() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Response toResponse(T exception) {
    return createResponse(exception);
  }

  /**
   * Creates the JAX-RS response for the exception.
   * <p>
   * This is the main method invoked by subclasses.
   * 
   * @param exception  the exception being processed, not null
   * @return the response, not null
   */
  public Response createResponse(T exception) {
    if (_headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
      String page = buildHtmlErrorPage(exception);
      logHtmlException(exception, page);
      return doHtmlResponse(exception, page);
    } else {
      logRestfulError(exception);
      return doRestfulResponse(exception);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Provides the HTML error page.
   * 
   * @param exception  the exception being processed, not null
   * @return the HTML error page, null if none
   */
  protected String buildHtmlErrorPage(T exception) {
    return null;
  }

  /**
   * Creates the HTML error page.
   * 
   * @param errorResource  the resource, not null
   * @param data  the substitution data, not null
   * @return the page, null if no page
   */
  protected String createHtmlErrorPage(String errorResource, Map<String, String> data) {
    try (InputStream in = _servletContext.getResourceAsStream("/WEB-INF/pages/errors/" + errorResource)) {
      if (in == null) {
        s_logger.debug("AbstractExceptionMapper resource not found: /WEB-INF/pages/errors/" + errorResource);
        return null;
      }
      List<String> lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
      for (ListIterator<String> it = lines.listIterator(); it.hasNext(); ) {
        String line = (String) it.next();
        for (Entry<String, String> entry : data.entrySet()) {
          line = StringUtils.replace(line, "${" + entry.getKey() + "}", entry.getValue());
          it.set(line);
        }
      }
      return Joiner.on('\n').join(lines);
      
    } catch (IOException | RuntimeException ex) {
      s_logger.debug("AbstractExceptionMapper error", ex);
      return null;
    }
  }

  /**
   * Builds the output message for the exception into the data map.
   * 
   * @param exception  the exception being processed, may be null
   * @param data  the substitution data, not null
   */
  protected void buildOutputMessage(Throwable exception, Map<String, String> data) {
    // includes HTML tags in locator so exception could be switched off in future
    if (exception == null) {
      data.put("message", "");
    } else if (exception.getMessage() == null) {
      if (exception.getCause() != null) {
        buildOutputMessage(exception.getCause(), data);
      } else {
        data.put("message", "");
        data.put("locator", "<p>" + errorLocator(exception) + "</p>");
      }
    } else {
      Throwable rootCause = Throwables.getRootCause(exception);
      if (rootCause instanceof UniformInterfaceException) {
        rootCause = exception;
      }
      String message = exception.getMessage();
      String rootMessage = rootCause.getMessage();
      if (message.contains(rootMessage) == false) {
        message = message + " caused by " + rootMessage;
      }
      data.put("message", message);
      if (Throwables.getRootCause(exception) instanceof UniformInterfaceException == false) {
        data.put("locator", "<p>" + errorLocator(rootCause) + "</p>");
      } else {
        data.put("locator", "");
      }
    }
  }

  private String errorLocator(Throwable exception) {
    String base = exception.getClass().getSimpleName();
    if (exception.getStackTrace().length == 0) {
      return base;
    }
    StrBuilder buf = new StrBuilder(512);
    buf.append(base).append("<br />");
    int count = 0;
    for (int i = 0; i < exception.getStackTrace().length && count < 4; i++) {
      StackTraceElement ste = exception.getStackTrace()[i];
      if (ste.getClassName().startsWith("sun.") || ste.getClassName().startsWith("javax.") || ste.getClassName().startsWith("com.sun.") ||
          (ste.getClassName().equals("java.lang.reflect.Method") && ste.getMethodName().equals("invoke"))) {
        continue;
      }
      if (ste.getLineNumber() >= 0) {
        buf.append(String.format("&nbsp;&nbsp;at %s.%s() L%d<br />", ste.getClassName(), ste.getMethodName(), ste.getLineNumber()));
      } else {
        buf.append(String.format("&nbsp;&nbsp;at %s.%s()<br />", ste.getClassName(), ste.getMethodName()));
      }
      count++;
    }
    return buf.toString();
  }

  /**
   * Returns the HTML response.
   * 
   * @param exception  the exception being processed, not null
   * @param htmlPage  the HTML page, may be null
   * @return the response, not null
   */
  protected abstract Response doHtmlResponse(T exception, String htmlPage);

  /**
   * Logs the error in the HTML scenario.
   * 
   * @param exception  the exception, not null
   * @param htmlPage  the HTML page, may be null
   */
  protected void logHtmlException(T exception, String htmlPage) {
    if (htmlPage != null) {
      s_logger.debug("RESTful website exception caught: " + packStackTrace(exception));
    } else {
      s_logger.info("RESTful website exception caught", exception);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the RESTful response.
   * 
   * @param exception  the exception being processed, not null
   * @return the response, not null
   */
  protected abstract Response doRestfulResponse(T exception);

  /**
   * Logs the error in the RESTful scenario.
   * 
   * @param exception  the exception, not null
   */
  protected void logRestfulError(final T exception) {
    s_logger.info("RESTful web-service exception caught and tunnelled to client:", exception);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an exception to a short stack trace.
   * 
   * @param exception  the exception, not null
   * @return the short stack trace, not null
   */
  protected String packStackTrace(T exception) {
    StackTraceElement[] stackTrace = exception.getStackTrace();
    switch (stackTrace.length) {
      case 0:
        return "Unknown";
      case 1:
        return stackTrace[0].toString();
      case 2:
        return stackTrace[0].toString() + " \n" + stackTrace[1].toString();
      default:
        return stackTrace[0].toString() + " \n" + stackTrace[1].toString() + " \n" + stackTrace[2].toString();
    }
  }

}
