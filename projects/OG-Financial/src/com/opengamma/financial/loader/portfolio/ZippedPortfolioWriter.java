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
    
    try {
      _zipFile = new ZipOutputStream(new FileOutputStream(filename));
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not create " + filename + " for writing");
    }

  }
  
  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
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

}
