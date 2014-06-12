/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge message builder for {@link DependencyGraph}
 * <p>
 * Nodes are all allocated an identifier. When a node is used by two or more other nodes, one will write the node content (including its identifier) as a sub-message and the other(s) will reference
 * the node with a numeric field value.
 * <p>
 * {@link ComputationTargetSpecification}values are either written in full for a node as a Fudge message, or if they are present elsewhere in the graph are written as the numeric identifier of the
 * node that has the full target specification sub-message.
 * <p>
 * {@link DependencyNodeFunction} values are either written in full for a node as a Fudge message, as a pair of {@link #FUNCTION_IDENTIFIER_FIELD} and {@link #FUNCTION_PARAMETERS_FIELD}, or if they
 * are present elsewhere in the graph are written as the numeric identifier of the node that has the full function details. If the parameters are empty, the parameter field is omitted.
 * <p>
 * {@link ValueSpecification} labels on graph edges are written as a value name and {@link ValueProperties} pair instead of a full value specification as the target is implied by the target of the
 * node. The inputs to a node are written in full - these imply the outputs of the node that provides them. Only outputs that are not consumed by other nodes (typically terminal outputs in a graph
 * that has had unnecessary values removed from it) are written for each node explicitly.
 * <p>
 * The terminal outputs are written as repeated {@link #TERMINAL_OUTPUT_FIELD} fields containing the value name, properties and target with repeated {@link #VALUE_REQUIREMENT_FIELD} fields for the
 * corresponding requirements. The target will always be the identifier of a node acting on that target; but may not necessarily be the node that produced that value specification.
 * <p>
 * {@code ValueProperties} values are either written as a Fudge message encoding the value properties, possibly with an additional {@link #IDENTIFIER_FIELD} field allocating a numeric value to it.
 * Instead of duplicating value property data, other values might be written as a numeric field referencing another instance in the message.
 */
@GenericFudgeBuilderFor(DependencyGraph.class)
public class DependencyGraphFudgeBuilder implements FudgeBuilder<DependencyGraph> {

  private static final Integer VISITED = Integer.valueOf(0);
  private static final ValueSpecification[] NO_OUTPUTS = new ValueSpecification[0];

  /** Fudge field name. */
  public static final String CALCULATION_CONFIGURATION_NAME_FIELD = "calculationConfiguration";
  /** Fudge field name. */
  public static final String NODE_FIELD = "node";
  /** Fudge field name. */
  public static final String SIZE_FIELD = "size";
  /** Fudge field name. */
  public static final String TERMINAL_OUTPUT_FIELD = "terminal";
  /** Fudge field name. */
  public static final String VALUE_REQUIREMENT_FIELD = "requirement";
  /** Fudge field name. */
  public static final String IDENTIFIER_FIELD = "id";
  /** Fudge field name. */
  public static final String FUNCTION_IDENTIFIER_FIELD = "function";
  /** Fudge field name. */
  public static final String FUNCTION_PARAMETERS_FIELD = "parameters";
  /** Fudge field name. */
  public static final String TARGET_FIELD = "target";
  /** Fudge field name. */
  public static final String INPUT_FIELD = "input";
  /** Fudge field name. */
  public static final String OUTPUT_FIELD = "output";
  /** Fudge field name. */
  public static final String VALUE_NAME_FIELD = "value";
  /** Fudge field name. */
  public static final String PROPERTIES_FIELD = "properties";

  private static class MessageEncoder {

    private final FudgeSerializer _serializer;
    private final Map<DependencyNode, Integer> _nodeIdentifiers;
    private final Map<ComputationTargetSpecification, Integer> _commonTargets;
    private final Map<DependencyNodeFunction, Integer> _commonFunctions = new Object2ObjectOpenCustomHashMap<DependencyNodeFunction, Integer>(DependencyNodeFunctionImpl.HASHING_STRATEGY);
    private final Set<ValueSpecification> _unconsumedOutputs = new HashSet<ValueSpecification>();
    private final Map<ValueProperties, Object> _properties = new HashMap<ValueProperties, Object>();
    private int _nextPropertyId;

    public MessageEncoder(final FudgeSerializer serializer, final int graphSize) {
      _serializer = serializer;
      _nodeIdentifiers = Maps.newHashMapWithExpectedSize(graphSize);
      _commonTargets = Maps.newHashMapWithExpectedSize(graphSize);
    }

    public MutableFudgeMsg encode(final DependencyGraph depGraph) {
      MutableFudgeMsg msg = _serializer.newMessage();
      msg.add(CALCULATION_CONFIGURATION_NAME_FIELD, null, depGraph.getCalculationConfigurationName());
      final int count = depGraph.getRootCount();
      for (int i = 0; i < count; i++) {
        final DependencyNode root = depGraph.getRootNode(i);
        findUnconsumedOutputs(root);
      }
      _nodeIdentifiers.clear();
      for (int i = 0; i < count; i++) {
        final DependencyNode root = depGraph.getRootNode(i);
        addNodeToMessage(msg, root);
      }
      final int size = _nodeIdentifiers.size();
      msg.add(SIZE_FIELD, null, size);
      for (Map.Entry<ValueSpecification, Set<ValueRequirement>> terminal : depGraph.getTerminalOutputs().entrySet()) {
        final MutableFudgeMsg subMsg = msg.addSubMessage(TERMINAL_OUTPUT_FIELD, null);
        subMsg.add(VALUE_NAME_FIELD, null, terminal.getKey().getValueName());
        subMsg.add(TARGET_FIELD, null, _commonTargets.get(terminal.getKey().getTargetSpecification()));
        _serializer.addToMessage(subMsg, PROPERTIES_FIELD, null, terminal.getKey().getProperties());
        for (ValueRequirement requirement : terminal.getValue()) {
          // TODO: Use some form of dictionary lookup to get the size of the value requirements down; at least avoid writing the value name
          _serializer.addToMessage(subMsg, VALUE_REQUIREMENT_FIELD, null, requirement);
        }
      }
      return msg;
    }

    private void findUnconsumedOutputs(final DependencyNode node) {
      if (_nodeIdentifiers.put(node, VISITED) != null) {
        return;
      }
      int count = node.getInputCount();
      for (int i = 0; i < count; i++) {
        findUnconsumedOutputs(node.getInputNode(i));
        _unconsumedOutputs.remove(node.getInputValue(i));
      }
      count = node.getOutputCount();
      for (int i = 0; i < count; i++) {
        _unconsumedOutputs.add(node.getOutputValue(i));
      }
    }

    private void addValuePropertiesToMessage(final MutableFudgeMsg msg, final ValueProperties properties) {
      Object v = _properties.get(properties);
      if (v == null) {
        // This is the first use, write in full and store for later
        final MutableFudgeMsg propertiesMsg = _serializer.objectToFudgeMsg(properties);
        _properties.put(properties, propertiesMsg);
        msg.add(PROPERTIES_FIELD, null, propertiesMsg);
      } else if (v instanceof Integer) {
        // This has already been used at least twice
        msg.add(PROPERTIES_FIELD, null, v);
      } else if (v instanceof MutableFudgeMsg) {
        // This has been written once before; add an integer to it and use it
        final MutableFudgeMsg propertiesMsg = (MutableFudgeMsg) v;
        final Integer identifier = _nextPropertyId++;
        propertiesMsg.add(IDENTIFIER_FIELD, null, identifier);
        _properties.put(properties, identifier);
        msg.add(PROPERTIES_FIELD, null, identifier);
      }
    }

    private void addNodeToMessage(final MutableFudgeMsg msg, final DependencyNode node) {
      Integer identifier = _nodeIdentifiers.get(node);
      if (identifier != null) {
        msg.add(NODE_FIELD, null, identifier);
        return;
      }
      identifier = _nodeIdentifiers.size();
      _nodeIdentifiers.put(node, identifier);
      final MutableFudgeMsg nodeMsg = msg.addSubMessage(NODE_FIELD, null);
      nodeMsg.add(IDENTIFIER_FIELD, null, identifier);
      int count = node.getInputCount();
      for (int i = 0; i < count; i++) {
        final ValueSpecification inputSpec = node.getInputValue(i);
        final MutableFudgeMsg inputMsg = nodeMsg.addSubMessage(INPUT_FIELD, null);
        addNodeToMessage(inputMsg, node.getInputNode(i));
        inputMsg.add(VALUE_NAME_FIELD, inputSpec.getValueName());
        addValuePropertiesToMessage(inputMsg, inputSpec.getProperties());
      }
      final Integer targetId = _commonTargets.get(node.getTarget());
      if (targetId == null) {
        _serializer.addToMessage(nodeMsg, TARGET_FIELD, null, node.getTarget());
        _commonTargets.put(node.getTarget(), identifier);
      } else {
        nodeMsg.add(TARGET_FIELD, null, targetId);
      }
      final Integer functionId = _commonFunctions.get(node.getFunction());
      if (functionId == null) {
        nodeMsg.add(FUNCTION_IDENTIFIER_FIELD, null, node.getFunction().getFunctionId());
        if (!EmptyFunctionParameters.INSTANCE.equals(node.getFunction().getParameters())) {
          _serializer.addToMessageWithClassHeaders(nodeMsg, FUNCTION_PARAMETERS_FIELD, null, node.getFunction().getParameters(), FunctionParameters.class);
        }
        _commonFunctions.put(node.getFunction(), identifier);
      } else {
        nodeMsg.add(FUNCTION_IDENTIFIER_FIELD, null, functionId);
      }
      count = node.getOutputCount();
      for (int i = 0; i < count; i++) {
        final ValueSpecification outputSpec = node.getOutputValue(i);
        if (_unconsumedOutputs.contains(outputSpec)) {
          final MutableFudgeMsg outputMsg = nodeMsg.addSubMessage(OUTPUT_FIELD, null);
          outputMsg.add(VALUE_NAME_FIELD, outputSpec.getValueName());
          addValuePropertiesToMessage(outputMsg, outputSpec.getProperties());
        }
      }
    }

  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DependencyGraph depGraph) {
    final MessageEncoder encoder = new MessageEncoder(serializer, depGraph.getSize());
    return encoder.encode(depGraph);
  }

  private static class TempDependencyNode {

    private final DependencyNodeFunction _function;
    private final ComputationTargetSpecification _target;
    private final ValueSpecification[] _inputValues;
    private final TempDependencyNode[] _inputNodes;
    private ValueSpecification[] _outputValues;
    private DependencyNode _node;

    public TempDependencyNode(final DependencyNodeFunction function, final ComputationTargetSpecification target, final ValueSpecification[] outputValues, final ValueSpecification[] inputValues,
        final TempDependencyNode[] inputNodes) {
      _function = function;
      _target = target;
      _outputValues = outputValues;
      _inputNodes = inputNodes;
      _inputValues = inputValues;
    }

    public DependencyNodeFunction getFunction() {
      return _function;
    }

    public ComputationTargetSpecification getTarget() {
      return _target;
    }

    public void addOutputValue(final ValueSpecification output) {
      for (ValueSpecification existing : _outputValues) {
        if (output.equals(existing)) {
          return;
        }
      }
      final ValueSpecification[] newOutputs = new ValueSpecification[_outputValues.length + 1];
      System.arraycopy(_outputValues, 0, newOutputs, 0, _outputValues.length);
      newOutputs[_outputValues.length] = output;
      _outputValues = newOutputs;
    }

    public DependencyNode toNode() {
      if (_node == null) {
        final DependencyNode[] inputNodes = new DependencyNode[_inputNodes.length];
        for (int i = 0; i < inputNodes.length; i++) {
          final TempDependencyNode input = _inputNodes[i];
          // Allow the GC to get rid of the temporary data as soon as possible
          _inputNodes[i] = null;
          inputNodes[i] = input.toNode();
        }
        _node = DependencyNodeImpl.of(_function, _target, _outputValues, _inputValues, inputNodes);
      }
      return _node;
    }

  }

  private static class MessageDecoder {

    private final FudgeDeserializer _deserializer;
    private final Map<Integer, ValueProperties> _properties = new HashMap<Integer, ValueProperties>();
    private TempDependencyNode[] _nodes;

    public MessageDecoder(final FudgeDeserializer deserializer) {
      _deserializer = deserializer;
    }

    private DependencyNodeFunction getFunctionFromMessage(final FudgeMsg msg) {
      FudgeField field = msg.getByName(FUNCTION_IDENTIFIER_FIELD);
      Object value = field.getValue();
      if (value instanceof Number) {
        final int identifier = ((Number) value).intValue();
        assert _nodes[identifier] != null;
        return _nodes[identifier].getFunction();
      } else {
        final String functionId = (String) value;
        field = msg.getByName(FUNCTION_PARAMETERS_FIELD);
        final FunctionParameters parameters;
        if (field != null) {
          parameters = _deserializer.fieldValueToObject(FunctionParameters.class, field);
        } else {
          parameters = EmptyFunctionParameters.INSTANCE;
        }
        return DependencyNodeFunctionImpl.of(functionId, parameters);
      }
    }

    private ComputationTargetSpecification getTargetFromField(final FudgeField field) {
      final Object fieldValue = field.getValue();
      if (fieldValue instanceof Number) {
        final int identifier = ((Number) fieldValue).intValue();
        assert _nodes[identifier] != null;
        return _nodes[identifier].getTarget();
      } else {
        return MemoryUtils.instance(_deserializer.fudgeMsgToObject(ComputationTargetReference.class, (FudgeMsg) fieldValue).getSpecification());
      }
    }

    private ValueProperties getValuePropertiesFromField(final FudgeField field) {
      final Object fieldValue = field.getValue();
      if (fieldValue instanceof Number) {
        final Integer identifier = ((Number) fieldValue).intValue();
        assert _properties.containsKey(identifier);
        return _properties.get(identifier);
      } else {
        final FudgeMsg msg = (FudgeMsg) fieldValue;
        final ValueProperties properties = _deserializer.fudgeMsgToObject(ValueProperties.class, msg);
        final Integer identifier = msg.getInt(IDENTIFIER_FIELD);
        if (identifier != null) {
          _properties.put(identifier, properties);
        }
        return properties;
      }
    }

    private TempDependencyNode getNodeFromField(final FudgeField field) {
      final Object fieldValue = field.getValue();
      if (fieldValue instanceof Number) {
        final int identifier = ((Number) fieldValue).intValue();
        assert _nodes[identifier] != null;
        return _nodes[identifier];
      }
      final FudgeMsg nodeMsg = (FudgeMsg) fieldValue;
      final Collection<FudgeField> inputFields = nodeMsg.getAllByName(INPUT_FIELD);
      final ValueSpecification[] inputValues = new ValueSpecification[inputFields.size()];
      final TempDependencyNode[] inputNodes = new TempDependencyNode[inputFields.size()];
      int i = 0;
      for (FudgeField inputField : inputFields) {
        final FudgeMsg inputMsg = (FudgeMsg) inputField.getValue();
        final TempDependencyNode inputNode = getNodeFromField(inputMsg.getByName(NODE_FIELD));
        final String valueName = inputMsg.getString(VALUE_NAME_FIELD);
        final ValueProperties properties = getValuePropertiesFromField(inputMsg.getByName(PROPERTIES_FIELD));
        final ValueSpecification value = MemoryUtils.instance(new ValueSpecification(valueName, inputNode.getTarget(), properties));
        inputNode.addOutputValue(value);
        inputValues[i] = value;
        inputNodes[i++] = inputNode;
      }
      final DependencyNodeFunction function = getFunctionFromMessage(nodeMsg);
      final ComputationTargetSpecification target = getTargetFromField(nodeMsg.getByName(TARGET_FIELD));
      final Collection<FudgeField> outputFields = nodeMsg.getAllByName(OUTPUT_FIELD);
      final ValueSpecification[] outputs;
      if (outputFields.isEmpty()) {
        outputs = NO_OUTPUTS;
      } else {
        outputs = new ValueSpecification[outputFields.size()];
        i = 0;
        for (FudgeField outputField : outputFields) {
          final FudgeMsg outputMsg = (FudgeMsg) outputField.getValue();
          final String valueName = outputMsg.getString(VALUE_NAME_FIELD);
          final ValueProperties properties = getValuePropertiesFromField(outputMsg.getByName(PROPERTIES_FIELD));
          outputs[i++] = MemoryUtils.instance(new ValueSpecification(valueName, target, properties));
        }
      }
      final TempDependencyNode node = new TempDependencyNode(function, target, outputs, inputValues, inputNodes);
      _nodes[nodeMsg.getInt(IDENTIFIER_FIELD)] = node;
      return node;
    }

    public DependencyGraph decode(final FudgeMsg msg) {
      final int size = msg.getInt(SIZE_FIELD);
      final String calcConfigName = msg.getString(CALCULATION_CONFIGURATION_NAME_FIELD);
      _nodes = new TempDependencyNode[size];
      Collection<FudgeField> fields = msg.getAllByName(NODE_FIELD);
      final TempDependencyNode[] roots = new TempDependencyNode[fields.size()];
      int i = 0;
      for (FudgeField field : fields) {
        roots[i++] = getNodeFromField(field);
      }
      fields = msg.getAllByName(TERMINAL_OUTPUT_FIELD);
      final Map<ValueSpecification, Set<ValueRequirement>> terminals = Maps.newHashMapWithExpectedSize(fields.size());
      for (FudgeField field : fields) {
        final FudgeMsg fieldMsg = (FudgeMsg) field.getValue();
        final String valueName = fieldMsg.getString(VALUE_NAME_FIELD);
        final ComputationTargetSpecification target = getTargetFromField(fieldMsg.getByName(TARGET_FIELD));
        final ValueProperties properties = getValuePropertiesFromField(fieldMsg.getByName(PROPERTIES_FIELD));
        final ValueSpecification valueSpec = MemoryUtils.instance(new ValueSpecification(valueName, target, properties));
        final Collection<FudgeField> requirementFields = fieldMsg.getAllByName(VALUE_REQUIREMENT_FIELD);
        if (requirementFields.size() == 1) {
          final ValueRequirement valueReq = MemoryUtils.instance(_deserializer.fieldValueToObject(ValueRequirement.class, requirementFields.iterator().next()));
          terminals.put(valueSpec, Collections.singleton(valueReq));
        } else {
          final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(requirementFields.size());
          for (FudgeField requirementField : requirementFields) {
            requirements.add(MemoryUtils.instance(_deserializer.fieldValueToObject(ValueRequirement.class, requirementField)));
          }
          terminals.put(valueSpec, requirements);
        }
      }
      // Allow the GC to get rid of the temporary data as soon as possible
      _nodes = null;
      final Collection<DependencyNode> rootNodes = new ArrayList<DependencyNode>(roots.length);
      for (i = 0; i < roots.length; i++) {
        final TempDependencyNode node = roots[i];
        // Allow the GC to get rid of the temporary data as soon as possible
        roots[i] = null;
        rootNodes.add(node.toNode());
      }
      return new DependencyGraphImpl(calcConfigName, rootNodes, size, terminals);
    }

  }

  @Override
  public DependencyGraph buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final MessageDecoder decoder = new MessageDecoder(deserializer);
    return decoder.decode(msg);
  }

}
