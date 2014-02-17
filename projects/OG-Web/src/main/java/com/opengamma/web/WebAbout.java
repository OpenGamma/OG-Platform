/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.threeten.bp.Duration;
import org.threeten.bp.ZoneId;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClasspathUtils;
import com.opengamma.util.ClasspathUtils.DependencyInfo;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.VersionUtils;
import com.opengamma.util.time.DateUtils;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * RESTful resource for the about page.
 */
@Path("/about")
public class WebAbout {

  /**
   * The servlet context.
   */
  private final ServletContext _servletContext;

  /**
   * Creates the resource.
   * 
   * @param servletContext  the servlet context, not null
   */
  public WebAbout(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    _servletContext = servletContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the ServletContext.
   * @return the context
   */
  public ServletContext getServletContext() {
    return _servletContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the default time-zone.
   * @return the default time-zone
   */
  public ZoneId getClockTimeZone() {
    return OpenGammaClock.getZone();
  }

  /**
   * Gets the default time-zone.
   * @return the default time-zone
   */
  public String getDefaultTimeZone() {
    return TimeZone.getDefault().getID();
  }

  /**
   * Gets the default time-zone.
   * @return the default time-zone
   */
  public ZoneId getOriginalTimeZone() {
    return DateUtils.ORIGINAL_TIME_ZONE;
  }

  /**
   * Gets the default locale.
   * @return the default locale
   */
  public Locale getDefaultLocale() {
    return Locale.getDefault();
  }

  /**
   * Gets the commons system utils.
   * @return the system utils
   */
  public TemplateModel getSystemUtils() {
    try {
      return BeansWrapper.getDefaultInstance().getStaticModels().get(SystemUtils.class.getName());
    } catch (TemplateModelException ex) {
      return TemplateModel.NOTHING;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the uptime.
   * @return the JVM uptime
   */
  public Duration getJvmUptime() {
    return Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
  }

  /**
   * Gets the JVM input arguments.
   * @return the JVM input arguments
   */
  public List<String> getJvmArguments() {
    List<String> args = new ArrayList<>(ManagementFactory.getRuntimeMXBean().getInputArguments());
    for (ListIterator<String> it = args.listIterator(); it.hasNext(); ) {
      String arg = it.next();
      if (arg.contains("secret") || arg.contains("password")) {
        int index = arg.indexOf("secret") + 6;
        if (index < 0) {
          index = arg.indexOf("password") + 8;
        }
        it.set(arg.substring(0, index) + "*************");
      }
    }
    return args;
  }

  /**
   * Gets the thread JMX.
   * @return the thread JMX
   */
  public ThreadMXBean getThreadJmx() {
    return ManagementFactory.getThreadMXBean();
  }

  /**
   * Gets the memory JMX.
   * @return the memory JMX
   */
  public MemoryMXBean getMemoryJmx() {
    return ManagementFactory.getMemoryMXBean();
  }

  /**
   * Gets the class loading JMX.
   * @return the class loading JMX
   */
  public ClassLoadingMXBean getClassLoadingJmx() {
    return ManagementFactory.getClassLoadingMXBean();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the OpenGamma version.
   * @return the version, not null
   */
  public String getOpenGammaVersion() {
    String version = VersionUtils.deriveVersion();
    return StringUtils.defaultIfEmpty(version, "?");
  }

  /**
   * Gets the OpenGamma build.
   * @return the build, not null
   */
  public String getOpenGammaBuild() {
    String build = VersionUtils.deriveBuild();
    return StringUtils.defaultIfEmpty(build, "?");
  }

  /**
   * Gets the OpenGamma build ID.
   * @return the build ID, not null
   */
  public String getOpenGammaBuildId() {
    String buildId = VersionUtils.deriveBuildId();
    return StringUtils.defaultIfEmpty(buildId, "?");
  }

  /**
   * Gets the classpath dependencies.
   * @return the classpath, not null
   */
  public List<DependencyInfo> getClasspath() {
    return ClasspathUtils.getDependencies();
  }

}
