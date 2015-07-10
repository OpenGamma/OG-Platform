/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.portfolio.writer;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.JodaBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.rowparser.RowParser;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.writer.SheetWriter;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * This class writes a portfolio that might contain multiple security types into a single sheet. The columns are
 * established from the set of row parsers that are supplied to the constructor.
 */
public class SingleSheetMultiParserPositionWriter extends SingleSheetPositionWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleSheetMultiParserPositionWriter.class);
  
  private Map<String, RowParser> _parserMap = new HashMap<String, RowParser>();
  
  // current row context
  private Map<String, String> _currentRow  = new HashMap<String, String>();
  private RowParser _currentParser;
  
  private ManageablePortfolioNode _currentNode;
  private ManageablePortfolio _portfolio;

  /** Generate one row per trade instead of one row per position */
  private boolean _includeTrades;

  public SingleSheetMultiParserPositionWriter(SheetWriter sheet, Map<String, RowParser> rowParsers) {
    super(sheet);
    
    ArgumentChecker.notNull(rowParsers, "rowParsers");
    _parserMap = rowParsers;

    // create virtual manageable portfolio
    _currentNode = new ManageablePortfolioNode("Root");
    _portfolio = new ManageablePortfolio("Portfolio", _currentNode);
    _currentNode.setPortfolioId(_portfolio.getUniqueId());

    _includeTrades = false;
  }

  public SingleSheetMultiParserPositionWriter(SheetWriter sheet,
                                              Map<String, RowParser> rowParsers,
                                              boolean includeTrades) {
    this(sheet, rowParsers);
    _includeTrades = includeTrades;
  }
    
  public SingleSheetMultiParserPositionWriter(SheetWriter sheet, String[] securityTypes) {
    this(sheet, getParsers(securityTypes));
    _includeTrades = false;
  }

  public SingleSheetMultiParserPositionWriter(SheetWriter sheet, String[] securityTypes, boolean includeTrades) {
    this(sheet, securityTypes);
    _includeTrades = includeTrades;
  }

  public SingleSheetMultiParserPositionWriter(SheetFormat sheetFormat, OutputStream outputStream,
                                              Map<String, RowParser> rowParsers) {
    this(SheetWriter.newSheetWriter(sheetFormat, outputStream, getColumns(rowParsers)), rowParsers);
    _includeTrades = false;
  }  

  public SingleSheetMultiParserPositionWriter(SheetFormat sheetFormat, OutputStream outputStream,
                                              Map<String, RowParser> rowParsers, boolean includeTrades) {
    this(sheetFormat, outputStream, rowParsers);
    _includeTrades = includeTrades;
  }

  public SingleSheetMultiParserPositionWriter(SheetFormat sheetFormat,
                                              OutputStream outputStream,
                                              String[] securityTypes) {
    this(SheetWriter.newSheetWriter(sheetFormat, outputStream, getColumns(getParsers(securityTypes))), getParsers(securityTypes));
    _includeTrades = false;
  }

  public SingleSheetMultiParserPositionWriter(SheetFormat sheetFormat,
                                              OutputStream outputStream,
                                              String[] securityTypes,
                                              boolean includeTrades) {
    this(sheetFormat, outputStream, securityTypes);
    _includeTrades = includeTrades;
  }

  public SingleSheetMultiParserPositionWriter(String filename, Map<String, RowParser> rowParsers) {
    this(SheetWriter.newSheetWriter(filename, getColumns(rowParsers)), rowParsers);
    _includeTrades = false;
  }

  public SingleSheetMultiParserPositionWriter(String filename, Map<String, RowParser> rowParsers, boolean includeTrades) {
    this(filename, rowParsers);
    _includeTrades = includeTrades;
  }

  public SingleSheetMultiParserPositionWriter(String filename, String[] securityTypes) {
    this(filename, getParsers(securityTypes));
    _includeTrades = false;
  }

  public SingleSheetMultiParserPositionWriter(String filename, String[] securityTypes, boolean includeTrades) {
    this(filename, securityTypes);
    _includeTrades = includeTrades;
  }

  @Override
  public void addAttribute(String key, String value) {
    // Not supported
  }

  private void writeSecurities(ManageableSecurity[] securities) {
        
    String className = securities[0].getClass().toString();
    className = className.substring(className.lastIndexOf('.') + 1).replace("Security", "");
    if ((_currentParser = _parserMap.get(className)) != null) { //CSIGNORE
      _currentRow.putAll(_currentParser.constructRow(securities));
    }
  }

  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> writePosition(ManageablePosition position, ManageableSecurity[] securities) {
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(securities, "securities");
    
    // Write securities
    writeSecurities(securities);
    
    // Write position
    if (_currentParser != null) {

//      List<ManageableTrade> trades = position.getTrades();
//      if (trades.size() > 1) {
//        s_logger.warn("Omitting extra trades: only one trade per position is currently supported");
//      }
//      if (trades.size() > 0) {
//        _currentRow.putAll(_currentParser.constructRow(trades.get(0)));
//      }

      if (_includeTrades) {
        // Write each trade as a separate row if the current position contains trades
        if (position.getTrades().size() > 0) {
          ManageablePosition subPosition = JodaBeanUtils.clone(position);
          for (ManageableTrade trade : position.getTrades()) {
            Map<String, String> tempRow = new HashMap<>();
            tempRow.putAll(_currentRow);
            tempRow.putAll(_currentParser.constructRow(trade));

            // Set position quantity to its trade's quantity and write position
            subPosition.setQuantity(trade.getQuantity());
            tempRow.putAll(_currentParser.constructRow(subPosition));

            // Flush out the current row with trade
            if (!tempRow.isEmpty()) {
              getSheet().writeNextRow(tempRow);
            }
          }
        } else {
          // Write position
          _currentRow.putAll(_currentParser.constructRow(position));

          // Flush out the current row (excluding trades)
          if (!_currentRow.isEmpty()) {
            getSheet().writeNextRow(_currentRow);
          }
        }
      } else {
        // Write position
        _currentRow.putAll(_currentParser.constructRow(position));

        // Export only the first trade of each position or none at all
        if (!position.getTrades().isEmpty()) {
          _currentRow.putAll(_currentParser.constructRow(position.getTrades().get(0)));
        }
        if (position.getTrades().size() > 1) {
          s_logger.warn("Omitting extra trades: only one trade per position is supported in the current mode");
        }
        if (!_currentRow.isEmpty()) {
          getSheet().writeNextRow(_currentRow);
        }
      }
    }

    // Empty the current row buffer
    _currentRow = new HashMap<String, String>();

    return ObjectsPair.of(position, securities);
  }

  private static Map<String, RowParser> getParsers(String[] securityTypes) {
    Map<String, RowParser> rowParsers = new HashMap<String, RowParser>();
    if (securityTypes != null) {
      for (String s : securityTypes) {
        JodaBeanRowParser parser = JodaBeanRowParser.newJodaBeanRowParser(s);
        if (parser != null) {
          rowParsers.put(s, parser);
        }
      }
    }
    return rowParsers;
  }

  private static String[] getColumns(Map<String, RowParser> rowParsers) {
    Set<String> columns = new HashSet<String>();
    for (RowParser rowParser : rowParsers.values()) {      
      // Combine columns from supplied row parsers
      for (String column : rowParser.getColumns()) {
        columns.add(column);
      }
    }
    return columns.toArray(new String[columns.size()]);
  }

  @Override
  public void setPath(String[] newPath) {
    // Nothing to do here (a specialised subclass might add a 'path' column to store the current path for each row)
  }

  @Override
  public String[] getCurrentPath() {
    return new String[] {};
  }

}
