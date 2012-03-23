/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.loadsave.portfolio.rowparser.RowParser;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.loadsave.sheet.reader.CsvSheetReader;
import com.opengamma.integration.loadsave.sheet.reader.SheetReader;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Portfolio reader that reads multiple CSV files within a ZIP archive, identifies the correct parser class for each,
 * using the file name, and persists all loaded trades/entries using the specified portfolio writer. Folder structure
 * in the ZIP archive is replicated in the portfolio node structure.
 */
public class ZippedPortfolioReader implements PortfolioReader {

  private static final Logger s_logger = LoggerFactory.getLogger(ZippedPortfolioReader.class);

  private static final String SHEET_EXTENSION = ".csv";

  private ToolContext _toolContext;
  private ZipFile _zipFile;
  private Map<String, Integer> _versionMap = new HashMap<String, Integer>();

  private Enumeration<ZipEntry> _zipEntries;
  
  public ZippedPortfolioReader(String filename, ToolContext toolContext) {
    _toolContext = toolContext;
    
    try {
      _zipFile = new ZipFile(filename);
      _zipEntries = (Enumeration<ZipEntry>) _zipFile.entries();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not open " + filename);
    }

    // Retrieve security hashes listed in config file
    readMetaData("METADATA.INI");
    
    s_logger.info("Using ZIP archive " + filename);
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

          RowParser parser = RowParser.newRowParser(secType, _toolContext);
          if (parser == null) {
            s_logger.error("Could not build a row parser for security type '" + secType + "'");
            continue; 
          }
          if (_versionMap.get(secType) == null) {
            s_logger.error("Versioning hash for security type '" + secType + "' could not be found");
            continue;
          }
          if (parser.getSecurityHashCode() != _versionMap.get(secType)) {
            s_logger.error("The parser version for the '" + secType + "' security (hash " + 
                Integer.toHexString(parser.getSecurityHashCode()) + 
                ") does not match the data stored in the archive (hash " + 
                Integer.toHexString(_versionMap.get(secType)) + ")");
            continue;
          }
          
          // Create a generic simple portfolio loader for the current sheet, using a dynamically loaded row parser class
          SingleSheetPortfolioReader portfolioReader = new SingleSheetSimplePortfolioReader(sheet, sheet.getColumns(), parser);

          s_logger.info("Processing rows in archive entry " + entry.getName() + " as " + secType);

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
          s_logger.warn("Could not import from " + entry.getName() + ", skipping file (exception is " + ex + ")");
        }
      }
    }
  }

  // TODO
  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {
    throw new UnsupportedOperationException();
//    if (!_zipEntries.hasMoreElements()) {
//      return null;
//    }
//    
//    ZipEntry entry = _zipEntries.nextElement();
//    
//    if (!entry.isDirectory() && entry.getName().substring(entry.getName().lastIndexOf('.')).equalsIgnoreCase(SHEET_EXTENSION)) {
//      try {
//        // Extract full path
//        String[] path = entry.getName().split("/");
//
//        // Extract security name
//        String secType = path[path.length - 1].substring(0, path[path.length - 1].lastIndexOf('.'));
//
//        // Set up a sheet reader for the current CSV file in the ZIP archive
//        SheetReader sheet = new CsvSheetReader(_zipFile.getInputStream(entry));
//
//        RowParser parser = RowParser.newRowParser(secType, _toolContext);
//        if (parser == null) {
//          s_logger.error("Could not build a row parser for security type '" + secType + "'");
//          return null; 
//        }
//        if (_versionMap.get(secType) == null) {
//          s_logger.error("Versioning hash for security type '" + secType + "' could not be found");
//          return null;
//        }
//        if (parser.getSecurityHashCode() != _versionMap.get(secType)) {
//          s_logger.error("The parser version for the '" + secType + "' security (hash " + 
//              Integer.toHexString(parser.getSecurityHashCode()) + 
//              ") does not match the data stored in the archive (hash " + 
//              Integer.toHexString(_versionMap.get(secType)) + ")");
//          return null;
//        }
//        
//        // Create a generic simple portfolio loader for the current sheet, using a dynamically loaded row parser class
//        SingleSheetPortfolioReader portfolioReader = new SingleSheetSimplePortfolioReader(sheet, sheet.getColumns(), parser);
//
//        s_logger.info("Processing rows in archive entry " + entry.getName() + " as " + secType);
//
//      } catch (Throwable ex) {
//        s_logger.warn("Could not import from " + entry.getName() + ", skipping file (exception is " + ex + ")");
//      }
//    }
//
//    return null;
  }
 
  
  
  private void readMetaData(String filename) {

    InputStream cfgInputStream;
    ZipEntry cfgEntry = _zipFile.getEntry(filename);
    if (cfgEntry != null) {
      try {
        cfgInputStream = _zipFile.getInputStream(cfgEntry);
        BufferedReader cfgReader = new BufferedReader(new InputStreamReader(cfgInputStream));
        
        String input;
        while ((input = cfgReader.readLine()) != null && !input.equals("[securityHashes]")); // CSIGNORE
        
        while ((input = cfgReader.readLine()) != null) {
          String[] line = input.split("=", 2);
          if (line.length == 2) {
            try {
              _versionMap.put(line[0].trim(), (int) Long.parseLong(line[1].trim(), 16));
            } catch (NumberFormatException e) {
              continue;
            }
          } else if (input.contains("[]")) {
            break;
          } else {
            continue;
          }
        }
        
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("Could not open METADATA.INI");
      }
    } else {
      throw new OpenGammaRuntimeException("Could not find METADATA.INI");
    }
  }

  // TODO
  @Override
  public ManageablePortfolioNode getCurrentNode() {
    throw new UnsupportedOperationException();
  }
 
}
