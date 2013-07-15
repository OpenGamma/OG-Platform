/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;

/**
 * Error handler that returns simple, plain text responses.
 */
public class PlainTextErrorHandler extends ErrorHandler {

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    baseRequest.setHandled(true);
    String method = request.getMethod();
    if (!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST) && !method.equals(HttpMethods.HEAD)) {
      return;
    }
    response.setContentType(MimeTypes.TEXT_PLAIN_8859_1);
    response.setHeader(HttpHeaders.CACHE_CONTROL, "must-revalidate,no-cache,no-store");
    ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(4096);
    handleErrorPage(request, writer, baseRequest.getResponse().getStatus(), baseRequest.getResponse().getReason());
    writer.flush();
    response.setContentLength(writer.size());
    writer.writeTo(response.getOutputStream());
    writer.destroy();
  }

  protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
    writer.write("Problem accessing ");
    writer.write(request.getRequestURI());
    writer.write("\nReason: ");
    writer.write(message != null ? message : HttpStatus.getMessage(code));
  }

}
