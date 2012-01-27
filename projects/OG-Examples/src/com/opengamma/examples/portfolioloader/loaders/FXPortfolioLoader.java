/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader.loaders;

import java.math.BigDecimal;
import java.util.Map;

import com.opengamma.examples.portfolioloader.SingleSheetPortfolioLoader;
import com.opengamma.examples.portfolioloader.MasterPortfolioWriter;
import com.opengamma.examples.portfolioloader.RowParser;
import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.FXParser;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

public class FXPortfolioLoader extends SimplePortfolioLoader {

  private static final String[] COLUMNS = { 
    FXParser.PAY_CURRENCY,
    FXParser.PAY_AMOUNT,
    FXParser.RECEIVE_CURRENCY,
    FXParser.RECEIVE_AMOUNT,
    FXParser.COUNTRY
  };

  public FXPortfolioLoader(SheetReader sheet) {
    super(sheet, new FXParser(), COLUMNS);
  }  
}
