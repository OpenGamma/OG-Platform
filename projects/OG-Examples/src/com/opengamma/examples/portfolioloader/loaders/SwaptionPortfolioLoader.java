package com.opengamma.examples.portfolioloader.loaders;

import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.SwaptionParser;

public class SwaptionPortfolioLoader extends SimplePortfolioLoader {

  private static final String[] COLUMNS = {
    SwaptionParser.EXPIRY,
    SwaptionParser.IS_LONG,
    SwaptionParser.IS_PAYER,
    SwaptionParser.CURRENCY,
    SwaptionParser.TRADE_DATE,
    SwaptionParser.STRIKE,
    SwaptionParser.NOTIONAL,
    SwaptionParser.COUNTERPARTY,
    SwaptionParser.SWAP_LENGTH
  };
  
  public SwaptionPortfolioLoader(SheetReader sheet) {
    super(sheet, new SwaptionParser(), COLUMNS);
  }

}
