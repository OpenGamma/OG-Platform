/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

import org.junit.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.InMemoryRequestConduit;

/**
 * 
 *
 * @author kirk
 */
public class RemoteCacheRequestResponseTest {
  
  @Test(timeout=10000l)
  public void singleThreadSpecLookupDifferentIdentifierValues() {
    RemoteCacheServer server = new RemoteCacheServer();
    FudgeRequestSender conduit = InMemoryRequestConduit.create(server);
    RemoteCacheClient client = new RemoteCacheClient(conduit);
    
    BitSet seenIds = new BitSet();
    for(int i = 0; i < 10; i++) {
      ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, new DomainSpecificIdentifier(new IdentificationDomain("Kirk"), "Value" + i))));
      long id = client.getValueSpecificationId(valueSpec);
      assertTrue(id <= Integer.MAX_VALUE);
      assertFalse(seenIds.get((int) id));
      seenIds.set((int) id);
    }
  }

}
