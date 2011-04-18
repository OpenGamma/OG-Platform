/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans for annotations and writes the names of classes using them to a file.
 */
public class AnnotationScannerTask extends Task {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AnnotationScannerTask.class);
  
  private String _outputFile;
  private String _annotationClassName;
  
  private String[] _classpath;
  
  private String getOutputFile() {
    return _outputFile;
  }
  
  public void setOutputFile(String outputFile) {
    _outputFile = outputFile;
  }
  
  public String getAnnotationClassName() {
    return _annotationClassName;
  }
  
  public void setAnnotationClassName(String annotationClassName) {
    _annotationClassName = annotationClassName;
  }
  
  private String[] getClasspath() {
    return _classpath;
  }
  
  public void setClasspathref(Reference classpathref) {
    Path classpath = new Path(getProject());
    classpath.setRefid(classpathref);
    _classpath = classpath.list();
  }
  
  //-------------------------------------------------------------------------
  
  @Override
  public void execute() throws BuildException {
    if (getOutputFile() == null) {
      throw new BuildException("outputFile must be set");
    }
    File outputFile = new File(getOutputFile());
    if (outputFile.exists() && !outputFile.canWrite()) {
      throw new BuildException("Cannot write to file '" + outputFile + "'");
    }
    if (getAnnotationClassName() == null) {
      throw new BuildException("annotationClassName must be set");
    }
    
    URL[] classpathUrlArray = getClasspathURLs();
    AnnotationDB annotationDb = getAnnotationDb(classpathUrlArray);
    Set<String> classNames = annotationDb.getAnnotationIndex().get(getAnnotationClassName());
    if (classNames == null) {
      classNames = Collections.emptySet();
    }
    try {
      PrintWriter writer = new PrintWriter(outputFile);
      for (String className : classNames) {
        writer.println(className);
      }
      writer.close();
    } catch (IOException e) {
      s_logger.error("Error writing to output file", e);
      throw new BuildException("Error writing to output file", e);
    }
  }

  private AnnotationDB getAnnotationDb(URL[] classpathUrlArray) {
    AnnotationDB annotationDb = new AnnotationDB();
    annotationDb.setScanClassAnnotations(true);
    annotationDb.setScanMethodAnnotations(true);
    annotationDb.setScanFieldAnnotations(true);
    annotationDb.setScanParameterAnnotations(false);
    try {
      annotationDb.scanArchives(classpathUrlArray);
    } catch (IOException e) {
      throw new BuildException("Error scanning for annotations", e);
    }
    return annotationDb;
  }

  private URL[] getClasspathURLs() {
    String[] classpath = getClasspath();
    if (classpath == null) {
      return new URL[0];
    }
    Set<URL> classpathUrls = new HashSet<URL>();
    for (String classpathEntry : classpath) {
      File f = new File(classpathEntry);
      if (!f.exists()) {
        s_logger.debug("Skipping non-existent classpath entry '{}'", classpathEntry);
        continue;
      }
      try {
        classpathUrls.add(f.toURI().toURL());
      } catch (MalformedURLException e) {
        throw new BuildException("Error interpreting classpath entry '" + classpathEntry + "' as URL", e);
      }
    }
    URL[] classpathUrlArray = classpathUrls.toArray(new URL[0]);
    return classpathUrlArray;
  }
  
}
