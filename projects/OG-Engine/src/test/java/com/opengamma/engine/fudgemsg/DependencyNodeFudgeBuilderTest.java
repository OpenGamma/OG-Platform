package com.opengamma.engine.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link DependencyNodeFudgeBuilder}
 */
@Test(groups = TestGroup.UNIT)
public class DependencyNodeFudgeBuilderTest extends AbstractFudgeBuilderTestCase {
  
  /**
   * Test graph:
   * 
   *      N0  N1  N4
   *        \ | /  |
   *          N2  N3
   *
   */
  private DependencyNode[] _testNode;
  private DependencyGraph _testGraph;
  private final ValueProperties _properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
  private final ValueSpecification _testValue20 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "20"), _properties);
  private final ValueSpecification _testValue21 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "21"), _properties);
  private final ValueSpecification _testValue24 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "24"), _properties);
  private final ValueSpecification _testValue34 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "34"), _properties);
  private final ValueRequirement _testRequirement0x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "0x"), ValueProperties.none());
  private final ValueSpecification _testValue0x = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "0x"), _properties);
  private final ValueRequirement _testRequirement1x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "1x"), ValueProperties.none());
  private final ValueSpecification _testValue1x = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "1x"), _properties);
  private final ValueRequirement _testRequirement4x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "4x"), ValueProperties.none());
  private final ValueSpecification _testValue4x = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "4x"), _properties);
  private final ValueSpecification _testValuex2 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "x2"), _properties);
  private final ValueSpecification _testValuex3 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "x3"), _properties);

  @BeforeMethod
  public void createGraph() {
    _testGraph = new DependencyGraph("Default");
    _testNode = new DependencyNode[5];
    for (int i = 0; i < _testNode.length; i++) {
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", Integer.toString(i)));
      _testNode[i] = new DependencyNode(target);
      _testNode[i].setFunction(MockFunction.getMockFunction(target, "foo"));
    }
    _testNode[0].addOutputValue(_testValue0x);
    _testNode[1].addOutputValue(_testValue1x);
    _testNode[2].addOutputValue(_testValue20);
    _testNode[2].addOutputValue(_testValue21);
    _testNode[2].addOutputValue(_testValue24);
    _testNode[3].addOutputValue(_testValue34);
    _testNode[4].addOutputValue(_testValue4x);
    _testNode[0].addInputNode(_testNode[2]);
    _testNode[0].addInputValue(_testValue20);
    _testNode[1].addInputNode(_testNode[2]);
    _testNode[1].addInputValue(_testValue21);
    _testNode[2].addInputValue(_testValuex2);
    _testNode[3].addInputValue(_testValuex3);
    _testNode[4].addInputNode(_testNode[2]);
    _testNode[4].addInputValue(_testValue24);
    _testNode[4].addInputNode(_testNode[3]);
    _testNode[4].addInputValue(_testValue34);
    for (DependencyNode a_testNode : _testNode) {
      _testGraph.addDependencyNode(a_testNode);
    }
    _testGraph.addTerminalOutput(_testRequirement0x, _testValue0x);
    _testGraph.addTerminalOutput(_testRequirement1x, _testValue1x);
    _testGraph.addTerminalOutput(_testRequirement4x, _testValue4x);
  }

  @Test
  public void testDependencyNodeFudgeCycle() {
    
    DependencyGraph cycledGraph = cycleObject(DependencyGraph.class, _testGraph);
    
    assertEquals(_testGraph.getCalculationConfigurationName(), cycledGraph.getCalculationConfigurationName());
    assertEquals(_testGraph.getAllComputationTargets(), cycledGraph.getAllComputationTargets());
    assertEquals(_testGraph.getOutputSpecifications(), cycledGraph.getOutputSpecifications());
    assertEquals(_testGraph.getSize(), cycledGraph.getSize());
    assertEquals(_testGraph.getTerminalOutputSpecifications(), cycledGraph.getTerminalOutputSpecifications());
    
    for (DependencyNode node : _testGraph.getDependencyNodes()) {
      boolean isRoot = _testGraph.getRootNodes().contains(node);
      for (ValueSpecification spec : node.getOutputValues()) {
        DependencyNode equivalentNode = cycledGraph.getNodeProducing(spec);
        assertEquals(isRoot, cycledGraph.getRootNodes().contains(equivalentNode));
        assertEquals(node.getInputValues(), equivalentNode.getInputValues());
        assertEquals(node.getOutputValues(), equivalentNode.getOutputValues());
        assertEquals(node.getTerminalOutputValues(), equivalentNode.getTerminalOutputValues());
        assertEquals(node.getFunction().getFunction().getFunctionDefinition().getShortName(), equivalentNode.getFunction().getFunction().getFunctionDefinition().getShortName());
        assertEquals(node.getFunction().getFunction().getFunctionDefinition().getUniqueId(), equivalentNode.getFunction().getFunction().getFunctionDefinition().getUniqueId());
      }
    }
  }
    
}
