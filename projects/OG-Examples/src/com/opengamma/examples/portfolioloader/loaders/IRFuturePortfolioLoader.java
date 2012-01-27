package com.opengamma.examples.portfolioloader.loaders;

import com.opengamma.examples.portfolioloader.RowParser;
import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.IRFutureParser;

public class IRFuturePortfolioLoader extends SimplePortfolioLoader {

  private static final String[] COLUMNS = {
    IRFutureParser.EXPIRY,
    IRFutureParser.TRADING_EXCHANGE,
    IRFutureParser.SETTLEMENT_EXCHANGE,
    IRFutureParser.CURRENCY,
    IRFutureParser.UNIT_AMOUNT,
    IRFutureParser.UNDERLYING_ID,
    IRFutureParser.NAME,
    IRFutureParser.BBG_CODE
  };
  
  public IRFuturePortfolioLoader(SheetReader sheet) {
    super(sheet, new IRFutureParser(), COLUMNS);
  }

}
