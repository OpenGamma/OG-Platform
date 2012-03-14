/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatters;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.integrate.freemarker.FreemarkerObjectWrapper;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateScalarModel;

/**
 * Main class that groups functionality for outputting to Freemarker.
 * <p>
 * An instance of this class is intended to be used from multiple threads,
 * however thread-safety is not enforced.
 */
public class FreemarkerOutputter {

  /**
   * The servlet context attribute.
   */
  private static final String FREEMARKER_CONFIGURATION = FreemarkerOutputter.class.getName() + ".FreemarkerConfiguration";

  /**
   * The Freemarker configuration.
   */
  private final Configuration _configuration;

  /**
   * Creates the Freemarker system configuration.
   * <p>
   * This creates the {@link Configuration Freemarker configuration} which must be customised
   * with a template loader. Callers must then invoke {@link #init(ServletContext, Configuration)}.
   * 
   * @return the standard Freemarker configuration, not null
   */
  public static Configuration createConfiguration() {
    Configuration cfg = new Configuration();
    cfg.setDefaultEncoding("UTF-8");
    cfg.setOutputEncoding("UTF-8");
    cfg.setLocale(Locale.ENGLISH);
    cfg.setLocalizedLookup(true);
    cfg.addAutoInclude("common/base.ftl");
    FreemarkerObjectWrapper objectWrapper = new FreemarkerObjectWrapper();
    objectWrapper.setNullModel(TemplateScalarModel.EMPTY_STRING);
    cfg.setObjectWrapper(objectWrapper);
    return cfg;
  }

  /**
   * Initializes the Freemarker system.
   * <p>
   * This stores the {@link Configuration Freemarker configuration} in the servlet context for later use.
   * 
   * @param servletContext  the servlet context, not null
   * @param configuration  the configuration to use, not null
   */
  public static void init(ServletContext servletContext, Configuration configuration) {
    servletContext.setAttribute(FreemarkerOutputter.FREEMARKER_CONFIGURATION, configuration);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the resource.
   * 
   * @param servletContext  the servlet context, not null
   */
  FreemarkerOutputter(final ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    _configuration = (Configuration) servletContext.getAttribute(FREEMARKER_CONFIGURATION);
  }

  /**
   * Creates the resource.
   * 
   * @param configuration  the configuration, not null
   */
  FreemarkerOutputter(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Freemarker configuration, which must not be altered.
   * 
   * @return the configuration, not null
   */
  public Configuration getConfiguration() {
    return _configuration;
  }

  /**
   * Creates a new Freemarker root data map.
   * 
   * @return the root data map, not null
   */
  public FlexiBean createRootData() {
    FlexiBean data = new FlexiBean();
    data.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    data.put("timeFormatter", DateTimeFormatters.pattern("HH:mm:ss"));
    data.put("offsetFormatter", DateTimeFormatters.pattern("Z"));
    return data;
  }

  /**
   * Creates a Freemarker template.
   * 
   * @param templateName  the template name, not null
   * @return the template, not null
   */
  public Template createTemplate(final String templateName) {
    try {
      return _configuration.getTemplate(templateName);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Builds the Freemarker template creating the output string.
   * 
   * @param templateName  the template name, not null
   * @param data  the root data to merge, not null
   * @return the template, not null
   */
  public String build(final String templateName, final Object data) {
    return build(createTemplate(templateName), data);
  }

  /**
   * Builds the Freemarker template creating the output string.
   * 
   * @param template  the template, not null
   * @param data  the root data to merge, not null
   * @return the template, not null
   */
  @SuppressWarnings("unchecked")
  public String build(final Template template, final Object data) {
    if (data instanceof FlexiBean) {
      ((FlexiBean) data).put("freemarkerTemplateName", template.getName());
      ((FlexiBean) data).put("freemarkerLocale", template.getLocale());
      ((FlexiBean) data).put("freemarkerVersion", Configuration.getVersionNumber());
    }
    if (data instanceof Map<?, ?>) {
      ((Map<String, Object>) data).put("freemarkerTemplateName", template.getName());
      ((Map<String, Object>) data).put("freemarkerLocale", template.getLocale());
      ((Map<String, Object>) data).put("freemarkerVersion", Configuration.getVersionNumber());
    }
    try {
      StringWriter out = new StringWriter(1024 * 4);
      template.process(data, out);
      out.close();
      return out.toString();
    } catch (Exception ex) {
      return handleException(ex);
    }
  }

  /**
   * Handles any exception in template output.
   * 
   * @param ex  the exception from Freemarker, not null
   * @return a dummy return type for Java compiler reasons
   */
  private String handleException(final Exception ex) {
    throw new RuntimeException(ex);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "FreemarkerOutputter[" + Configuration.getVersionNumber() + "]";
  }

}
