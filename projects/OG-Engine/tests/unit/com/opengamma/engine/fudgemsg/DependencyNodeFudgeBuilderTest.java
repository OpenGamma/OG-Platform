package com.opengamma.engine.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test {@link DependencyNodeFudgeBuilder}
 */
public class DependencyNodeFudgeBuilderTest extends AbstractFudgeBuilderTestCase {
  
  /**
   * Test graph:
   * 
   *      N0  N1  N4
   *        \ | /  |
   *          N2  N3
   *
   * If not partitioned:
   *    v20, v21, v24, v34 to go into private cache
   *    v0x, v1x, v4x to go into shared cache
   *    vx2, vx3 to go into shared cache
   */
  private DependencyNode[] _testNode;
  private DependencyGraph _testGraph;
  private final ValueSpecification _testValue20 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "20"), ValueProperties.builder().with(
      ValuePropertyNames.FUNCTION, "Mock").get());
  private final ValueSpecification _testValue21 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "21"), ValueProperties.builder().with(
      ValuePropertyNames.FUNCTION, "Mock").get());
  private final ValueSpecification _testValue24 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "24"), ValueProperties.builder().with(
      ValuePropertyNames.FUNCTION, "Mock").get());
  private final ValueSpecification _testValue34 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "34"), ValueProperties.builder().with(
      ValuePropertyNames.FUNCTION, "Mock").get());


  private final ValueRequirement _testRequirement0x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "0x"), ValueProperties.none());
  private final ValueSpecification _testValue0x = new ValueSpecification(_testRequirement0x, "Mock");

  private final ValueRequirement _testRequirement1x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "1x"), ValueProperties.none());
  private final ValueSpecification _testValue1x = new ValueSpecification(_testRequirement1x, "Mock");

  private final ValueRequirement _testRequirement4x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "4x"), ValueProperties.none());
  private final ValueSpecification _testValue4x = new ValueSpecification(_testRequirement4x, "Mock");

  private final ValueRequirement _testRequirementx2 = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "2x"), ValueProperties.none());
  private final ValueSpecification _testValuex2 = new ValueSpecification(_testRequirementx2, "LiveDataSourcingFunction");

  private final ValueRequirement _testRequirementx3 = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "3x"), ValueProperties.none());
  private final ValueSpecification _testValuex3 = new ValueSpecification(_testRequirementx3, "LiveDataSourcingFunction");

  @BeforeMethod
  public void createGraph() {
    _testGraph = new DependencyGraph("Default");
    _testNode = new DependencyNode[5];
    for (int i = 0; i < _testNode.length; i++) {
      final ComputationTarget target = new ComputationTarget(Integer.toString(i));
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
