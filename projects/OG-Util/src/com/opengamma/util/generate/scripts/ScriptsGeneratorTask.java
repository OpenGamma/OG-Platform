/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.generate.scripts;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.annotation.ClassNameAnnotationScanner;

/**
 * Scans for annotations and writes the names of classes using them to a file.
 */
public class ScriptsGeneratorTask extends Task {

  private static final Logger s_logger = LoggerFactory.getLogger(ScriptsGeneratorTask.class);

  private String _outputFile;

  private String _projectName;

  private String _scriptDir;

  private ArrayList<FileSet> _filesets = newArrayList();


  private String getOutputFile() {
    return _outputFile;
  }

  public void setOutputFile(String outputFile) {
    _outputFile = outputFile;
  }

  public String getProjectName() {
    return _projectName;
  }

  public void setProjectName(String projectName) {
    _projectName = projectName;
  }

  public String getScriptDir() {
    return _scriptDir;
  }

  public void setScriptDir(String scriptDir) {
    _scriptDir = scriptDir;
  }


  public void addFileset(FileSet fileset) {
    _filesets.add(fileset);
  }

  public ArrayList<FileSet> getFilesets() {
    return _filesets;
  }

  //-------------------------------------------------------------------------

  @Override
  public void execute() throws BuildException {
    List<String> jars = newArrayList();
    for (FileSet fileSet : getFilesets()) {
      Iterator i = fileSet.iterator();
      while (i.hasNext()) {
        Object next = i.next();        
        jars.add(next.toString());
      }
    }

    Set<String> classNames = ClassNameAnnotationScanner.scan(jars.toArray(new String[jars.size()]), Scriptable.class);

    for (String className : classNames) {
      ScriptsGenerator.generate(new File(getScriptDir()), getProjectName(), className);
    }
  }

}
