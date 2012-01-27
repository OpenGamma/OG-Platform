package com.opengamma.examples.portfolioloader.loaders;

import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.VanillaFXOptionParser;

public class VanillaFXOptionPortfolioLoader extends SimplePortfolioLoader {

  private static final String[] COLUMNS = {
    VanillaFXOptionParser.PUT_CURRENCY,
    VanillaFXOptionParser.CALL_CURRENCY,
    VanillaFXOptionParser.PUT_AMOUNT,
    VanillaFXOptionParser.CALL_AMOUNT,
    VanillaFXOptionParser.EXPIRY,
    VanillaFXOptionParser.IS_LONG
  };
  
  public VanillaFXOptionPortfolioLoader(SheetReader sheet) {
    super(sheet, new VanillaFXOptionParser(), COLUMNS);
  }

}
