package com.opengamma.master.portfolio.impl;


import static org.testng.Assert.*;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DynamicDelegatingPortfolioMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class DynamicDelegatingPortfolioMasterTest {

  private final String schemeA = "A";
  private final String schemeB = "B";
  private final ObjectIdSupplier schemeAProvider = new ObjectIdSupplier(schemeA);
  private final ObjectIdSupplier schemeBProvider = new ObjectIdSupplier(schemeB);

  @Test(expectedExceptions = DataNotFoundException.class)
  void test_DefaultDelegateShouldNotFindAnyData() {
    final UniqueId doesNotExist = UniqueId.of(schemeA,"DoesNotExist");
    DynamicDelegatingPortfolioMaster sut = new DynamicDelegatingPortfolioMaster();
    sut.get(doesNotExist);
  }

  @Test
  void test_AddSomeDelegates() {
    PortfolioDocument portA = generatePortfolio("PortA", schemeAProvider);
    PortfolioDocument portB = generatePortfolio("PortB", schemeBProvider);

    DynamicDelegatingPortfolioMaster sut = new DynamicDelegatingPortfolioMaster();

    sut.register(schemeA, new InMemoryPortfolioMaster(schemeAProvider));
    PortfolioDocument addedPortA = sut.add(schemeA, portA);
    assertEquals(addedPortA, portA, "adding the document had unexpected side effect");
    PortfolioDocument fetchedPortA = sut.get(addedPortA.getUniqueId());
    assertEquals(fetchedPortA, portA, "unable to fetch same document right after adding");

    sut.register(schemeB, new InMemoryPortfolioMaster(schemeBProvider));
    PortfolioDocument addedPortB = sut.add(schemeB, portB);
    assertEquals(addedPortB, portB, "adding the document had unexpected side effect");
    PortfolioDocument fetchedPortB = sut.get(addedPortB.getUniqueId());
    assertEquals(fetchedPortB, portB, "unable to fetch same document right after adding");

    fetchedPortA = sut.get(addedPortA.getUniqueId());
    assertEquals(fetchedPortA, portA, "unable to fetch document a second time");

    fetchedPortB = sut.get(addedPortB.getUniqueId());
    assertEquals(fetchedPortB, portB, "unable to fetch document a second time");
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  void test_RemovingDelegates() {
    PortfolioDocument portA = generatePortfolio("PortA", schemeAProvider);
    PortfolioDocument portB = generatePortfolio("PortB", schemeBProvider);

    DynamicDelegatingPortfolioMaster sut = new DynamicDelegatingPortfolioMaster();

    sut.register(schemeA, new InMemoryPortfolioMaster(schemeAProvider));
    UniqueId addedPort = sut.add(schemeA, portA).getUniqueId();

    sut.register(schemeB, new InMemoryPortfolioMaster(schemeBProvider));
    sut.add(schemeB, portB);

    sut.deregister(schemeA);

    sut.get(addedPort); // will throw data not found exception because we deregistered scheme A
  }

  private PortfolioDocument generatePortfolio(String name, ObjectIdSupplier provider) {
    ManageablePortfolioNode rootNode = generatePortfolioNodes(name, provider, 2, 2);
    PortfolioDocument document = new PortfolioDocument(new ManageablePortfolio(name, rootNode));
    return document;
  }

  private ManageablePortfolioNode generatePortfolioNodes(String namePrefix, ObjectIdSupplier provider,  int width, int depth) {
    ManageablePortfolioNode root = new ManageablePortfolioNode(namePrefix);
    root.addPosition(provider.get());
    if (depth > 0) {
      for (int i = 0; i < width; i++) {
        root.addChildNode(generatePortfolioNodes(namePrefix + "-" + depth + "-" + i, provider, width, depth - 1));
      }
    }
    return root;
  }
}
