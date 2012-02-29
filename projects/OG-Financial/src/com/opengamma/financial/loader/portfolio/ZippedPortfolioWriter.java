/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.portfolio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.tools.zip.ZipOutputStream;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.loader.rowparser.RowParser;
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

  private ZipOutputStream _zipFile;
  private SheetWriter _sheet;

  private ManageablePortfolioNode _currentNode;
  private ManageablePortfolio _portfolio;

  // Current portfolio node (directory) context
  private Map<String, SingleSheetPortfolioWriter> _writerMap = new HashMap<String, SingleSheetPortfolioWriter>();
  
  // current row context
  private Map<String, String> _currentRow  = new HashMap<String, String>();
  private RowParser _currentParser;
  
  public ZippedPortfolioWriter(String filename) {
    
    // Create zip file
    try {
      _zipFile = new ZipOutputStream(new FileOutputStream(filename));
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not create zip archive " + filename + " for writing: " + ex.getMessage());
    }

  }
  
  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    // Identify the correct row parser
    String className = security.getClass().toString();
    className = className.substring(className.lastIndexOf('.') + 1).replace("Security", "");
    PortfolioWriter writer = _writerMap.get(className);
    
//    if ((_currentParser = _writerMap.get(className)) != null) { //CSIGNORE
//      _currentRow.putAll(_currentParser.constructRow(security)); // this should get relevant data from any underlyings too
//    }

    // create writer map entry if not there for this security type
    
    return null;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {
    return null;
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
    
    // Change to the new path in the zip file (might need to create)
    
    _currentNode = node;
    return node;
  }

  @Override
  public void flush() {
    _sheet.flush();
    try {
      _zipFile.flush();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not flush data into zip file");
    }
  }

  @Override
  public void close() {
    
    // write current set of CSVs to the zip file
    
    flush();
    try {
      _zipFile.close();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not close zip file");
    }
  }
}
