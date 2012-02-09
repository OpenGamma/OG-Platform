/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatters;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.util.OpenGammaClock;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Main class that groups functionality for outputting to Freemarker.
 * <p>
 * An instance of this class is intended to be used from multiple threads,
 * however thread-safety is not enforced.
 */
public class FreemarkerOutputter {

  /**
   * The Freemarker configuration.
   */
  private final Configuration _configuration;

  /**
   * Creates the resource.
   */
  FreemarkerOutputter(final ServletContext servletContext) {
    _configuration = FreemarkerConfigurationFactory.instance(servletContext);
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
