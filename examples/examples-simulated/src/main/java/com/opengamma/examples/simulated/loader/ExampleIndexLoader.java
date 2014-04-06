/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.Period;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.Index;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.util.time.Tenor;
import com.opengamma.financial.analytics.ircurve.IndexType;

import static com.opengamma.core.id.ExternalSchemes.syntheticSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

/**
 * Load a portfolio of {@link IndexSecurity} instances for testing.
 * <p>
 * This creates a set of security instances from hard coded values, stores them in the security master and then creates a portfolio containing a position in each.
 */
public class ExampleIndexLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleIndexLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates five sample instances of {@link IndexSecurity}.
   * <p>
   * Our example asset class has an underlying security. For demonstration purposes we will use the first five from the example equities created during installation of the example server.
   *
   * @return the example security instances, not null and not containing null
   */
  protected List<Index> loadSecurities() {
    final List<Index> securities = new ArrayList<Index>();
    Index ii;


    ii = new IborIndex("EUREURIBORP6M", Tenor.of(Period.ofMonths(6)), simpleNameSecurityId("EUR EURIBOR 6m"));
    ii.addExternalId(syntheticSecurityId("EUREURIBORP6M"));
    securities.add(ii);

    ii = new OvernightIndex("EONIA", simpleNameSecurityId("EUR EONIA"));
    ii.addExternalId(syntheticSecurityId("EONIA"));
    securities.add(ii);

    final String[] currencies = new String[] {"USD", "AUD", "CHF",
                                              "JPY", "GBP", "EUR"};
    final int[] months = new int[] {3, 6, 12};
    for (final int month : months) {
      for (final String currency : currencies) {
        String syntheticid = currency + "LIBORP" + month + "M";
        String simpleid = currency + " LIBOR " + month + "m";
        ii = new IborIndex(syntheticid,
                               Tenor.of(Period.ofMonths(month)),
                               simpleNameSecurityId(simpleid));
        ii.addExternalId(syntheticSecurityId(syntheticid));
        ii.addExternalId(simpleNameSecurityId(simpleid));
        securities.add(ii);
      }
      ii = new IborIndex("EUREURIBORP" + month + "M",
                         Tenor.of(Period.ofMonths(month)),
                         simpleNameSecurityId("EURIBOR " + month + "m"));
      ii.addExternalId(syntheticSecurityId("EUREURIBORP" + month + "M"));
      ii.addExternalId(simpleNameSecurityId("EURIBOR " + month + "m"));
      securities.add(ii);
      if (month != 12) {
        ii = new IborIndex(IndexType.BBSW + "_AUD_P" + month + "M",
                           Tenor.of(Period.ofMonths(month)),
                           simpleNameSecurityId("AUD Bank Bill " + 
                                                  month + "m"));
        ii.addExternalId(syntheticSecurityId("AUDBBP" + month + "M"));
        ii.addExternalId(simpleNameSecurityId(IndexType.BBSW + "_AUD_P" 
                                                + month + "M"));
        securities.add(ii);
      }
    }
    ii = new IborIndex("USDLIBORP7D", Tenor.of(Period.ofDays(7)), simpleNameSecurityId("USD LIBOR 7d"));
    ii.addExternalId(syntheticSecurityId("USDLIBORP7D"));
    securities.add(ii);

    final int[] isdaFixTenor = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30 };
    // ISDA fixing 11.00 New-York
    for (final int element : isdaFixTenor) {
      final String tenorString = element + "Y";
      final String syntheticID = "USDISDA10P" + tenorString;
      ii = new SwapIndex(syntheticID, Tenor.of(Period.ofYears(element)), simpleNameSecurityId("USD LIBOR 3m"));
      ii.addExternalId(syntheticSecurityId(syntheticID));
      securities.add(ii);
    }
    return securities;
  }

  @Override
  protected void doRun() {
    final SecurityMaster securities = getToolContext().getSecurityMaster();
    for (final Index security : loadSecurities()) {
      securities.add(new SecurityDocument(security));
    }
  }

}
