/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maven.scripts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opengamma.maven.MojoUtils;
import com.opengamma.util.ClasspathUtils;
import com.opengamma.util.annotation.ClassNameAnnotationScanner;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.generate.scripts.ScriptsGenerator;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Maven plugin for generating scripts to run tools annotated with {@link Scriptable}.
 * <p>
 * Variables available in the Freemarker template are:
 * <ul>
 * <li> className - the fully-qualified Java class name
 * </ul>
 * 
 * @goal scripts-tools
 * @requiresDependencyResolution compile
 * @threadSafe
 */
public class ScriptableScriptGeneratorMojo extends AbstractMojo {

  /**
   * @parameter alias="outputDir" default-value="${project.build.directory}/scripts-tools"
   * @required
   */
  private File _outputDir;
  /**
   * @parameter alias="template"
   */
  private String _template;
  /**
   * @parameter alias="baseClassTemplateMap"
   */
  private Map<String, String> _baseClassTemplateMap;
  /**
   * @parameter alias="resourceArtifact"
   */
  private String _resourceArtifact;
  /**
   * @parameter alias="additionalScripts"
   */
  private String[] _additionalScripts;
  /**
   * @parameter default-value="${project.basedir}"
   * @required
   */
  private File _baseDir;
  
  /**
   * @component
   */
  private MavenProject _project;
  
  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    boolean templateSet = !StringUtils.isBlank(_template);
    boolean templateMapSet = _baseClassTemplateMap != null && _baseClassTemplateMap.isEmpty();
    if ((templateSet && templateMapSet) || (!templateSet && !templateMapSet)) {
      throw new MojoExecutionException("Exactly one of 'template' or 'baseClassTemplateMap' must be set");
    }
    if (!_outputDir.exists()) {
      try {
        getLog().debug("Creating output directory " + _outputDir);
        if (!_outputDir.mkdirs()) {
          throw new MojoExecutionException("Unable to create output directory " + _outputDir.getAbsolutePath());
        }
      } catch (Exception e) {
        throw new MojoExecutionException("Error creating output directory " + _outputDir.getAbsolutePath());
      }
    }
    
    List<String> classpathElementList;
    try {
      classpathElementList = _project.getCompileClasspathElements();
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException("Error obtaining dependencies", e);
    }
    URL[] classpathUrls = ClasspathUtils.getClasspathURLs(classpathElementList);
    Set<String> annotationClasses = ClassNameAnnotationScanner.scan(classpathUrls, Scriptable.class.getName());
    getLog().info("Generating " + annotationClasses.size() + " scripts");
    
    ClassLoader classLoader = new URLClassLoader(classpathUrls, this.getClass().getClassLoader());
    Map<Class<?>, Template> templateMap = resolveTemplateMap(classLoader);
    
    for (String className : annotationClasses) {
      Map<String, Object> templateData = new HashMap<String, Object>();
      templateData.put("className", className);
      Template template = getTemplateForClass(className, classLoader, templateMap);
      if (template == null) {
        getLog().warn("No template for scriptable class " + className);
        continue;
      }
      ScriptsGenerator.generate(className, _outputDir, template, templateData, false);
    }
    
    if (_additionalScripts != null) {
      getLog().info("Copying " + _additionalScripts.length + " additional script(s)");
      for (String script : _additionalScripts) {
        File scriptFile = new File(script);
        if (scriptFile.exists()) {
          if (!scriptFile.isFile()) {
            throw new MojoExecutionException("Can only copy files, but additional script '" + scriptFile + "' is not a file");
          }
          try {
            FileUtils.copyFileToDirectory(scriptFile, _outputDir);
            File copiedFile = new File(_outputDir, scriptFile.getName());
            copiedFile.setReadable(true, false);
            copiedFile.setExecutable(true, false);
          } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy additional script '" + scriptFile + "'", e);
          }
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  private Map<Class<?>, Template> resolveTemplateMap(ClassLoader classLoader) throws MojoExecutionException {
    if (_baseClassTemplateMap == null || _baseClassTemplateMap.isEmpty()) {
      return ImmutableMap.<Class<?>, Template>of(Object.class, getTemplate(_template));
    }
    Map<Class<?>, Template> templateMap = new HashMap<Class<?>, Template>();
    for (Map.Entry<String, String> unresolvedEntry : _baseClassTemplateMap.entrySet()) {
      String className = unresolvedEntry.getKey();
      Class<?> clazz;
      try {
        clazz = classLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("Unable to resolve class " + className);
      }
      Template template = getTemplate(unresolvedEntry.getValue());
      templateMap.put(clazz, template);
    }
    return templateMap;
  }
  
  private Template getTemplateForClass(String className, ClassLoader classLoader, Map<Class<?>, Template> templateMap) throws MojoExecutionException {
    if (templateMap.size() == 1 && Object.class.equals(Iterables.getOnlyElement(templateMap.keySet()))) {
      return Iterables.getOnlyElement(templateMap.values());
    }
    Class<?> clazz;
    try {
      clazz = classLoader.loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new MojoExecutionException("Unable to resolve class " + className);
    }
    for (Map.Entry<Class<?>, Template> templateMapEntry : templateMap.entrySet()) {
      if (templateMapEntry.getKey().isAssignableFrom(clazz)) {
        return templateMapEntry.getValue();
      }
    }
    return null;
  }

  private Template getTemplate(String templateName) throws MojoExecutionException {
    try {
      if (_resourceArtifact == null) {
        return getTemplateFromFile(_baseDir, templateName);
      } else {
        ClassLoader resourceLoader = getResourceLoader(_resourceArtifact, _project);
        InputStream resourceStream = resourceLoader.getResourceAsStream(templateName);
        if (resourceStream == null) {
          throw new MojoExecutionException("Resource '" + templateName + "' not found");
        }
        String templateStr;
        try {
          templateStr = IOUtils.toString(resourceStream);
        } finally {
          resourceStream.close();
        }
        return new Template(templateName, new StringReader(templateStr), new Configuration());
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Error loading Freemarker template from '" + templateName + "'", e);
    }
  }

  private static ClassLoader getResourceLoader(String resourceArtifact, MavenProject project) throws MojoExecutionException, MalformedURLException {
    if (StringUtils.isBlank(resourceArtifact)) {
      return ScriptableScriptGeneratorMojo.class.getClassLoader();
    }
    File artifactFile = MojoUtils.getArtifactFile(resourceArtifact, project);
    return new URLClassLoader(new URL[] { artifactFile.toURI().toURL() });
  }

  private static Template getTemplateFromFile(File baseDir, String templateFile) throws IOException {
    Template template;
    Configuration cfg = new Configuration();
    cfg.setTemplateLoader(new FileTemplateLoader(baseDir));
    template = cfg.getTemplate(templateFile);
    return template;
  }

}
