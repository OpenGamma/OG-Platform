/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fudgemsg.AnnotationReflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.SingletonFactoryBean;

/**
 * A Spring factory bean for obtaining a list of classes with a particular annotation.
 */
public class AnnotationScanningStringListFactoryBean extends SingletonFactoryBean<List<String>> {

  private static final Logger s_logger = LoggerFactory.getLogger(AnnotationScanningStringListFactoryBean.class);
  
  private String _cacheFile;
  private String _forceScanSystemProperty;
  private String _annotationClassName;

  public String getCacheFile() {
    return _cacheFile;
  }
  
  public void setCacheFile(String cacheFile) {
    _cacheFile = cacheFile;
  }
  
  public String getForceScanSystemProperty() {
    return _forceScanSystemProperty;
  }
  
  public void setForceScanSystemProperty(String forceScanSystemProperty) {
    _forceScanSystemProperty = forceScanSystemProperty;
  }
  
  public String getAnnotationClassName() {
    return _annotationClassName;
  }
  
  public void setAnnotationClassName(String annotationClassName) {
    _annotationClassName = annotationClassName;
  }
  
  @Override
  protected List<String> createObject() {
    try {
      boolean forceScan = shouldForceScan();
      if (!forceScan && getCacheFile() != null) {
        ClassPathResource cacheFileResource = new ClassPathResource(getCacheFile());
        if (cacheFileResource.exists()) {
          File cacheFile = cacheFileResource.getFile();
          s_logger.debug("Getting classes containing annotation {} from cache {}", getAnnotationClassName(), cacheFile.getAbsoluteFile());
          return getFromCache(cacheFile);
        }
      }
      s_logger.debug("Scanning for classes containing annotation {}", getAnnotationClassName());
      return new ArrayList<>(getByScanning(getAnnotationClassName()));
    } catch (Exception e) {
      s_logger.warn("Unable to retrieve classes containing annotation " + getAnnotationClassName(), e);
      return Collections.emptyList();
    }
  }
  
  private boolean shouldForceScan() {
    if (getForceScanSystemProperty() == null) {
      s_logger.debug("Force scan system property not specified");
      return false;
    }
    String forceScanPropertyValue = System.getProperty(getForceScanSystemProperty());
    s_logger.debug("Force scan system property set to '{}'", forceScanPropertyValue);
    return forceScanPropertyValue != null;
  }

  private List<String> getFromCache(File cacheFile) {
    List<String> stringList = new ArrayList<String>();
    try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
      String nextLine;
      while ((nextLine = reader.readLine()) != null) {
        stringList.add(nextLine);
      }
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("File not found: " + cacheFile.getAbsoluteFile(), e);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error while reading file: " + cacheFile.getAbsoluteFile(), e);
    }
    return stringList;
  }

  private Set<String> getByScanning(String annotationClassName) {
    Set<String> annotated = AnnotationReflector.getDefaultReflector().getReflector().getStore().getTypesAnnotatedWith(annotationClassName);
    s_logger.debug("Found {} classes containing annotation: {}", annotated.size(), annotated);
    return annotated;
  }

}
