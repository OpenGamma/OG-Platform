/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;

/**
 * Function to classify positions by Currency.
 *
 */
public class RegionAggregationFunction implements AggregationFunction<String> {
  private boolean _useAttributes;
  
  private static final Logger s_logger = LoggerFactory.getLogger(RegionAggregationFunction.class);
  private static final String NAME = "Region";
  private static final String OTHER = "Other";
  private static final String NO_REGION = "N/A";
  
  private static final Set<String> s_topLevelRegions = Sets.newHashSet("Africa", "Asia", "South America", "Europe");
  private static final Set<String> s_specialCountriesRegions = Sets.newHashSet("United States", "Canada");
  private static final Set<String> s_requiredEntries = Sets.newHashSet();
  
  static {
    s_requiredEntries.addAll(s_topLevelRegions);
    s_requiredEntries.addAll(s_specialCountriesRegions);
    s_requiredEntries.add(OTHER);
    s_requiredEntries.add(NO_REGION);
  }
  
  private SecuritySource _secSource;
  private RegionSource _regionSource;
  private ExchangeSource _exchangeSource;
    
  public RegionAggregationFunction(SecuritySource secSource, RegionSource regionSource, ExchangeSource exchangeSource) {
    this(secSource, regionSource, exchangeSource, true);
  }
  
  public RegionAggregationFunction(SecuritySource secSource, RegionSource regionSource, ExchangeSource exchangeSource, boolean useAttributes) {
    _secSource = secSource;
    _regionSource = regionSource;
    _exchangeSource = exchangeSource;
    _useAttributes = useAttributes;
  }
  
  /**
   * Can use this when no RegionSource available and will get the ISO code instead of the pretty string.
   */
  public RegionAggregationFunction() {
    _regionSource = null;
    _exchangeSource = null;
  }
  
  @Override
  public String classifyPosition(Position position) {
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      s_logger.warn("attributes on " + position + " = " + attributes.entrySet());
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NO_REGION;
      }
    } else {
      try {
        Security security = position.getSecurityLink().resolve(_secSource);
        ExternalId id = FinancialSecurityUtils.getRegion(security);
        if (_regionSource != null) {
          if (id != null) {
            Region highestLevelRegion = _regionSource.getHighestLevelRegion(id);
            if (highestLevelRegion != null) {
              return highestLevelRegion.getName();
            } else {
              return id.getValue();
            }
          } else if (_exchangeSource != null) {
            ExternalId exchangeId = FinancialSecurityUtils.getExchange(security);
            if (exchangeId != null) {
              Exchange exchange = _exchangeSource.getSingleExchange(exchangeId);
              if (exchange == null) {
                s_logger.info("No exchange could be found with ID {}", exchangeId);
                return NO_REGION;
              }
              if (exchange.getRegionIdBundle() == null) {
                s_logger.info("Exchange " + exchange.getName() + " region bundle was null");
                return NO_REGION;
              }
              Region highestLevelRegion = _regionSource.getHighestLevelRegion(exchange.getRegionIdBundle());
              if (s_specialCountriesRegions.contains(highestLevelRegion.getName())) {
                return highestLevelRegion.getName();
              } else {
                Set<UniqueId> parentRegionIds = highestLevelRegion.getParentRegionIds();
                s_logger.info("got " + highestLevelRegion + ", looking for parent");
                String parent = findTopLevelRegion(parentRegionIds);
                s_logger.info("parent was " + parent);
                return parent;
              }
            }
          } 
          return NO_REGION;
        } else {
          if (id != null) {
            return id.getValue();
          } else {
            return NO_REGION;
          }
        }    
      } catch (UnsupportedOperationException ex) {
        return NO_REGION;
      }
    }
  }
  
  private String findTopLevelRegion(Set<?> parentRegions) {
    for (Object parentRegion : parentRegions) {
      Region region;
      if (parentRegion instanceof String) {
        region = _regionSource.getRegion(UniqueId.parse((String) parentRegion));
      } else {
        region = _regionSource.getRegion((UniqueId) parentRegion);
      }
      if (region != null) {
        if (s_topLevelRegions.contains(region.getName())) {
          return region.getName();
        }
      }
    }
    return OTHER;
  }

  public String getName() {
    return NAME;
  }
  


  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.unmodifiableSet(s_requiredEntries);
  }
}
