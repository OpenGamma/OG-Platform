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
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.integrate.freemarker.FreemarkerObjectWrapper;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;

import com.opengamma.core.user.UserProfile;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.web.user.WebUser;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateScalarModel;

/**
 * Main class that groups functionality for outputting to Freemarker.
 * <p>
 * Freemarker is a template engine that allows Jaa objects to be easily converted to text.
 * The Freemarker system is controlled by a {@link Configuration configuration class}.
 * <p>
 * The configuration is typically managed within the {@link ServletContext}.
 * The application initialization should call {@link #createConfiguration()}
 * followed by {@link #init(ServletContext, Configuration)} to setup the servlet context.
 * This can then be used by subclasses of {@link AbstractPerRequestWebResource}.
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
   * <p>
   * The configuration uses UTF-8, a default locale of English, localized lookup and
   * always includes the file "common/base.ftl".
   * A model is added that converts nulls to empty strings
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

  /**
   * Creates a new Freemarker root data.
   * <p>
   * This creates a new data object to be passed to Freemarker with some standard keys:
   * <ul>
   * <li>now - the current date-time using {@link OpenGammaClock}
   * <li>timeFormatter - a formatter that outputs the time as HH:mm:ss
   * <li>offsetFormatter - a formatter that outputs the time-zone offset
   * </ul>
   * 
   * @return the root data, not null
   */
  public static FlexiBean createRootData() {
    FlexiBean data = new FlexiBean();
    data.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    data.put("locale", Locale.ENGLISH);
    data.put("timeZone", OpenGammaClock.getInstance().getZone());
    data.put("dateFormatter", DateTimeFormatter.ofPattern("d MMM yyyy"));
    data.put("timeFormatter", DateTimeFormatter.ofPattern("HH:mm:ss"));
    data.put("offsetFormatter", new DateTimeFormatterBuilder().appendOffsetId().toFormatter());
    return data;
  }

  /**
   * Creates a new Freemarker root data.
   * <p>
   * This creates a new data object to be passed to Freemarker with some standard keys:
   * <ul>
   * <li>now - the current date-time using {@link OpenGammaClock}
   * <li>timeFormatter - a formatter that outputs the time as HH:mm:ss
   * <li>offsetFormatter - a formatter that outputs the time-zone offset
   * <li>homeUris - the home URIs
   * <li>baseUri - the base URI
   * <li>security - an instance of WebSecurity
   * </ul>
   * 
   * @param uriInfo  the URI information, not null
   * @return the root data, not null
   */
  public static FlexiBean createRootData(UriInfo uriInfo) {
    FlexiBean out = FreemarkerOutputter.createRootData();
    out.put("homeUris", new WebHomeUris(uriInfo));
    out.put("baseUri", uriInfo.getBaseUri().toString());
    WebUser user = new WebUser(uriInfo);
    UserProfile profile = user.getProfile();
    if (profile != null) {
      Locale locale = profile.getLocale();
      ZoneId zone = profile.getZone();
      DateTimeFormatter dateFormatter = profile.getDateStyle().formatter(locale);
      DateTimeFormatter timeFormatter = profile.getTimeStyle().formatter(locale);
      ZonedDateTime now = ZonedDateTime.now(OpenGammaClock.getInstance().withZone(zone));
      out.put("now", now);
      out.put("locale", locale);
      out.put("timeZone", zone);
      out.put("dateFormatter", dateFormatter);
      out.put("timeFormatter", timeFormatter);
    }
    out.put("userSecurity", user);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the resource.
   * <p>
   * This constructor extracts the Freemarker configuration from the {@code ServletContext}.
   * 
   * @param servletContext  the servlet context, not null
   */
  public FreemarkerOutputter(final ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    _configuration = (Configuration) servletContext.getAttribute(FREEMARKER_CONFIGURATION);
    ArgumentChecker.notNull(_configuration, "Freemarker configuration");
  }

  /**
   * Creates the resource.
   * <p>
   * This constructor allows the Freemarker configuration to be directly passed in.
   * It is recommended to use the {@code ServletContext} constructor to allow the configuration
   * to be managed in the context.
   * 
   * @param configuration  the configuration, not null
   */
  public FreemarkerOutputter(final Configuration configuration) {
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
   * Creates a Freemarker template.
   * <p>
   * This converts a template name, which may include a file system path, into a
   * configured {@code Template} object.
   * 
   * @param templateName  the template name, not null
   * @return the template, not null
   * @throws RuntimeException if an error occurs
   */
  public Template createTemplate(final String templateName) {
    try {
      return _configuration.getTemplate(templateName);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builds the Freemarker template creating the output string.
   * <p>
   * The following keys are added to the data if it is a {@code Map} or {@code FlexiBean}:
   * <ul>
   * <li>freemarkerTemplateName - the template name
   * <li>freemarkerLocale - the locale of the template
   * <li>freemarkerVersion - the version of the Freemarker configuration
   * </ul>
   * 
   * @param templateName  the template name, not null
   * @param data  the root data to merge, not null
   * @return the template, not null
   * @throws RuntimeException if an error occurs
   */
  public String build(final String templateName, final Object data) {
    return build(createTemplate(templateName), data);
  }

  /**
   * Builds the Freemarker template creating the output string.
   * <p>
   * The following keys are added to the data if it is a {@code Map} or {@code FlexiBean}:
   * <ul>
   * <li>freemarkerTemplateName - the template name
   * <li>freemarkerLocale - the locale of the template
   * <li>freemarkerVersion - the version of the Freemarker configuration
   * </ul>
   * 
   * @param template  the template, not null
   * @param data  the root data to merge, not null
   * @return the template, not null
   * @throws RuntimeException if an error occurs
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
    return String.format("FreemarkerOutputter[%s]", Configuration.getVersionNumber());
  }

}
