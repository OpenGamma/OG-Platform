/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.portfolio.ManageablePortfolioNode;

/**
 * Portfolio reader that reads multiple CSV files within a ZIP archive, identifies the correct parser class for each,
 * using the file name, and persists all loaded trades/entries using the specified portfolio writer.
 */
public class ZippedPortfolioReader implements PortfolioReader {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioImportCmdLineTool.class);

  private static final String CLASS_PREFIX = "com.opengamma.financial.loader.rowparsers.";
  private static final String CLASS_POSTFIX = "Parser";
  private static final String SHEET_EXTENSION = ".csv";
  private static final String CONFIG_FILE = "METADATA.INI";
  private static final String VERSION_TAG = "version";
  private static final String VERSION = "1.0";
  
  private ZipFile _zipFile;
  
  public ZippedPortfolioReader(String filename) {
   
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

    ManageablePortfolioNode node = portfolioWriter.getCurrentNode();
    
    // Iterate through the CSV file entries in the ZIP archive
    Enumeration<?> e = _zipFile.entries();
    while (e.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) e.nextElement();
      if (!entry.isDirectory() && entry.getName().substring(entry.getName().lastIndexOf('.')).equalsIgnoreCase(SHEET_EXTENSION)) {
        try {
          // Identify the appropriate parser class from the ZIP entry's file name
          String className = CLASS_PREFIX + entry.getName().substring(0, entry.getName().lastIndexOf('.')) + CLASS_POSTFIX;
          Class<?> parserClass = Class.forName(className);

          // Find the constructor
          Constructor<?> constructor = parserClass.getConstructor();
          
          // Set up a sheet reader for the current CSV file in the ZIP archive
          SheetReader sheet = new CsvSheetReader(_zipFile.getInputStream(entry));
          
          // Create a generic simple portfolio loader for the current sheet, using a dynamically loaded row parser class
          SingleSheetPortfolioReader portfolioLoader = new SimplePortfolioReader(sheet, (RowParser) constructor.newInstance(), sheet.getColumns());

          s_logger.info("Processing " + entry.getName() + " with " + className);
          
          // Add a portfolio node named after the current asset class and change to it
          ManageablePortfolioNode subNode = new ManageablePortfolioNode();
          subNode.setName(entry.getName().substring(0, entry.getName().lastIndexOf('.')));
          node.addChildNode(subNode);
          portfolioWriter.setCurrentNode(subNode);
          
          // Persist the current sheet's trades/positions using the specified portfolio writer
          portfolioLoader.writeTo(portfolioWriter);
          
          // Change back to the root portfolio node
          portfolioWriter.setCurrentNode(node);
          
          // Flush changes to portfolio master
          portfolioWriter.flush();

        } catch (Throwable ex) {
          //throw new OpenGammaRuntimeException("Could not identify an appropriate loader for ZIP entry " + entry.getName());
          s_logger.warn("Could not identify an appropriate loader for " + entry.getName() + ", skipping file.");
        }
      }
    }
  }
}
