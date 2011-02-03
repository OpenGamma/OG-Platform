/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Concat;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileList.FileName;

/**
 * Configures UI Resources.
 */
public class UiResourceConfigTask extends Task {

  private static final String DEFAULT_VERSION = "1.0";

  /**
   * Version number.
   */
  private String _version = DEFAULT_VERSION;
  /**
   * UiResourceConfig XML file.
   */
  private File _xmlFile;
  /**
   * Directory for output files.
   */
  private File _dir;

  private Map<String, Bundle> _bundlesMap = new HashMap<String, Bundle>();
  private Map<com.opengamma.util.web.File, List<String>> _filesMap = new HashMap<com.opengamma.util.web.File, List<String>>();

  /**
   * Gets the xml file field.
   * 
   * @return the file
   */
  public File getXmlFile() {
    return _xmlFile;
  }

  /**
   * Sets the file field.
   * 
   * @param file  the file
   */
  public void setXmlFile(File file) {
    _xmlFile = file;
  }

  /**
   * Gets the dir field.
   * 
   * @return the dir
   */
  public File getDir() {
    return _dir;
  }

  /**
   * Sets the dir field.
   * 
   * @param dir  the dir
   */
  public void setDir(File dir) {
    _dir = dir;
  }

  /**
   * Gets the version field.
   * 
   * @return the version
   */
  public String getVersion() {
    return _version;
  }

  /**
   * Sets the version field.
   * 
   * @param version  the version
   */
  public void setVersion(String version) {
    _version = version;
  }

  //-------------------------------------------------------------------------
  @Override
  public void execute() throws BuildException {
    if (_xmlFile == null || !_xmlFile.exists()) {
      throw new BuildException("Attribute file is required");
    }
    UiResourceConfig resourceConfig = null;
    try {
      resourceConfig = UiResourceConfig.parse(_xmlFile);
    } catch (Exception ex) {
      throw new BuildException("Unable to parse " + _xmlFile.getAbsolutePath(), ex);
    }
    createFileToBundleMapping(resourceConfig);
    concatFiles();
  }

  private void concatFiles() {
    for (Entry<com.opengamma.util.web.File, List<String>> entry : _filesMap.entrySet()) {
      com.opengamma.util.web.File file = entry.getKey();
      List<String> fileList = new ArrayList<String>();
      for (String bundleName : entry.getValue()) {
        Bundle bundle = _bundlesMap.get(bundleName);
        String id = bundle.getId();
        assert id.equals(bundleName);
        fileList.addAll(bundle.getFragment());
      }
      
      Concat concatTask = new Concat();
      concatTask.setDestfile(createDestinationFile(file));
    
      FileList antFileList = new FileList();
      antFileList.setDir(_dir);
      
      for (String fileName : fileList) {
        FileName antFileName = new FileList.FileName();
        antFileName.setName(fileName);
        antFileList.addConfiguredFile(antFileName);
      }
      concatTask.addFilelist(antFileList);
      concatTask.execute();
    }
  }

  private File createDestinationFile(com.opengamma.util.web.File file) {
    String buildNumber = getProject().getProperty("build.number");
    StringBuilder buf = new StringBuilder();
    buf.append(_dir.getAbsolutePath());
    buf.append(System.getProperty("file.separator"));
    buf.append(file.getId());
    buf.append("-");
    buf.append(getVersion());
    if (buildNumber != null) {
      buf.append("-");
      buf.append(buildNumber);
    }
    buf.append(".");
    buf.append(file.getSuffix());
    File destFile = new File(buf.toString());
    log("destFile:" + buf.toString());
    return destFile;
  }

  private void createFileToBundleMapping(UiResourceConfig resourceConfig) {
    for (Bundle bundle : resourceConfig.getBundles()) {
      String id = bundle.getId();
      if (_bundlesMap.get(id) != null) {
        //duplicate bundle id not allowed
        throw new BuildException("duplicate bundle id " + id);
      } else {
        _bundlesMap.put(id, bundle);
      }
    }
    for (com.opengamma.util.web.File file : resourceConfig.getFiles()) {
      if (_filesMap.get(file) != null) {
        //duplicate file id not allowed
        throw new BuildException("duplicate file id " + file.getId());
      } else {
        _filesMap.put(file, file.getBundle());
      }
    }
  }

}
