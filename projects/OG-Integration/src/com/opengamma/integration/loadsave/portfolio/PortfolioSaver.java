/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.integration.loadsave.portfolio.reader.MasterPortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.SingleSheetPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.ZippedPortfolioWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Provides portfolio saving functionality
 */
public class PortfolioSaver {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioSaver.class);
  
  public void run(String portfolioName, String fileName, String[] securityTypes, boolean persist, ToolContext toolContext) {
                
    // Set up writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        fileName, 
        securityTypes,
        persist,
        toolContext);
    
     // Set up reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        portfolioName, 
        toolContext);
    
    // Load in and write the securities, positions and trades
    //portfolioReader.writeTo(portfolioWriter);
    while (true) { 
      // Read in next row
      ObjectsPair<ManageablePosition, ManageableSecurity[]> next = portfolioReader.readNext();
      
      // Check for EOF
      if (next == null) {
        break;
      }
      
      // If position and security data is available, send it to the writer
      if (next.getFirst() != null && next.getSecond() != null) {
        
        System.out.print("Path: ");
        for (String s : portfolioReader.getCurrentPath()) {
          System.out.print("/" + s);
        }
        System.out.println();
        
        // TODO set current node
        
        for (ManageableSecurity security : next.getSecond()) {
          portfolioWriter.writeSecurity(security);
        }
        portfolioWriter.writePosition(next.getFirst());
      }
    }

    // Flush changes to portfolio master & close
    portfolioWriter.flush();
    portfolioWriter.close();
    
  }
  
  private static PortfolioWriter constructPortfolioWriter(String filename, String[] securityTypes, boolean write, ToolContext toolContext) {
    if (write) {  
      // Check that the file name was specified on the command line
      if (filename == null) {
        throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
      }
      
      s_logger.info("Write option specified, will persist to file '" + filename + "'");
 
      String extension = filename.substring(filename.lastIndexOf('.'));

      if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
        
        return new SingleSheetPortfolioWriter(filename, securityTypes, toolContext);
            
      // Multi-asset ZIP file extension
      } else if (extension.equalsIgnoreCase(".zip")) {
        // Create zipped multi-asset class loader
        return new ZippedPortfolioWriter(filename, toolContext);
      } else {
        throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS or .ZIP");
      }

    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to file");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();
    }
  }

  private static PortfolioReader constructPortfolioReader(String portfolioName, ToolContext toolContext) {
    return new MasterPortfolioReader(portfolioName, toolContext);
  }

}
