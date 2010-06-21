/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import javax.time.calendar.LocalDate;

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
  private static final String EXCHANGE_SCHEME = "EXCHANGE_SCHEME";
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
    Region region = _regionRepo.getHierarchyNode(asOf, regionIdentifier);
    Exchange partialExchange = new Exchange(identifiers, name, region);
    UniqueIdentifier uid = _idMapper.add(identifiers, new Exchange(identifiers, name, region));
    partialExchange.setUniqueIdentifier(uid);
    Exchange completedExchange = partialExchange; // pointless, but trying to make a point that before we've set the uid, it's not 'safe'.
    return completedExchange;
  }
}
