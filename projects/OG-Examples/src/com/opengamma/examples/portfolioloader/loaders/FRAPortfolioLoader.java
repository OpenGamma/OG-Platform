package com.opengamma.examples.portfolioloader.loaders;

import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.FRAParser;

public class FRAPortfolioLoader extends SimplePortfolioLoader {

  private static final String[] COLUMNS = { 
    FRAParser.CURRENCY,
    FRAParser.REGION,
    FRAParser.START_DATE,
    FRAParser.END_DATE,
    FRAParser.RATE,
    FRAParser.AMOUNT,
    FRAParser.BBG_ID
  };

  public FRAPortfolioLoader(SheetReader sheet) {
    super(sheet, new FRAParser(), COLUMNS);
  }  
}
