/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.util.Locale;

import javax.servlet.ServletContext;

import org.joda.beans.integrate.freemarker.FreemarkerObjectWrapper;

import com.opengamma.util.ArgumentChecker;

import freemarker.template.Configuration;
import freemarker.template.TemplateScalarModel;

/**
 * Abstract base class for RESTful portfolio resources.
 */
final class FreemarkerConfigurationFactory {

  /**
   * The singleton instance.
   */
  private static volatile Configuration s_configuration;

  /**
   * Obtains an instance of the Freemarker configuration.
   * @param servletContext  the servlet context, not null
   * @return the configuration, not null
   */
  static Configuration instance(final ServletContext servletContext) {
    Configuration cfg = s_configuration;
    if (cfg == null) {
      ArgumentChecker.notNull(servletContext, "servletContext");
      synchronized (FreemarkerConfigurationFactory.class) {
        cfg = s_configuration;
        if (cfg == null) {
          cfg = new Configuration();
          cfg.setServletContextForTemplateLoading(servletContext, "WEB-INF/pages");
          cfg.setDefaultEncoding("UTF-8");
          cfg.setOutputEncoding("UTF-8");
          cfg.setLocale(Locale.ENGLISH);
          cfg.setLocalizedLookup(true);
          cfg.addAutoInclude("common/base.ftl");
          FreemarkerObjectWrapper objectWrapper = new FreemarkerObjectWrapper();
          objectWrapper.setNullModel(TemplateScalarModel.EMPTY_STRING);
          cfg.setObjectWrapper(objectWrapper);
          s_configuration = cfg;
        }
      }
    }
    return cfg;
  }

  /**
   * Restricted construcor.
   */
  private FreemarkerConfigurationFactory() {
  }

}
