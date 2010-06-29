/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleMapper;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 *
 * @author jim
 */
public class InMemoryExchangeRespository implements ExchangeRepository {
  public static final String EXCHANGE_SCHEME = "EXCHANGE_SCHEME"; // so the unit test can see it.
  private IdentifierBundleMapper<Exchange> _idMapper = new IdentifierBundleMapper<Exchange>(EXCHANGE_SCHEME);
  private RegionRepository _regionRepo;
  
  public InMemoryExchangeRespository(RegionRepository regionRepo) {
    _regionRepo = regionRepo;
  }
  
  @Override
  public Exchange resolveExchange(LocalDate asOf, IdentifierBundle identifiers) {
    return _idMapper.get(identifiers);
  }

  @Override
  public Exchange resolveExchange(LocalDate asOf, Identifier identifier) {
    return _idMapper.get(identifier);
  }

  @Override
  public Exchange resolveExchange(LocalDate asOf, UniqueIdentifier identifier) {
    return _idMapper.get(identifier);
  }

  public Exchange putExchange(LocalDate asOf, IdentifierBundle identifiers, String name, UniqueIdentifier regionIdentifier) {
    Exchange exchange = resolveExchange(asOf, identifiers);
    Region region = _regionRepo.getHierarchyNode(asOf, regionIdentifier);
    if (exchange == null) {
      Exchange partialExchange = new Exchange(identifiers, name, region);
      UniqueIdentifier uid = _idMapper.add(identifiers, partialExchange);
      partialExchange.setUniqueIdentifier(uid);
      Exchange completedExchange = partialExchange; // pointless, but trying to make a point that before we've set the uid, it's not 'safe'.
      return completedExchange;
    } else {
      if (name.equals(exchange.getName()) &&
        region.equals(exchange.getRegion())) {
        _idMapper.add(identifiers, exchange);
        IdentifierBundle identifierBundle = _idMapper.getIdentifierBundle(exchange);
        exchange.setIdentifiers(identifierBundle);
        return exchange;
      } else {
        throw new OpenGammaRuntimeException("supplied name [" + name + "] and region [" + region + "] conflicts with exchange [" + exchange + "] matching an id in the supplied bundle.");
      }
    }
  }
}
