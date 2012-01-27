package com.opengamma.examples.portfolioloader.loaders;

import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.CashParser;

public class CashPortfolioLoader extends SimplePortfolioLoader {

  private static final String[] COLUMNS = { 
    CashParser.CURRENCY,
    CashParser.REGION,
    CashParser.MATURITY,
    CashParser.RATE,
    CashParser.AMOUNT
  };

  public CashPortfolioLoader(SheetReader sheet) {
    super(sheet, new CashParser(), COLUMNS);
  }  
}
