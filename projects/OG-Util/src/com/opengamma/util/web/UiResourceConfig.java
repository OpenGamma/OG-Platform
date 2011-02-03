/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.web;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class to present config XML.
 */
@XmlRootElement(name = "uiResourceConfig")
public class UiResourceConfig {

  @XmlElement(name = "bundle")
  private List<Bundle> _bundles;
  @XmlElement(name = "file")
  private List<File> _files;

  /**
   * Gets the bundles field.
   * 
   * @return the bundles
   */
  public List<Bundle> getBundles() {
    return _bundles;
  }

  /**
   * Sets the bundles field.
   * 
   * @param bundles  the bundles
   */
  public void setBundles(ArrayList<Bundle> bundles) {
    _bundles = bundles;
  }

  /**
   * Gets the files field.
   * @return the files
   */
  public List<File> getFiles() {
    return _files;
  }

  /**
   * Sets the files field.
   * @param files  the files
   */
  public void setFiles(ArrayList<File> files) {
    _files = files;
  }

  /**
   * Parses a file.
   * 
   * @param file  the file to parse
   * @return the config.
   * @throws JAXBException if an error occurs
   * @throws FileNotFoundException if an error occurs
   */
  public static UiResourceConfig parse(java.io.File file) throws JAXBException, FileNotFoundException {
    JAXBContext context = JAXBContext.newInstance(UiResourceConfig.class);
    Unmarshaller um = context.createUnmarshaller();
    UiResourceConfig resourceConfig = (UiResourceConfig) um.unmarshal(new FileReader(file));
    return resourceConfig;
  }

}
