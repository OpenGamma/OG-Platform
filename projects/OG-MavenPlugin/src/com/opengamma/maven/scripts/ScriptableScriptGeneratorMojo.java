/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maven.scripts;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

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
   * @parameter alias="templateFile"
   * @required
   */
  private String _templateFile;
  
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
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Template template;
    try {
      Configuration cfg = new Configuration();
      cfg.setTemplateLoader(new FileTemplateLoader(_baseDir));
      template = cfg.getTemplate(_templateFile);
    } catch (IOException e) {
      throw new MojoExecutionException("Error loading Freemarker template from '" + _templateFile + "'", e);
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
    String[] classpathElements = classpathElementList.toArray(new String[classpathElementList.size()]);
    Set<String> annotationClasses = ClassNameAnnotationScanner.scan(classpathElements, Scriptable.class.getName());
    getLog().info("Generating " + annotationClasses.size() + " scripts");
    
    for (String className : annotationClasses) {
      Map<String, Object> templateData = new HashMap<String, Object>();
      templateData.put("className", className);
      ScriptsGenerator.generate(className, _outputDir, template, templateData);
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

}
