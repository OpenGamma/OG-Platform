/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet to configure the cometd system.
 */
public class ConfigurationServlet extends GenericServlet {
  // See http://cometd.org/book/export/html/64

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public void init() throws ServletException {
    // Grab Spring's ApplicationContent
    ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

    // Trigger service initialization
    try {
      ((LiveResultsServiceBean) context.getBean("webInterfaceBean")).afterPropertiesSet();
    } catch (BeansException e) {
      throw new RuntimeException("Could not obtain webInterfaceBean", e);      
    }
  }

  @Override
  public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
    throw new ServletException();
  }

}
