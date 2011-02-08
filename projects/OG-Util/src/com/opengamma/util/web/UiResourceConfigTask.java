/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Concat;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileList.FileName;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Configures UI Resources.
 */
public class UiResourceConfigTask extends Task {

  private static final String DEFAULT_VERSION = "1.0";
  private static final String DEFAULT_CHARSET = "ISO-8859-1";

  /**
   * Version number.
   */
  private String _version;
  /**
   * UiResourceConfig XML file.
   */
  private File _xmlFile;
  /**
   * Directory for output files.
   */
  private File _dir;

  private String _charset = DEFAULT_CHARSET;
  private int _lineBreakPosition = -1;
  private boolean _munge;
  private boolean _warn = true;
  private boolean _preserveAllSemiColons = true;
  private boolean _optimize = true;
  private boolean _delete = true;
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
    validate();
    UiResourceConfig resourceConfig = null;
    try {
      resourceConfig = UiResourceConfig.parse(_xmlFile);
    } catch (Exception ex) {
      throw new BuildException("Unable to parse " + _xmlFile.getAbsolutePath(), ex);
    }
    createFileToBundleMapping(resourceConfig);
    concatAndCompressFiles();
  }

  private void validate() {
    if (_xmlFile == null || !_xmlFile.exists()) {
      throw new BuildException("Attribute xmlFile is required");
    }
    if (_dir == null || !_dir.isDirectory()) {
      throw new BuildException(getDir() + " is not a valid directory");
    }
  }

  private Reader openFile(File file) throws BuildException {
    Reader in;
    try {
      in = new InputStreamReader(new FileInputStream(file), getCharset());
    } catch (UnsupportedCharsetException uche) {
      throw new BuildException("Unsupported charset name: " + getCharset(), uche);
    } catch (IOException ioe) {
      throw new BuildException("I/O Error when reading input file", ioe);
    }
    return in;
  }

  private void concatAndCompressFiles() {
    List<File> concatFileList = new ArrayList<File>();
    for (Entry<com.opengamma.util.web.File, List<String>> entry : _filesMap.entrySet()) {
      final com.opengamma.util.web.File file = entry.getKey();
      final List<String> bundleNames = entry.getValue();
      File concatFile = concatenate(file, bundleNames);
      try {
        Reader in = openFile(concatFile);
        File compressedFile = createCompressedFile(file);
        compressedFile.getParentFile().mkdirs();
        Writer out = new OutputStreamWriter(new FileOutputStream(compressedFile), getCharset());
        if (isJavaScript(file)) {
          JavaScriptCompressor jsCompressor = createJavaScriptCompressor(in);
          jsCompressor.compress(out, getLineBreakPosition(), isMunge(), isWarn(), isPreserveAllSemiColons(), !isOptimize());
        } else if (isCssFile(file)) {
          CssCompressor compressor = new CssCompressor(in);
          compressor.compress(out, getLineBreakPosition());
        }
        in.close();
        in = null;
        out.close();
        out = null;
      } catch (Exception ex) {
        throw new BuildException("I/O Error when compressing file", ex);
      } 
      concatFileList.add(concatFile);
    }
    createConcatFilesProperty(concatFileList);
    deleteConcatFiles(concatFileList);
  }

  private File concatenate(final com.opengamma.util.web.File file, final List<String> bundleNames) {
    List<String> fileList = getAllFiles(bundleNames);
    Concat concatTask = new Concat();
    File concatFile = createConcatDestinationFile(file);
    concatTask.setDestfile(concatFile);
  
    FileList antFileList = createAntFileList(fileList);
    concatTask.addFilelist(antFileList);
    concatTask.execute();
    return concatFile;
  }

  private void deleteConcatFiles(List<File> concatFileList) {
    if (isDelete()) {
      Delete deleteTask = new Delete();
      for (File file : concatFileList) {
        deleteTask.setFile(file);
        deleteTask.execute();
      }
    }
  }

  private void createConcatFilesProperty(List<File> fileList) {
    StringBuilder buf = new StringBuilder();
    int counter = 0;
    for (File file : fileList) {
      buf.append(file.getAbsolutePath());
      if (++counter != fileList.size()) {
        buf.append(",");
      }
    }
    getProject().setNewProperty("uiResource.concats", buf.toString());
  }

  private boolean isCssFile(com.opengamma.util.web.File file) {
    return file.getSuffix().equals(ResourceType.CSS.getSuffix());
  }

  private JavaScriptCompressor createJavaScriptCompressor(Reader in) throws IOException {

    JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

      private String getMessage(String source, String message, int line, int lineOffset) {
        String logMessage;
        if (line < 0) {
          logMessage = (source != null) ? source + ":" : "" + message;
        } else {
          logMessage = (source != null) ? source + ":" : "" + line + ":" + lineOffset + ":" + message;
        }
        return logMessage;
      }

      public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        log(getMessage(sourceName, message, line, lineOffset), Project.MSG_WARN);
      }

      public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        log(getMessage(sourceName, message, line, lineOffset), Project.MSG_ERR);

      }

      public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        log(getMessage(sourceName, message, line, lineOffset), Project.MSG_ERR);
        return new EvaluatorException(message);
      }
    });
    return compressor;
  }

  private boolean isJavaScript(com.opengamma.util.web.File file) {
    return file.getSuffix().equals(ResourceType.JS.getSuffix());
  }

  private FileList createAntFileList(List<String> fileList) {
    FileList antFileList = new FileList();
    antFileList.setDir(_dir);
    for (String fileName : fileList) {
      FileName antFileName = new FileList.FileName();
      antFileName.setName(fileName);
      antFileList.addConfiguredFile(antFileName);
    }
    return antFileList;
  }

  private List<String> getAllFiles(List<String> bundleNames) {
    List<String> fileList = new ArrayList<String>();
    for (String bundleName : bundleNames) {
      Bundle bundle = _bundlesMap.get(bundleName);
      if (bundle != null) {
        String id = bundle.getId();
        assert id.equals(bundleName);
        fileList.addAll(bundle.getFragment());
      }
    }
    return fileList;
  }

  private File createCompressedFile(com.opengamma.util.web.File file) {
    return createDestinationFile(file, null);
  }

  private File createDestinationFile(com.opengamma.util.web.File file, String prefix) {
    StringBuilder buf = new StringBuilder();
    buf.append(_dir.getAbsolutePath());
    buf.append(System.getProperty("file.separator"));
    buf.append(file.getId());
    if (prefix != null) {
      buf.append("-");
      buf.append(prefix);
    } 
    if (getVersion() != null) {
      buf.append("-");
      buf.append(getVersion());
    }
    String buildTime = getProject().getProperty("buildTime");
    if (buildTime != null) {
      buf.append("-");
      buf.append(buildTime);
    }
    buf.append(".");
    buf.append(file.getSuffix());
    File destFile = new File(buf.toString());
    return destFile;
  }

  private File createConcatDestinationFile(com.opengamma.util.web.File file) {
    return createDestinationFile(file, "C");
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

  /**
   * Gets the charset field.
   * @return the charset
   */
  public String getCharset() {
    return _charset;
  }

  /**
   * Sets the charset field.
   * @param charset  the charset
   */
  public void setCharset(String charset) {
    _charset = charset;
  }

  /**
   * Gets the lineBreakPosition field.
   * @return the lineBreakPosition
   */
  public int getLineBreakPosition() {
    return _lineBreakPosition;
  }

  /**
   * Sets the lineBreakPosition field.
   * @param lineBreakPosition  the lineBreakPosition
   */
  public void setLineBreakPosition(int lineBreakPosition) {
    _lineBreakPosition = lineBreakPosition;
  }

  /**
   * Gets the munge field.
   * @return the munge
   */
  public boolean isMunge() {
    return _munge;
  }

  /**
   * Sets the munge field.
   * @param munge  the munge
   */
  public void setMunge(boolean munge) {
    _munge = munge;
  }

  /**
   * Gets the warn field.
   * @return the warn
   */
  public boolean isWarn() {
    return _warn;
  }

  /**
   * Sets the warn field.
   * @param warn  the warn
   */
  public void setWarn(boolean warn) {
    _warn = warn;
  }

  /**
   * Gets the preserveAllSemiColons field.
   * @return the preserveAllSemiColons
   */
  public boolean isPreserveAllSemiColons() {
    return _preserveAllSemiColons;
  }

  /**
   * Sets the preserveAllSemiColons field.
   * @param preserveAllSemiColons  the preserveAllSemiColons
   */
  public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
    _preserveAllSemiColons = preserveAllSemiColons;
  }

  /**
   * Gets the optimize field.
   * @return the optimize
   */
  public boolean isOptimize() {
    return _optimize;
  }

  /**
   * Sets the optimize field.
   * @param optimize  the optimize
   */
  public void setOptimize(boolean optimize) {
    _optimize = optimize;
  }

  /**
   * Gets the delete field.
   * @return the delete
   */
  public boolean isDelete() {
    return _delete;
  }

  /**
   * Sets the delete field.
   * @param delete  the delete
   */
  public void setDelete(boolean delete) {
    _delete = delete;
  }

}
