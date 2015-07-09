/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.market;

import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditDefaultSwapType;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * Default implementation of {@link IndexCdsMarketDataResolverFn}. Derives
 * a {@link CreditCurveDataKey} from a given {@link IndexCDSSecurity} to be
 * used for resolving market data. 
 */
public class DefaultIndexCdsMarketDataResolverFn implements IndexCdsMarketDataResolverFn {

  private final CreditKeyMapperFn _creditKeyMapperFn;
  /**
   * Creates an instance of the function.
   *
   * @param creditKeyMapperFn credit key mapper function to use
   * inferring restructuring clauses on standard CDSs.
   */
  public DefaultIndexCdsMarketDataResolverFn(CreditKeyMapperFn creditKeyMapperFn) {
    _creditKeyMapperFn = ArgumentChecker.notNull(creditKeyMapperFn, "creditKeyMapperFn");
  }

  @Override
  public Result<CreditCurveDataKey> resolve(Environment env, IndexCDSSecurity security) {
    Currency currency = security.getNotional().getCurrency();

    String referenceEntity = security.getUnderlyingIndex().resolve().getName();

    CreditCurveDataKey key = CreditCurveDataKey.builder()
                                .currency(currency)
                                .curveName(referenceEntity)
                                .cdsType(CreditDefaultSwapType.INDEX)
                                .build();
    
    return _creditKeyMapperFn.getMapping(key);
  }

}
