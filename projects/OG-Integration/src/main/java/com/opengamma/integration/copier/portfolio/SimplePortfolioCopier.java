/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A simple portfolio copier that copies positions from readers to the specified writer.
 */
public class SimplePortfolioCopier implements PortfolioCopier {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioCopier.class);

  private boolean _flatten;

  public SimplePortfolioCopier() {
    _flatten = false;
  }

  public SimplePortfolioCopier(boolean flatten) {
    _flatten = flatten;
  }

  @Override
  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter) {
    copy(portfolioReader, portfolioWriter, null);
  }

  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter, PortfolioCopierVisitor visitor) {

    ArgumentChecker.notNull(portfolioWriter, "portfolioWriter");
    ArgumentChecker.notNull(portfolioReader, "portfolioReader");
    
    ObjectsPair<ManageablePosition, ManageableSecurity[]> next;

    while (true) {

      // Read in next row, checking for errors and EOF
      try {
        next = portfolioReader.readNext();
      } catch (Exception e) {
        // skip to next row on uncaught exception while parsing row
        s_logger.error("Unable to parse row: " + e.getMessage());
        continue;
      }
      if (next == null) {
        // stop loading on EOF
        break;
      }

      // Is position and security data is available for the current row?
      if (next.getFirst() != null && next.getSecond() != null) {
        
        // Set current path
        String[] path = _flatten ? new String[0] : portfolioReader.getCurrentPath();
        portfolioWriter.setPath(path);
        
        // Write position and security data
        ObjectsPair<ManageablePosition, ManageableSecurity[]> written = 
            portfolioWriter.writePosition(next.getFirst(), next.getSecond());
        
        if (visitor != null && written != null) {
          visitor.info(StringUtils.arrayToDelimitedString(path, "/"), written.getFirst(), written.getSecond());
        }
      } else {
        if (visitor != null) {
          if (next.getFirst() == null) {
            visitor.error("Could not load position");
          }
          if (next.getSecond() == null) {
            visitor.error("Could not load security(ies)");
          }
        }
      }

    }

  }
}
