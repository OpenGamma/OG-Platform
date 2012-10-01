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
import org.apache.maven.artifact.Artifact;
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
   */
  private String _templateFile;
  /**
   * @parameter alias="resourceArtifact"
   */
  private String _resourceArtifact;
  /**
   * @parameter alias="templateResource"
   */
  private String _templateResource;
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
  
  @SuppressWarnings("unchecked")
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Template template = getTemplate();
    
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

  private Template getTemplate() throws MojoExecutionException, MojoFailureException {
    try {
      if (!StringUtils.isBlank(_templateFile)) {
        return getTemplateFromFile(_baseDir, _templateFile);
      } else if (!StringUtils.isBlank(_templateResource)) {
        ClassLoader resourceLoader = getResourceLoader(_resourceArtifact, _project);
        InputStream resourceStream = resourceLoader.getResourceAsStream(_templateResource);
        if (resourceStream == null) {
          throw new MojoExecutionException("Resource '" + _templateResource + "' not found");
        }
        String templateStr;
        try {
          templateStr = IOUtils.toString(resourceStream);
        } finally {
          resourceStream.close();
        }
        return new Template(_templateResource, new StringReader(templateStr), new Configuration());
      } else {
        throw new MojoFailureException("templateFile or templateResource must be specified");
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Error loading Freemarker template from '" + _templateFile + "'", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static ClassLoader getResourceLoader(String resourceArtifact, MavenProject project) throws MojoExecutionException, MalformedURLException {
    if (StringUtils.isBlank(resourceArtifact)) {
      return ScriptableScriptGeneratorMojo.class.getClassLoader();
    }
    String[] artifactParts = resourceArtifact.split(":");
    if (artifactParts.length < 2 || artifactParts.length > 4) {
      throw new MojoExecutionException("resourceArtifact must be of the form groupId:artifactId[:type[:classifier]]");
    }
    String groupId = artifactParts[0];
    String artifactId = artifactParts[1];
    String type = artifactParts.length > 2 ? artifactParts[2] : null;
    String classifier = artifactParts.length > 3 ? artifactParts[3] : null;
    
    File artifactFile = null;
    for (Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts()) {
      if (groupId.equals(artifact.getGroupId()) && artifactId.equals(artifact.getArtifactId()) &&
          (type == null || type.equals(artifact.getType())) && (classifier == null || classifier.equals(artifact.getClassifier()))) {
        artifactFile = artifact.getFile();
        break;
      }
    }
    if (artifactFile == null) {
      throw new MojoExecutionException("Unable to find artifact with coordinates '" + resourceArtifact + "'");
    }
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
