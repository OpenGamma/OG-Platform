/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.sass;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletContext;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.sass.JRubySassCompiler;

/**
 * Creates a Jruby sass compiler with load paths correctly setup
 */
public final class WebJRubySassCompiler extends JRubySassCompiler {
  
  /**
   * The servlet context attribute.
   */
  private static final String JRUBY_SASS_COMPILER = JRubySassCompiler.class.getName() + ".Instance";

  private WebJRubySassCompiler(final List<String> loadPaths) {
    super(loadPaths);
  }

  public static void init(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    
    try {
      URL resource = servletContext.getResource("/WEB-INF/lib/sass/lib");
      if (resource == null) {
        throw new OpenGammaRuntimeException("Sass gem  is missing in web-engine/WEB-INF/lib");
      }
      String path = resource.getPath();
      WebJRubySassCompiler sassCompiler = new WebJRubySassCompiler(ImmutableList.of(path));
      servletContext.setAttribute(JRUBY_SASS_COMPILER, sassCompiler);
    } catch (MalformedURLException ex) {
      // path name should be in right format
    }
  }
  
  public static WebJRubySassCompiler of(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    
    WebJRubySassCompiler compiler = null;
    
    Object attribute = servletContext.getAttribute(JRUBY_SASS_COMPILER);
    if (attribute != null && attribute instanceof WebJRubySassCompiler) {
      compiler = (WebJRubySassCompiler) attribute;
    }
    return compiler;
  }
  
}
