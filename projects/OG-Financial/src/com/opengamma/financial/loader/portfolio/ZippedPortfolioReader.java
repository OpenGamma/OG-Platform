/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.portfolio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.loader.sheet.CsvSheetReader;
import com.opengamma.financial.loader.sheet.SheetReader;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.ManageablePortfolioNode;

/**
 * Portfolio reader that reads multiple CSV files within a ZIP archive, identifies the correct parser class for each,
 * using the file name, and persists all loaded trades/entries using the specified portfolio writer. Folder structure
 * in the ZIP archive is replicated in the portfolio node structure.
 */
public class ZippedPortfolioReader implements PortfolioReader {

  private static final Logger s_logger = LoggerFactory.getLogger(ZippedPortfolioReader.class);

  private static final String SHEET_EXTENSION = ".csv";
  private static final String CONFIG_FILE = "METADATA.INI";
  private static final String VERSION_TAG = "version";
  private static final String VERSION = "1.0";

  private ZipFile _zipFile;
  private ToolContext _toolContext;

  public ZippedPortfolioReader(String filename, ToolContext toolContext) {
    _toolContext = toolContext;
    
    try {
      _zipFile = new ZipFile(filename);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not open " + filename);
    }

    // Check archive version listed in config file
    InputStream cfgInputStream;
    ZipEntry cfgEntry = _zipFile.getEntry(CONFIG_FILE);
    if (cfgEntry != null) {
      try {
        cfgInputStream = _zipFile.getInputStream(cfgEntry);
        BufferedReader cfgReader = new BufferedReader(new InputStreamReader(cfgInputStream));
        
        String input;
        while ((input = cfgReader.readLine()) != null) {
          String[] line = input.split("=", 2);
          if (line[0].trim().equalsIgnoreCase(VERSION_TAG) && line[1].trim().equalsIgnoreCase(VERSION)) {
            
            s_logger.info("Using ZIP archive " + filename);
            return;
          }
        }
        throw new OpenGammaRuntimeException("Archive " + filename + " should be at version " + VERSION);
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("Could not open configuration file " + CONFIG_FILE + " in ZIP archive" + filename);
      }
    } else {
      throw new OpenGammaRuntimeException("Could not find configuration file " + CONFIG_FILE + " in ZIP archive" + filename);
    }
    
  }
  
  @Override
  public void writeTo(PortfolioWriter portfolioWriter) {

    ManageablePortfolioNode rootNode = portfolioWriter.getCurrentNode();
    
    // Iterate through the CSV file entries in the ZIP archive
    Enumeration<?> e = _zipFile.entries();
    while (e.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) e.nextElement();
      if (!entry.isDirectory() && entry.getName().substring(entry.getName().lastIndexOf('.')).equalsIgnoreCase(SHEET_EXTENSION)) {
        try {
          // Extract full path
          String[] path = entry.getName().split("/");

          // Extract security name
          String secType = path[path.length - 1].substring(0, path[path.length - 1].lastIndexOf('.'));

          // Set up a sheet reader for the current CSV file in the ZIP archive
          SheetReader sheet = new CsvSheetReader(_zipFile.getInputStream(entry));

          // Create a generic simple portfolio loader for the current sheet, using a dynamically loaded row parser class
          SingleSheetPortfolioReader portfolioReader = new SingleSheetSimplePortfolioReader(sheet, sheet.getColumns(), secType, _toolContext);

          s_logger.info("Processing " + entry.getName() + " as " + secType);

          // Replicate the zip entry's path in the portfolio node tree:
          // Start at root and traverse existing portfolio nodes that match,
          // Create the rest of the path with new portfolio nodes
          ManageablePortfolioNode currentNode = rootNode;
          for (String p : Arrays.copyOf(path, path.length - 1)) {
            ManageablePortfolioNode childNode = null;
            for (ManageablePortfolioNode n : currentNode.getChildNodes()) {
              if (n.getName().equals(p)) {
                childNode = n;
                break;
              }
            }
            if (childNode == null) {
              childNode = new ManageablePortfolioNode();
              childNode.setName(p);
              currentNode.addChildNode(childNode);
            }
            currentNode = childNode;
          }
          
          portfolioWriter.setCurrentNode(currentNode);
          
          // Persist the current sheet's trades/positions using the specified portfolio writer
          portfolioReader.writeTo(portfolioWriter);
          
          // Change back to the root portfolio node
          portfolioWriter.setCurrentNode(rootNode);
          
          // Flush changes to portfolio master
          portfolioWriter.flush();

        } catch (Throwable ex) {
          //throw new OpenGammaRuntimeException("Could not identify an appropriate loader for ZIP entry " + entry.getName());
          s_logger.warn("Could not import from " + entry.getName() + ", skipping file (exception is " + ex + ")");
        }
      }
    }
  }
  
 
}
