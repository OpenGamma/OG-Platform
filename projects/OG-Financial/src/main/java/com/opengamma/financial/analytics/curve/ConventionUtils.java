/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Utility class for resolving index conventions in backward compatible fashion
 */
public class ConventionUtils {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ConventionUtils.class);
  /** The security source */
  private SecuritySource _securitySource;
  /** The convention source */
  private ConventionSource _conventionSource;
  
  private ConventionUtils(SecuritySource securitySource, ConventionSource conventionSource) {
    _securitySource = securitySource;
    _conventionSource = conventionSource;
  }
  public static ConventionUtils of(SecuritySource secSource, ConventionSource conventionSource) {
    return new ConventionUtils(secSource, conventionSource);
  }
  
  public ObjectsPair<IborIndexConvention, Tenor> withIborIndexId(ExternalId iborIndexConventionOrIndexId, Tenor legacyTenor) {
    // try to load the index from the sec master
    final Security iborIndexSec = _securitySource.getSingle(iborIndexConventionOrIndexId.toBundle());
    final IborIndexConvention iborIndexConvention;
    final Tenor iborIndexTenor;
    if (iborIndexSec instanceof com.opengamma.financial.security.index.IborIndex) {
      // we managed to load the IborIndex, now look up it's convention.
      com.opengamma.financial.security.index.IborIndex iborIndex = (com.opengamma.financial.security.index.IborIndex) iborIndexSec; // implicit null check
      try {
        iborIndexConvention = _conventionSource.getSingle(iborIndex.getConventionId(), IborIndexConvention.class);
        iborIndexTenor = iborIndex.getTenor();
      } catch (Exception e) {
        s_logger.error("Found IBOR index, but could not find linked convention {}", iborIndex.getConventionId());
        throw new OpenGammaRuntimeException("Found index, but could not find linked convention " + iborIndex.getConventionId(), e);        
      }
    } else {
      // we couldn't find it in the sec master, fall back to checking for a convention using the same ID.
      s_logger.warn("Falling back to iborIndexConvention field pointing to convention rather than index.");
      try {
        iborIndexConvention = _conventionSource.getSingle(iborIndexConventionOrIndexId, IborIndexConvention.class);
        iborIndexTenor = legacyTenor;
      } catch (Exception e) {
        s_logger.error("Could not find ibor index convention {}", iborIndexConventionOrIndexId);        
        throw new OpenGammaRuntimeException("Could not find ibor index convention " + iborIndexConventionOrIndexId, e);
      }
    }
    return ObjectsPair.<IborIndexConvention, Tenor>of(iborIndexConvention, iborIndexTenor);
  }
  
  public IborIndexConvention withIborIndexId(ExternalId iborIndexConventionOrIndexId) {
    // try to load the index from the sec master
    final IborIndexConvention iborIndexConvention;
    if (_securitySource != null) {
      final Security iborIndexSec = _securitySource.getSingle(iborIndexConventionOrIndexId.toBundle());
      if (iborIndexSec instanceof com.opengamma.financial.security.index.IborIndex) {
        // we managed to load the IborIndex, now look up it's convention.
        com.opengamma.financial.security.index.IborIndex iborIndex = (com.opengamma.financial.security.index.IborIndex) iborIndexSec; // implicit null check
        try {
          iborIndexConvention = _conventionSource.getSingle(iborIndex.getConventionId(), IborIndexConvention.class);
        } catch (Exception e) {
          s_logger.error("Found IBOR index, but could not find linked convention {}", iborIndex.getConventionId());
          throw new OpenGammaRuntimeException("Found index, but could not find linked convention " + iborIndex.getConventionId(), e);
        }
        return iborIndexConvention;
      }
      s_logger.warn("Couldn't find index {}, falling back to looking in convention source for compatibility", iborIndexConventionOrIndexId);      
    } else {
      s_logger.warn("Security source was null, falling back to looking in convention source for compatibility", iborIndexConventionOrIndexId);
    }
    try {
      iborIndexConvention = _conventionSource.getSingle(iborIndexConventionOrIndexId, IborIndexConvention.class);
    } catch (Exception e) {
      s_logger.error("Could not find ibor index convention {}", iborIndexConventionOrIndexId);        
      throw new OpenGammaRuntimeException("Could not find ibor index convention " + iborIndexConventionOrIndexId, e);
    }
    return iborIndexConvention;
  }
  
  public ObjectsPair<IborIndexConvention, Tenor> withIborIndexId(ExternalId indexId, ExternalId legacyIndexConventionId, Tenor legacyTenor) {
    // try to load the index from the sec master
    final IborIndexConvention iborIndexConvention;
    if (_securitySource != null) {
      final Security iborIndexSec = _securitySource.getSingle(indexId.toBundle());
      if (iborIndexSec instanceof com.opengamma.financial.security.index.IborIndex) {
        // we managed to load the IborIndex, now look up it's convention.
        com.opengamma.financial.security.index.IborIndex iborIndex = (com.opengamma.financial.security.index.IborIndex) iborIndexSec; // implicit null check
        try {
          iborIndexConvention = _conventionSource.getSingle(iborIndex.getConventionId(), IborIndexConvention.class);
        } catch (Exception e) {
          s_logger.error("Found IBOR index, but could not find linked convention {}", iborIndex.getConventionId());
          throw new OpenGammaRuntimeException("Found index, but could not find linked convention " + iborIndex.getConventionId(), e);
        }
        return ObjectsPair.of(iborIndexConvention, iborIndex.getTenor());
      }
      s_logger.warn("Couldn't find index {}, falling back to looking in convention source for compatibility", indexId);      
    } else {
      s_logger.warn("Security source was null, falling back to looking in convention source for compatibility", indexId);
    }
    try {
      if (legacyIndexConventionId == null) {
        s_logger.error("Legacy overnight index convention was null, giving up.");
        throw new OpenGammaRuntimeException("Legacy overnight index convention was null, giving up");
      }
      iborIndexConvention = _conventionSource.getSingle(legacyIndexConventionId, IborIndexConvention.class);
    } catch (Exception e) {
      s_logger.error("Could not find ibor index convention {}", legacyIndexConventionId);        
      throw new OpenGammaRuntimeException("Could not find ibor index convention " + legacyIndexConventionId, e);
    }
    return ObjectsPair.of(iborIndexConvention, legacyTenor);
  }
  
  public OvernightIndexConvention withOvernightIndexId(ExternalId overnightIndexConventionOrIndexId) {
    final OvernightIndexConvention indexConvention;
    if (_securitySource != null) {
      final Security overnightIndexSec = _securitySource.getSingle(overnightIndexConventionOrIndexId.toBundle());
      if (overnightIndexSec instanceof OvernightIndex) {
        OvernightIndex overnightIndex = (OvernightIndex) overnightIndexSec; // implicit null check
        try {
          indexConvention = _conventionSource.getSingle(overnightIndex.getConventionId(), OvernightIndexConvention.class);
        } catch (Exception e) {
          s_logger.error("Found index, but could not find linked convention {}", overnightIndex.getConventionId());
          throw new OpenGammaRuntimeException("Found index, but could not find linked convention " + overnightIndex.getConventionId(), e);
        }
        return indexConvention;
      }
      s_logger.warn("Couldn't find index {}, falling back to looking in convention source for compatibility", overnightIndexConventionOrIndexId);      
    } else {
      s_logger.warn("Security source was null, falling back to looking in convention source for compatibility");
    }
    try {
      indexConvention = _conventionSource.getSingle(overnightIndexConventionOrIndexId, OvernightIndexConvention.class);
    } catch (Exception e) {
      s_logger.error("Could not find legacy overnight index convention {}", overnightIndexConventionOrIndexId);
      throw new OpenGammaRuntimeException("Could not find legacy overnight index convention " + overnightIndexConventionOrIndexId, e);
    }
    return indexConvention;
  }
  
  public OvernightIndexConvention withOvernightIndexId(ExternalId indexId, ExternalId legacyIndexConventionId) {
    final OvernightIndexConvention indexConvention;
    if (_securitySource != null) {
      final Security overnightIndexSec = _securitySource.getSingle(indexId.toBundle());
      if (overnightIndexSec instanceof OvernightIndex) {
        OvernightIndex overnightIndex = (OvernightIndex) overnightIndexSec; // implicit null check
        try {
          indexConvention = _conventionSource.getSingle(overnightIndex.getConventionId(), OvernightIndexConvention.class);
        } catch (Exception e) {
          s_logger.error("Found index, but could not find linked convention {}", overnightIndex.getConventionId());
          throw new OpenGammaRuntimeException("Found index, but could not find linked convention " + overnightIndex.getConventionId(), e);
        }
        return indexConvention;
      }
      s_logger.warn("Couldn't find index {}, falling back to looking in convention source for compatibility", indexId);      
    } else {
      s_logger.warn("Security source was null, falling back to looking in convention source for compatibility");
    }
    try {
      if (legacyIndexConventionId == null) {
        s_logger.error("Legacy overnight index convention was null, giving up.");
        throw new OpenGammaRuntimeException("Legacy overnight index convention was null, giving up");
      }
      indexConvention = _conventionSource.getSingle(legacyIndexConventionId, OvernightIndexConvention.class);
    } catch (Exception e) {
      s_logger.error("Could not find legacy overnight index convention {}", legacyIndexConventionId);
      throw new OpenGammaRuntimeException("Could not find legacy overnight index convention " + legacyIndexConventionId, e);
    }
    return indexConvention;
  }
  
  public ObjectsPair<PriceIndexConvention, ExternalIdBundle> withPriceIndexId(ExternalId priceIndexConventionOrIndexId) {
    final PriceIndexConvention indexConvention;
    final ExternalIdBundle ids;
    if (_securitySource != null) {
      final Security priceIndexSec = _securitySource.getSingle(priceIndexConventionOrIndexId.toBundle());
      if (priceIndexSec instanceof PriceIndex) {
        PriceIndex priceIndex = (PriceIndex) priceIndexSec; // implicit null check
        try {
          indexConvention = _conventionSource.getSingle(priceIndex.getConventionId(), PriceIndexConvention.class);
          ids = priceIndex.getExternalIdBundle();
        } catch (Exception e) {
          s_logger.error("Found price index, but could not find linked convention {}", priceIndex.getConventionId());
          throw new OpenGammaRuntimeException("Found index, but could not find linked convention " + priceIndex.getConventionId(), e);
        }
        return ObjectsPair.of(indexConvention, ids);
      }
      s_logger.warn("Couldn't find price index {}, falling back to looking in convention source for compatibility", priceIndexConventionOrIndexId);      
    } else {
      s_logger.warn("Security source was null, falling back to looking in convention source for compatibility", priceIndexConventionOrIndexId);
    }
    try {
      indexConvention = _conventionSource.getSingle(priceIndexConventionOrIndexId, PriceIndexConvention.class);
    } catch (Exception e) {
      s_logger.error("Could not find legacy price index convention {}", priceIndexConventionOrIndexId);
      throw new OpenGammaRuntimeException("Could not find legacy price index convention " + priceIndexConventionOrIndexId, e);
    }
    return ObjectsPair.of(indexConvention, indexConvention.getPriceIndexId().toBundle());
  }
  
  public SwapIndexConvention withSwapIndexId(ExternalId swapIndexConventionOrIndexId) {
    // try to load the index from the sec master
    final SwapIndexConvention swapIndexConvention;
    if (_securitySource != null) {
      final Security swapIndexSec = _securitySource.getSingle(swapIndexConventionOrIndexId.toBundle());
      if (swapIndexSec instanceof com.opengamma.financial.security.index.SwapIndex) {
        // we managed to load the IborIndex, now look up it's convention.
        com.opengamma.financial.security.index.SwapIndex swapIndex = (com.opengamma.financial.security.index.SwapIndex) swapIndexSec; // implicit null check
        try {
          swapIndexConvention = _conventionSource.getSingle(swapIndex.getConventionId(), SwapIndexConvention.class);
        } catch (Exception e) {
          s_logger.error("Found swap index, but could not find linked convention {}", swapIndex.getConventionId());
          throw new OpenGammaRuntimeException("Found swap index, but could not find linked convention " + swapIndex.getConventionId(), e);
        }
        return swapIndexConvention;
      }
      s_logger.warn("Couldn't find swap index {}, falling back to looking in convention source for compatibility", swapIndexConventionOrIndexId);      
    } else {
      s_logger.warn("Security source was null, falling back to looking in convention source for compatibility", swapIndexConventionOrIndexId);
    }
    try {
      swapIndexConvention = _conventionSource.getSingle(swapIndexConventionOrIndexId, SwapIndexConvention.class);
    } catch (Exception e) {
      s_logger.error("Could not find swap index convention {}", swapIndexConventionOrIndexId);        
      throw new OpenGammaRuntimeException("Could not find swap index convention " + swapIndexConventionOrIndexId, e);
    }
    return swapIndexConvention;
  }
}
