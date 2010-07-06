/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.DelegateByScheme;

/**
 * A {@link LiveDataAvailabilityProvider} implementation which allows the scheme of any requested {@link ValueRequirement}
 * to control which underlying {@code LiveDataAvailabilityProvider} is used. If the scheme is not recognized, a default
 * is used.
 */
public class DelegatingLiveDataAvailabilityProvider extends DelegateByScheme<LiveDataAvailabilityProvider> implements
    LiveDataAvailabilityProvider {
  
  private static final Logger s_logger = LoggerFactory.getLogger(DelegatingLiveDataAvailabilityProvider.class);

  /**
   * @param defaultDelegate default provider
   */
  public DelegatingLiveDataAvailabilityProvider(LiveDataAvailabilityProvider defaultDelegate) {
    super(defaultDelegate);
  }

  public DelegatingLiveDataAvailabilityProvider(final LiveDataAvailabilityProvider defaultDelegate,
      final Map<String, LiveDataAvailabilityProvider> delegates) {
    super(defaultDelegate, delegates);
  }

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    s_logger.debug("isAvailable {}", requirement);
    return chooseDelegate(requirement.getTargetSpecification().getUniqueIdentifier()).isAvailable(requirement);
  }

}
