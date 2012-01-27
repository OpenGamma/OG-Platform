package com.opengamma.examples.portfolioloader.loaders;

import com.opengamma.examples.portfolioloader.RowParser;
import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.SwapParser;
import com.opengamma.financial.interestrate.swap.definition.Swap;

public class SwapPortfolioLoader extends SimplePortfolioLoader {

  private static final String[] COLUMNS = {
    SwapParser.TRADE_DATE,
    SwapParser.EFFECTIVE_DATE,
    SwapParser.TERMINATION_DATE,
    SwapParser.PAY_FIXED,
    SwapParser.FIXED_LEG_CURRENCY,
    SwapParser.FIXED_LEG_NOTIONAL,
    SwapParser.FIXED_LEG_DAYCOUNT,
    SwapParser.FIXED_LEG_BUS_DAY_CONVENTION,
    SwapParser.FIXED_LEG_FREQUENCY,
    SwapParser.FIXED_LEG_REGION,
    SwapParser.FIXED_LEG_RATE,
    SwapParser.FLOATING_LEG_CURRENCY,
    SwapParser.FLOATING_LEG_NOTIONAL,
    SwapParser.FLOATING_LEG_DAYCOUNT,
    SwapParser.FLOATING_LEG_BUS_DAY_CONVENTION,
    SwapParser.FLOATING_LEG_FREQUENCY,
    SwapParser.FLOATING_LEG_REGION,
    SwapParser.FLOATING_LEG_RATE,
    SwapParser.FLOATING_LEG_REFERENCE
  };
  
  public SwapPortfolioLoader(SheetReader sheet) {
    super(sheet, new SwapParser(), COLUMNS);
  }

}
