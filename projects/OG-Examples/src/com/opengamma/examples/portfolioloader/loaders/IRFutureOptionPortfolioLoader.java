package com.opengamma.examples.portfolioloader.loaders;

import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.IRFutureOptionParser;

public class IRFutureOptionPortfolioLoader extends SimplePortfolioLoader {

  private static final String[] COLUMNS = { 
    IRFutureOptionParser.EXCHANGE,
    IRFutureOptionParser.EXPIRY,
    IRFutureOptionParser.UNDERLYING_ID,
    IRFutureOptionParser.POINT_VALUE,
    IRFutureOptionParser.CURRENCY,
    IRFutureOptionParser.STRIKE,
    IRFutureOptionParser.IS_CALL
  };

  public IRFutureOptionPortfolioLoader(SheetReader sheet) {
    super(sheet, new IRFutureOptionParser(), COLUMNS);
  }

}
