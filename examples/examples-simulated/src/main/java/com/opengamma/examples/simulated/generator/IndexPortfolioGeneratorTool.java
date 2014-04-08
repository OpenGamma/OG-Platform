/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Tenor;

/**
 * Creates a portfolio of indices. 
 */
// TODO what is the best way to do load these index securities? There is no need to create the portfolio 
public class IndexPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The indices */
  private static final List<ManageableSecurity> INDICES = new ArrayList<>();

  static {
    final String[] currencies = new String[] {"USD", "EUR", "JPY", "CHF", "GBP" };
<<<<<<< HEAD
    final String[] overnightTickers = new String[] {"USDFF", "EONIA", "TONAR", "TOISTOIS", "SONIO" };
    final Tenor[] tenors = new Tenor[] {Tenor.ONE_MONTH, Tenor.THREE_MONTHS, Tenor.SIX_MONTHS };
    for (int i = 0; i < currencies.length; i++) {
      final String currency = currencies[i];
      final String overnightTicker = overnightTickers[i];
      for (final Tenor tenor : tenors) {
        final String iborTicker = currency + "LIBOR" + tenor.toFormattedString();
=======
    final Tenor[] tenors = new Tenor[] {Tenor.ONE_MONTH, Tenor.THREE_MONTHS, Tenor.SIX_MONTHS };
    for (final String currency : currencies) {
      for (final Tenor tenor : tenors) {
        final String iborTicker = currency + " " + tenor.toFormattedString().substring(1) + " IBOR INDEX";
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
        final ExternalId iborIndexId = ExternalSchemes.syntheticSecurityId(iborTicker);
        final IborIndex iborIndex = new IborIndex(iborTicker, tenor, iborIndexId);
        iborIndex.setExternalIdBundle(iborIndexId.toBundle());
        INDICES.add(iborIndex);
<<<<<<< HEAD
        final ExternalId overnightIndexId = ExternalSchemes.syntheticSecurityId(overnightTickers[i]);
=======
        final String overnightTicker = currency + " OVERNIGHT INDEX";
        final ExternalId overnightIndexId = ExternalSchemes.syntheticSecurityId(overnightTicker);
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
        final OvernightIndex overnightIndex = new OvernightIndex(overnightTicker, overnightIndexId);
        overnightIndex.setExternalIdBundle(overnightIndexId.toBundle());
        INDICES.add(overnightIndex);
      }
    }
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final SecurityGenerator<ManageableSecurity> securities = createIndexSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Indices"), positions, INDICES.size());
  }

  /**
   * Creates a security generator that loops over the list of indices.
   * @return The security generator
   */
  private SecurityGenerator<ManageableSecurity> createIndexSecurityGenerator() {
    final SecurityGenerator<ManageableSecurity> securities = new SecurityGenerator<ManageableSecurity>() {
      private int _count;

      @SuppressWarnings("synthetic-access")
      @Override
      public ManageableSecurity createSecurity() {
        final ManageableSecurity index = INDICES.get(_count++);
        return index;
      }

    };
    configure(securities);
    return securities;
  }

}
