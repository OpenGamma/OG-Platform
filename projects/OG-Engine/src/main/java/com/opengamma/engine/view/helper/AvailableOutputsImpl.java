/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ValueProperties;

/**
 * Partial implementation of {@link AvailableOutputs} using {@link AvailableOutputImpl} instances.
 */
public class AvailableOutputsImpl implements AvailableOutputs {

  private final Set<String> _securityTypes = new HashSet<String>();
  private final Map<String, AvailableOutputImpl> _outputs = new HashMap<String, AvailableOutputImpl>();

  public AvailableOutputsImpl() {
  }

  protected AvailableOutputImpl createOutput(final String valueName) {
    return new AvailableOutputImpl(valueName, null);
  }

  private AvailableOutputImpl getOrCreateOutput(final String valueName) {
    AvailableOutputImpl output = _outputs.get(valueName);
    if (output == null) {
      output = createOutput(valueName);
      _outputs.put(valueName, output);
    }
    return output;
  }

  public void portfolioNodeOutput(final String valueName, final ValueProperties properties) {
    final AvailableOutputImpl output = getOrCreateOutput(valueName);
    output.setPortfolioNodeProperties(properties);
  }

  public void positionOutput(final String valueName, final String securityType, final ValueProperties properties) {
    final AvailableOutputImpl output = getOrCreateOutput(valueName);
    _securityTypes.add(securityType);
    output.setPositionProperties(properties, securityType);
  }

  @Override
  public Set<String> getSecurityTypes() {
    return Collections.unmodifiableSet(_securityTypes);
  }

  @Override
  public Set<AvailableOutput> getPositionOutputs(final String securityType) {
    final Set<AvailableOutput> result = new HashSet<AvailableOutput>();
    for (AvailableOutputImpl output : _outputs.values()) {
      if (output.isAvailableOn(securityType)) {
        result.add(AvailableOutputImpl.ofPosition(output, securityType));
      }
    }
    return result;
  }

  @Override
  public Set<AvailableOutput> getPortfolioNodeOutputs() {
    final Set<AvailableOutput> result = new HashSet<AvailableOutput>();
    for (AvailableOutputImpl output : _outputs.values()) {
      if (output.isAvailableOnPortfolioNode()) {
        result.add(AvailableOutputImpl.ofPortfolioNode(output));
      }
    }
    return result;
  }

  @Override
  public Set<AvailableOutput> getPositionOutputs() {
    final Set<AvailableOutput> result = new HashSet<AvailableOutput>();
    for (AvailableOutputImpl output : _outputs.values()) {
      if (output.isAvailableOnPosition()) {
        result.add(AvailableOutputImpl.ofPosition(output));
      }
    }
    return result;
  }

  @Override
  public Set<AvailableOutput> getOutputs() {
    return new HashSet<AvailableOutput>(_outputs.values());
  }

}
