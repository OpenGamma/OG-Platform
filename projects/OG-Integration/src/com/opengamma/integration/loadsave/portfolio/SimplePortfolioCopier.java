/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio;

import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A simple portfolio copier that copies positions from readers to the specified writer.
 */
public class SimplePortfolioCopier implements PortfolioCopier {

  @Override
  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter) {
    ObjectsPair<ManageablePosition, ManageableSecurity[]> next;

    ArgumentChecker.notNull(portfolioWriter, "portfolioWriter");
    ArgumentChecker.notNull(portfolioReader, "portfolioReader");
    
    // Read in next row, checking for EOF
    while ((next = portfolioReader.readNext()) != null) { 
      
      // Is position and security data is available for the current row?
      if (next.getFirst() != null && next.getSecond() != null) {
        
        // Set current path
        portfolioWriter.setPath(portfolioReader.getCurrentPath());
        
        // Write position and security data
        for (ManageableSecurity security : next.getSecond()) {
          portfolioWriter.writeSecurity(security);
        }
        portfolioWriter.writePosition(next.getFirst());
      }
    }

    // Flush changes to portfolio master
    portfolioWriter.flush();
  }

}
