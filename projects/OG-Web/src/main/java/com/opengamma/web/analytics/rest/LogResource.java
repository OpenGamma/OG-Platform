/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST endpoint that allows the web client to log errors on the server. Messages are logged at error level
 * with a logger name starting with {@link #LOGGER_NAME}.
 * TODO harden this for real deployments - escape the message, limit the size?
 */
@Path("clienterror")
public class LogResource {

  /** Default name for the logger. */
  private static final String LOGGER_NAME = "com.opengamma.web.js";

  /**
   * Logs an error to the server-side log.
   * @param loggerNameSuffix Suffix for the logger name - this is appended to {@link #LOGGER_NAME} to generate
   * the full logger name. Can be null in which case {@link #LOGGER_NAME} is used for the name
   * @param message The error message
   */
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  public void logClientError(@FormParam("logger") String loggerNameSuffix,
                             @FormParam("message") String message) {
    String loggerName;
    if (loggerNameSuffix == null) {
      loggerName = LOGGER_NAME;
    } else {
      loggerName = LOGGER_NAME + "." + loggerNameSuffix;
    }
    Logger logger = LoggerFactory.getLogger(loggerName);
    logger.error(message);
  }
}
