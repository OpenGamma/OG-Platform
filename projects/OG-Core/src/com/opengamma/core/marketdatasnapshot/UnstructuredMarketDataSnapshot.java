package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

/**
 * A set of market data taken at a particular time and potentially altered by hand which 
 *  should be applied to computations in some scope (e.g. a yield curve)
 */
public interface UnstructuredMarketDataSnapshot {

  Map<MarketDataValueSpecification, ValueSnapshot> getValues();
}
