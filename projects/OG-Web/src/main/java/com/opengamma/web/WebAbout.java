/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.ZoneId;

import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.time.DateUtils;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * RESTful resource for the about page.
 */
@Path("/about")
public class WebAbout extends AbstractWebResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(WebAbout.class);
  /** Build name. */
  private static final Attributes.Name IMPLEMENTATION_BUILD = new Attributes.Name("Implementation-Build");

  /**
   * The servlet context.
   */
  private final ServletContext _servletContext;
  /**
   * The classpath.
   */
  private final List<AboutDependency> _classpath;

  /**
   * Creates the resource.
   * 
   * @param servletContext  the servlet context, not null
   */
  public WebAbout(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    _servletContext = servletContext;
    Set<URL> classpathUrls = forManifest(forJavaClassPath());
    List<AboutDependency> classpath = new ArrayList<>();
    for (URL url : classpathUrls) {
      classpath.add(new AboutDependency(url));
    }
    _classpath = classpath;
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
    URL url = ClasspathHelper.forClass(getClass(), getClass().getClassLoader());
    if (url != null && url.toString().contains(".jar")) {
      try {
        final String part = ClasspathHelper.cleanPath(url);
        try (JarFile myJar = new JarFile(part)) {
          final Manifest manifest = myJar.getManifest();
          if (manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            if (attributes != null) {
              if (attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION) != null) {
                return attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
              }
              if (attributes.getValue(Attributes.Name.SPECIFICATION_VERSION) != null) {
                return attributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
              }
            }
          }
        }
      } catch (Exception ex) {
        s_logger.warn(ex.getMessage(), ex);
      }
    } else {
      List<AboutDependency> classpath = getClasspath();
      for (AboutDependency depend : classpath) {
        if ("og-web".equals(depend.getArtifactId())) {
          return depend.getVersion();
        }
      }
    }
    return "?";
  }

  /**
   * Gets the OpenGamma version.
   * @return the version, not null
   */
  public String getOpenGammaBuild() {
    URL url = ClasspathHelper.forClass(getClass(), getClass().getClassLoader());
    if (url != null && url.toString().contains(".jar")) {
      try {
        final String part = ClasspathHelper.cleanPath(url);
        try (JarFile myJar = new JarFile(part)) {
          final Manifest manifest = myJar.getManifest();
          if (manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            if (attributes != null) {
              if (attributes.getValue(IMPLEMENTATION_BUILD) != null) {
                return attributes.getValue(IMPLEMENTATION_BUILD);
              }
            }
          }
        }
      } catch (Exception ex) {
        s_logger.warn(ex.getMessage(), ex);
      }
    }
    return "?";
  }

  /**
   * Gets the classpath.
   * @return the classpath, not null
   */
  public List<AboutDependency> getClasspath() {
    return new ArrayList<>(_classpath);
  }

  //-------------------------------------------------------------------------
  private static Set<URL> forJavaClassPath() {
    Set<URL> urls = Sets.newLinkedHashSet();
    String javaClassPath = System.getProperty("java.class.path");
    if (javaClassPath != null) {
      for (String path : javaClassPath.split(File.pathSeparator)) {
        try {
          urls.add(new File(path).toURI().toURL());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return urls;
  }

  private static Set<URL> forManifest(final Iterable<URL> urls) {
    Set<URL> result = Sets.newLinkedHashSet();
    for (URL url : urls) {
      result.addAll(forManifest(url));
    }
    return result;
  }

  private static Set<URL> forManifest(final URL url) {
    Set<URL> result = Sets.newLinkedHashSet();
    result.add(url);
    try {
      final String part = ClasspathHelper.cleanPath(url);
      File jarFile = new File(part);
      try (JarFile myJar = new JarFile(part)) {
        URL validUrl = tryToGetValidUrl(jarFile.getPath(), new File(part).getParent(), part);
        if (validUrl != null) {
          result.add(validUrl);
        }
        final Manifest manifest = myJar.getManifest();
        if (manifest != null) {
          final String classPath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
          if (classPath != null) {
            for (String jar : classPath.split(" ")) {
              validUrl = tryToGetValidUrl(jarFile.getPath(), new File(part).getParent(), jar);
              if (validUrl != null) {
                result.add(validUrl);
              }
            }
          }
        }
      }
    } catch (IOException e) {
      // don't do anything, we're going on the assumption it is a jar, which could be wrong
    }
    return result;
  }

  private static URL tryToGetValidUrl(String workingDir, String path, String filename) {
    try {
      if (new File(filename).exists()) {
        return new File(filename).toURI().toURL();
      }
      if (new File(path + File.separator + filename).exists()) {
        return new File(path + File.separator + filename).toURI().toURL();
      }
      if (new File(workingDir + File.separator + filename).exists()) {
        return new File(workingDir + File.separator + filename).toURI().toURL();
      }
      if (new File(new URL(filename).getFile()).exists()) {
        return new File(new URL(filename).getFile()).toURI().toURL();
      }
    } catch (MalformedURLException e) {
      // don't do anything, we're going on the assumption it is a jar, which could be wrong
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Dependency accessor.
   */
  public static class AboutDependency {
    private final URL _url;
    private final String _version;
    private final String _groupId;
    private final String _artifactId;

    public AboutDependency(URL url) {
      _url = url;
      
      FilterBuilder filter = new FilterBuilder().include(".*pom[.]properties");
      Reflections ref = new Reflections(new ResourcesScanner(), _url, WebAbout.class.getClassLoader(), filter);
      Set<String> resources = ref.getResources(new FilterBuilder().include("pom[.]properties"));
      Properties properties = new Properties();
      if (resources.size() == 1) {
        String relativePath = resources.iterator().next();
        try (URLClassLoader cl = new URLClassLoader(new URL[] {_url})) {
          URL resource = cl.getResource(relativePath);
          if (resource != null) {
            try (InputStream in = resource.openStream()) {
              properties.load(in);
            } catch (IOException ex) {
              s_logger.debug(ex.getMessage(), ex);
            }
          }
        } catch (IOException ex2) {
          s_logger.debug(ex2.getMessage(), ex2);
        }
      } else if (_url.toString().endsWith(".jar")) {
        String name = StringUtils.substringAfterLast(_url.toString(), "/");
        name = StringUtils.substringBeforeLast(name, ".jar");
        properties.setProperty("version", StringUtils.substringAfterLast(name, "-"));
        properties.setProperty("groupId", "?");
        properties.setProperty("artifactId", StringUtils.substringBeforeLast(name, "-"));
      }
      _version = properties.getProperty("version", "?");
      _groupId = properties.getProperty("groupId", "?");
      _artifactId = properties.getProperty("artifactId", "?");
    }

    /**
     * Is the dependency a Jar file.
     * @return true if a jar
     */
    public boolean isJarFile() {
      return _url.toString().endsWith(".jar");
    }

    /**
     * Is the dependency information parsed.
     * @return true if a jar
     */
    public boolean isInfoParsed() {
      return "?".equals(_version) == false;
    }

    /**
     * Is the groupId information parsed.
     * @return true if a jar
     */
    public boolean isGroupParsed() {
      return "?".equals(_groupId) == false;
    }

    /**
     * Get URL.
     * @return the URL
     */
    public URL getUrl() {
      return _url;
    }

    /**
     * Get version.
     * @return the version
     */
    public String getVersion() {
      return _version;
    }

    /**
     * Get group id.
     * @return the group id
     */
    public String getGroupId() {
      return _groupId;
    }

    /**
     * Get artifact id.
     * @return the artifact id
     */
    public String getArtifactId() {
      return _artifactId;
    }

  }

}
