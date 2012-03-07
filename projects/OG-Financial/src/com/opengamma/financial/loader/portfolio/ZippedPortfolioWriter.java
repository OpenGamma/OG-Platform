/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.portfolio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.loader.rowparser.RowParser;
import com.opengamma.financial.loader.sheet.CsvSheetWriter;
import com.opengamma.financial.loader.sheet.SheetWriter;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * This class writes positions/securities to a zip file, using the zip file's directory structure to represent the portfolio
 * node structure.
 */
public class ZippedPortfolioWriter implements PortfolioWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(ZippedPortfolioWriter.class);

  private static final char DIRECTORY_SEPARATOR = '/';

  private ZipOutputStream _zipFile;
  private ToolContext _toolContext;
  private ManageablePortfolio _portfolio;

  // Current node and row context
  private ManageablePortfolioNode _currentNode;
  private Map<String, SingleSheetPortfolioWriter> _writerMap = new HashMap<String, SingleSheetPortfolioWriter>();
  private Map<String, ByteArrayOutputStream> _bufferMap = new HashMap<String, ByteArrayOutputStream>();
  private SingleSheetPortfolioWriter _currentWriter;

  public ZippedPortfolioWriter(String filename, ToolContext toolContext) {

    _toolContext = toolContext;

    // Confirm file doesn't already exist
    File file = new File(filename);
    if (file.exists()) {
      throw new OpenGammaRuntimeException("File " + filename + " already exists");
    }
    
    // Create zip file
    try {
      _zipFile = new ZipOutputStream(new FileOutputStream(filename));
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not create zip archive " + filename + " for writing: " + ex.getMessage());
    }
    
    // Write version file
    writeVersion();

    // Set up virtual portfolio and root node
    ManageablePortfolioNode root = new ManageablePortfolioNode("Root");
    _portfolio = new ManageablePortfolio("Virtual Portfolio", root);
    _currentNode = _portfolio.getRootNode();
  }

  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {

    identifyOrCreatePortfolioWriter(security);

    if (_currentWriter != null) {
      security = _currentWriter.writeSecurity(security);
    } else {
      s_logger.warn("Could not identify a suitable parser for security: " + security.getName());
    }

    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {

    if (_currentWriter != null) {
      position = _currentWriter.writePosition(position);
    } else {
      s_logger.warn("Could not identify a suitable parser for position: " + position.getName());
    }

    return position;
  }

  @Override
  public ManageablePortfolio getPortfolio() {
    return _portfolio;
  }

  @Override
  public ManageablePortfolioNode getCurrentNode() {
    return _currentNode;
  }

  @Override
  public ManageablePortfolioNode setCurrentNode(ManageablePortfolioNode node) {

    // First, write the current set of CSVs to the zip file and clear the map of writers
    flushCurrentBuffers();

    // Change to the new path in the zip file (might need to create)
    String path = getPath(node);
    if (!path.equals("")) {
      ZipEntry entry = new ZipEntry(path);
      try {
        _zipFile.putNextEntry(entry);
        _zipFile.closeEntry();
      } catch (IOException ex) {
        // assume the directory already exists
        //throw new OpenGammaRuntimeException("Could not create folder " + entry.getName() + " in zip file: " + ex.getMessage());
      }
    }

    _currentNode = node;
    return node;
  }

  @Override
  public void flush() {
    try {
      _zipFile.flush();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not flush data into zip archive");
    }
  }

  @Override
  public void close() {

    flushCurrentBuffers();
    flush();
    try {
      _zipFile.close();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not close zip file");
    }
  }

  private String getPath(ManageablePortfolioNode node) {
    Stack<ManageablePortfolioNode> stack = findNodeStackByNode(_portfolio.getRootNode(), node);
    String path = "";

    while (!stack.isEmpty()) {
      path += stack.pop().getName().replace(DIRECTORY_SEPARATOR + "", "<slash>") + DIRECTORY_SEPARATOR;
    }

    return path;
  }

  private Stack<ManageablePortfolioNode> findNodeStackByNode(final ManageablePortfolioNode start, final ManageablePortfolioNode treasure) {

    // This node IS the treasure
    if (start == treasure) {
      Stack<ManageablePortfolioNode> stack = new Stack<ManageablePortfolioNode>();
      stack.push(start);
      return stack;
    }

    // Look for treasure in the child nodes
    for (ManageablePortfolioNode childNode : start.getChildNodes()) {
      Stack<ManageablePortfolioNode> stack = findNodeStackByNode(childNode, treasure);
      if (stack != null) {
        stack.push(start);
        return stack;
      }
    }

    // None of the child nodes contain treasure
    return null;
  }

  private void flushCurrentBuffers() {

    String path = getPath(_currentNode);
    
    s_logger.info("Flushing CSV buffers for ZIP directory " + path);

    for (Map.Entry<String, ByteArrayOutputStream> entry : _bufferMap.entrySet()) {

      _writerMap.get(entry.getKey()).flush();
      _writerMap.get(entry.getKey()).close();

      ZipEntry zipEntry = new ZipEntry(path + entry.getKey() + ".csv");
      s_logger.info("Writing " + zipEntry.getName());
      try {
        _zipFile.putNextEntry(zipEntry);
        entry.getValue().writeTo(_zipFile);
        _zipFile.closeEntry();
        _zipFile.flush();
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("Could not write file entry " + zipEntry.getName() + " to zip archive: " + ex.getMessage());
      }
    }

    _bufferMap = new HashMap<String, ByteArrayOutputStream>();
    _writerMap = new HashMap<String, SingleSheetPortfolioWriter>();
  }

  private PortfolioWriter identifyOrCreatePortfolioWriter(ManageableSecurity security) {
    // Identify the correct portfolio writer for this security
    String className = security.getClass().toString();
    className = className.substring(className.lastIndexOf('.') + 1).replace("Security", "");
    _currentWriter = _writerMap.get(className);
    RowParser parser;

    // create writer/output buffer map entry if not there for this security type
    if (_currentWriter == null) {

      s_logger.info("Creating a new row parser for " + className + " securities");

      parser = RowParser.newRowParser(className, _toolContext);
      if (parser == null) {
        return null;
      }
      Map<String, RowParser> parserMap = new HashMap<String, RowParser>();
      parserMap.put(className, parser);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      SheetWriter sheet = new CsvSheetWriter(out, parser.getColumns());

      _currentWriter = new SingleSheetPortfolioWriter(sheet, parserMap);

      _writerMap.put(className, _currentWriter);
      _bufferMap.put(className, out);
    }

    return _currentWriter;
  }

  private void writeVersion() {
    try {
      _zipFile.putNextEntry(new ZipEntry("METADATA.INI"));
      _zipFile.write("Version = 1.0".getBytes());
      _zipFile.closeEntry();
      _zipFile.flush();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not write METADATA.INI to zip archive");
    }
  }
  
}
