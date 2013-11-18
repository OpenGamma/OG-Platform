/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.functions;

import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.dsl.FunctionInput;
import com.opengamma.engine.function.dsl.FunctionOutput;
import com.opengamma.engine.function.dsl.FunctionSignature;
import com.opengamma.engine.function.dsl.TargetSpecificationReference;
import com.opengamma.engine.function.dsl.properties.RecordingValueProperties;
import com.opengamma.engine.function.dsl.properties.ValuePropertiesModifier;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.lambdava.functions.Function2;
import com.opengamma.lambdava.streams.Stream;
import com.opengamma.lambdava.streams.StreamI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

public abstract class BaseNonCompiledInvoker extends AbstractFunction.NonCompiledInvoker {

  private FunctionSignature _functionSignature;

  private static Pair<Map<String, StreamI<FunctionInput>>, Map<String, StreamI<FunctionOutput>>> ioputsByName(FunctionSignature signature) {
    Map<String, StreamI<FunctionInput>> inputsByName = signature.getInputs().reduce(new HashMap<String, StreamI<FunctionInput>>(), new Function2<HashMap<String, StreamI<FunctionInput>>, FunctionInput, HashMap<String, StreamI<FunctionInput>>>() {
      @Override
      public HashMap<String, StreamI<FunctionInput>> execute(HashMap<String, StreamI<FunctionInput>> acc, FunctionInput functionInput) {
        String name = functionInput.getName();
        if (name == null) {
          throw new IllegalArgumentException("Input's name must be provided");
        }
        StreamI<FunctionInput> inputs = acc.get(name);
        if (inputs == null) {
          inputs = Stream.empty();
          acc.put(name, inputs);
        }
        acc.put(name, inputs.cons(functionInput));
        return acc;
      }
    });

    Map<String, StreamI<FunctionOutput>> outputsByName = signature.getOutputs().reduce(new HashMap<String, StreamI<FunctionOutput>>(), new Function2<HashMap<String, StreamI<FunctionOutput>>, FunctionOutput, HashMap<String, StreamI<FunctionOutput>>>() {
      @Override
      public HashMap<String, StreamI<FunctionOutput>> execute(HashMap<String, StreamI<FunctionOutput>> acc, FunctionOutput functionOutput) {
        String name = functionOutput.getName();
        if (name == null) {
          throw new IllegalArgumentException("Output's name must be provided");
        }
        StreamI<FunctionOutput> outputs = acc.get(name);
        if (outputs == null) {
          outputs = Stream.empty();
        }
        acc.put(name, outputs.cons(functionOutput));
        return acc;
      }
    });
    return Pairs.of(inputsByName, outputsByName);
  }

  protected abstract FunctionSignature functionSignature();

  private FunctionSignature getFunctionSignature() {
    if (_functionSignature == null) {
      _functionSignature = functionSignature();
    }
    return _functionSignature;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return getFunctionSignature().getComputationTargetType();
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {

    FunctionSignature signature = getFunctionSignature();
    Pair<Map<String, StreamI<FunctionInput>>, Map<String, StreamI<FunctionOutput>>> ioputsByName = ioputsByName(signature);

    Map<String, StreamI<FunctionInput>> inputsByName = ioputsByName.getFirst();
    Map<String, StreamI<FunctionOutput>> outputsByName = ioputsByName.getSecond();

    Set<ValueSpecification> valueSpecifications = new HashSet<>();

    for (String name : outputsByName.keySet()) {
      StreamI<FunctionOutput> functionOutputs = outputsByName.get(name);
      for (FunctionOutput functionOutput : functionOutputs) {
        TargetSpecificationReference tsr = functionOutput.getTargetSpecificationReference();
        ComputationTargetSpecification cts = functionOutput.getComputationTargetSpecification();
        RecordingValueProperties rvps = functionOutput.getRecordingValueProperties();
        ValueProperties vps = functionOutput.getValueProperties();
        if (tsr == null && cts == null) {
          throw new IllegalArgumentException("Target specification must be provided by function DSL, but there wasn't any for input: " + name);
        }
        if (rvps == null && vps == null) {
          throw new IllegalArgumentException("Value properties must be provided by function DSL, but there wasn't any for input: " + name);
        }

        ComputationTargetSpecification computationTargetSpecification;
        if (cts != null) {
          computationTargetSpecification = cts;
        } else {
          computationTargetSpecification = target.toSpecification();
        }
        ValueProperties valueProperties;
        if (vps != null) {
          valueProperties = vps;
        } else {
          valueProperties = ValueProperties.all();
        }
        ValueSpecification valueSpecification = new ValueSpecification(name, computationTargetSpecification, valueProperties);
        valueSpecifications.add(valueSpecification);
      }
    }
    return valueSpecifications;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    FunctionSignature signature = getFunctionSignature();
    Pair<Map<String, StreamI<FunctionInput>>, Map<String, StreamI<FunctionOutput>>> ioputsByName = ioputsByName(signature);

    Map<String, StreamI<FunctionInput>> inputsByName = ioputsByName.getFirst();
    Map<String, StreamI<FunctionOutput>> outputsByName = ioputsByName.getSecond();

    Set<ValueRequirement> valueRequirements = new HashSet<ValueRequirement>();

    for (String name : inputsByName.keySet()) {
      StreamI<FunctionInput> functionInputs = inputsByName.get(name);
      for (FunctionInput functionInput : functionInputs) {
        TargetSpecificationReference tsr = functionInput.getTargetSpecificationReference();
        ComputationTargetSpecification cts = functionInput.getComputationTargetSpecification();
        RecordingValueProperties rvps = functionInput.getRecordingValueProperties();
        ValueProperties vps = functionInput.getValueProperties();
        if (tsr == null && cts == null) {
          throw new IllegalArgumentException("Target specification must be provided by function DSL, but there wasn't any for input: " + name);
        }
        if (rvps == null && vps == null) {
          throw new IllegalArgumentException("Value properties must be provided by function DSL, but there wasn't any for input: " + name);
        }

        ComputationTargetSpecification computationTargetSpecification;
        if (cts != null) {
          computationTargetSpecification = cts;
        } else {
          computationTargetSpecification = target.toSpecification();
        }
        ValueProperties valueProperties;
        if (vps != null) {
          valueProperties = vps;
        } else {
          ValueProperties.Builder copiedValueProperties = desiredValue.getConstraints().copy();
          StreamI<ValuePropertiesModifier> recorderValueProperties = rvps.getRecordedValueProperties();
          ValueProperties.Builder valuePropertiesBuilder = recorderValueProperties.reduce(copiedValueProperties, new Function2<ValueProperties.Builder, ValuePropertiesModifier, ValueProperties.Builder>() {
            @Override
            public ValueProperties.Builder execute(ValueProperties.Builder builder, ValuePropertiesModifier modifier) {
              return modifier.modify(builder);
            }
          });
          valueProperties = valuePropertiesBuilder.get();
        }
        ValueRequirement valueRequirement = new ValueRequirement(name, computationTargetSpecification, valueProperties);
        valueRequirements.add(valueRequirement);
      }
    }
    return valueRequirements;

  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputSpecificationsMap) {
    FunctionSignature signature = getFunctionSignature();
    Pair<Map<String, StreamI<FunctionInput>>, Map<String, StreamI<FunctionOutput>>> ioputsByName = ioputsByName(signature);

    Map<String, StreamI<FunctionInput>> inputsByName = ioputsByName.getFirst();
    Map<String, StreamI<FunctionOutput>> outputsByName = ioputsByName.getSecond();

    Set<ValueSpecification> valueSpecifications = new HashSet<>();

    for (final String name : outputsByName.keySet()) {
      StreamI<FunctionOutput> functionOutputs = outputsByName.get(name);
      for (FunctionOutput functionOutput : functionOutputs) {
        TargetSpecificationReference tsr = functionOutput.getTargetSpecificationReference();
        ComputationTargetSpecification cts = functionOutput.getComputationTargetSpecification();
        final RecordingValueProperties rvps = functionOutput.getRecordingValueProperties();
        ValueProperties vps = functionOutput.getValueProperties();
        if (tsr == null && cts == null) {
          throw new IllegalArgumentException("Target specification must be provided by function DSL, but there wasn't any for input: " + name);
        }
        if (rvps == null && vps == null) {
          throw new IllegalArgumentException("Value properties must be provided by function DSL, but there wasn't any for input: " + name);
        }

        final ComputationTargetSpecification computationTargetSpecification;
        if (cts != null) {
          computationTargetSpecification = cts;
        } else {
          computationTargetSpecification = target.toSpecification();
        }
        ValueProperties valueProperties;
        if (vps != null) {
          valueProperties = vps;
        } else {
          //FunctionInput copyFrom = inputsByName.get(rvps.getCopiedFrom()).first();

          //Find the apropierate valueSpecifications

          ValueSpecification copyFrom = functional(inputSpecificationsMap.keySet()).filter(new Function1<ValueSpecification, Boolean>() {
            @Override
            public Boolean execute(ValueSpecification valueSpecification) {
              return valueSpecification.getValueName().equals(rvps.getCopiedFrom());
              //&& valueSpecification.getTargetSpecification().equals(computationTargetSpecification) && valueSpecification.getProperties().isSatisfiedBy()
            }
          }).first();

          ValueProperties.Builder builder;
          if (copyFrom != null) {
            builder = copyFrom.getProperties().copy();
          } else {
            builder = ValueProperties.all().copy();
          }
          StreamI<ValuePropertiesModifier> recorderValueProperties = rvps.getRecordedValueProperties();
          ValueProperties.Builder valuePropertiesBuilder = recorderValueProperties.reduce(builder, new Function2<ValueProperties.Builder, ValuePropertiesModifier, ValueProperties.Builder>() {
            @Override
            public ValueProperties.Builder execute(ValueProperties.Builder builder, ValuePropertiesModifier modifier) {
              return modifier.modify(builder);
            }
          });
          valueProperties = valuePropertiesBuilder.get();
        }
        ValueSpecification valueSpecification = new ValueSpecification(name, computationTargetSpecification, valueProperties);
        valueSpecifications.add(valueSpecification);
      }
    }
    return valueSpecifications;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    FunctionSignature signature = getFunctionSignature();
    ComputationTargetType ctt = signature.getComputationTargetType();
    Class ctc = signature.getComputationTargetClass();
    if (ctt != null && !ctt.isCompatible(target.getType())) {
      return false;
    }
    if (ctc != null && !ctc.isAssignableFrom(target.getValue().getClass())) {
      return false;
    }
    return true;
  }
}
