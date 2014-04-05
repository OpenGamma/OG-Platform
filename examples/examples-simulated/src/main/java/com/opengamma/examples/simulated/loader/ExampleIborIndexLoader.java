/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.ArrayList;
import java.util.List;
import org.threeten.bp.Period;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.util.time.Tenor;
import com.opengamma.financial.analytics.ircurve.IndexType;
import static com.opengamma.core.id.ExternalSchemes.syntheticSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

/**
 * Load a portfolio of {@link IborIndexSecurity} instances for testing.
 * <p>
 * This creates a set of security instances from hard coded values, stores them in the security master and then creates a portfolio containing a position in each.
 */
public class ExampleIborIndexLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleIborIndexLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates five sample instances of {@link IborIndexSecurity}.
   * <p>
   * Our example asset class has an underlying security. For demonstration purposes we will use the first five from the example equities created during installation of the example server.
   *
   * @return the example security instances, not null and not containing null
   */
  protected List<IborIndex> loadSecurities() {
    final List<IborIndex> securities = new ArrayList<IborIndex>();
    IborIndex ii;
    ii = new IborIndex("AUDLIBORP3M", Tenor.of(Period.ofMonths(3)),  syntheticSecurityId("AUDLIBORP3M"));
    ii.addExternalId(syntheticSecurityId("AUDLIBORP3M"));
    securities.add(ii);
    ii = new IborIndex("AUDLIBORP6M", Tenor.of(Period.ofMonths(6)),  syntheticSecurityId("AUDLIBORP6M"));
    ii.addExternalId(syntheticSecurityId("AUDLIBORP6M"));
    securities.add(ii);
    ii = new IborIndex(IndexType.BBSW + "_AUD_P3M", Tenor.of(Period.ofMonths(3)),  syntheticSecurityId("AUDBBP3M"));
    ii.addExternalId(syntheticSecurityId("AUDBBP3M"));
    ii.addExternalId(simpleNameSecurityId(IndexType.BBSW + "_AUD_P3M"));
    securities.add(ii);
    ii = new IborIndex(IndexType.BBSW + "_AUD_P6M", Tenor.of(Period.ofMonths(3)),  syntheticSecurityId("AUDBBP6M"));
    ii.addExternalId(syntheticSecurityId("AUDBBP6M"));
    ii.addExternalId(simpleNameSecurityId(IndexType.BBSW + "_AUD_P6M"));
    securities.add(ii);
    return securities;
  }

  @Override
  protected void doRun() {
    final SecurityMaster securities = getToolContext().getSecurityMaster();
    for (final IborIndex security : loadSecurities()) {
      securities.add(new SecurityDocument(security));
    }
  }

}
