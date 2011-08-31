/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.lang.reflect.Field;

/**
 * <em>I'm sure this class is reinventing functionality that already exists in Spring but I can't find it.
 * Ideally this class should be removed and replaced with Spring's implementation rather than
 * reinventing the wheel.</em>
 *
 * <p>This class's {@link #init(ServletConfig)} method populates any fields annotated with {@link Autowired} using
 * beans from the Spring {@link ApplicationContext} found in the servlet context.  It is a naive implementation that
 * assumes there is exatly one bean in the context with a compatible type for each field.</p>
 */
public abstract class SpringConfiguredServlet extends HttpServlet {

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
    Field[] fields = getClass().getDeclaredFields();
    for (Field field : fields) {
      Autowired annotation = field.getAnnotation(Autowired.class);
      if (annotation != null) {
        try {
          field.setAccessible(true);
          field.set(this, ctx.getBean(field.getType()));
        } catch (IllegalAccessException e) {
          throw new ServletException("Unable to configure servlet", e);
        }
      }
    }
  }
}
