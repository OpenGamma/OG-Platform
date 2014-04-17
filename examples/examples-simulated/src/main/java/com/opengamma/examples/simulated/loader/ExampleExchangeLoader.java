/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import org.threeten.bp.ZoneId;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.i18n.Country;

/**
 * Example code to load some basic exchange data.
 * <p>
 * This code is kept deliberately as simple as possible to demonstrate pushing data into the exchange master, and to allow basic operations on it by other parts of the system. We have loaders for data
 * available from third party data providers. Please contact us for more information.
 */
@Scriptable
public class ExampleExchangeLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleExchangeLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates two exchange objects directly and stores them into the master.
   * <p>
   * A more typical loader would open a file (e.g. CSV or XML) and use that to create the {@link ManageableExchange} instances to be stored.
   */
  @Override
  protected void doRun() {
    storeExchange(new ManageableExchange(ExternalIdBundle.of(ExternalSchemes.isoMicExchangeId("XLON")), "London Stock Exchange", ExternalIdBundle.of(ExternalSchemes.countryRegionId(Country.GB)),
        ZoneId.of("Europe/London")));
    storeExchange(new ManageableExchange(ExternalIdBundle.of(ExternalSchemes.isoMicExchangeId("XNYS")), "New York Stock Exchange", ExternalIdBundle.of(ExternalSchemes.countryRegionId(Country.US)),
        ZoneId.of("America/New_York")));
  }

  /**
   * Stores the exchange in the exchange master. If there is already an exchange with that name it is updated.
   *
   * @param exchange the exchange to add
   */
  private void storeExchange(final ManageableExchange exchange) {
    final ExchangeMaster master = getToolContext().getExchangeMaster();
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName(exchange.getName());
    final ExchangeSearchResult result = master.search(request);
    if (result.getFirstDocument() != null) {
      //System.out.println("Updating " + exchange.getName());
      final ExchangeDocument document = result.getFirstDocument();
      document.setExchange(exchange);
      master.update(document);
    } else {
      //System.out.println("Adding " + exchange.getName());
      master.add(new ExchangeDocument(exchange));
    }
  }

}
