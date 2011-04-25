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
 * See http://cometd.org/book/export/html/64.
 */
public class ConfigurationServlet extends GenericServlet {
  
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

  public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
    throw new ServletException();
  }
}
