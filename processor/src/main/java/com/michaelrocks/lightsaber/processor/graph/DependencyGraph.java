/*
 * Copyright 2015 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michaelrocks.lightsaber.processor.graph;

import com.michaelrocks.lightsaber.processor.MethodDescriptor;
import com.michaelrocks.lightsaber.processor.ProcessingException;
import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.analysis.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.analysis.ModuleDescriptor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyGraph {
    private final Map<Type, List<Type>> typeGraph;

    public DependencyGraph(final ProcessorContext processorContext) {
        typeGraph = new DependencyGraphBuilder(processorContext).build();
    }

    public Collection<Type> getTypes() {
        return typeGraph.keySet();
    }

    public Collection<Type> getTypeDependencies(final Type type) {
        return typeGraph.get(type);
    }

    public Collection<Type> getUnresolvedDependencies() {
        final UnresolvedDependenciesSearcher searcher = new UnresolvedDependenciesSearcher(typeGraph);
        return searcher.findUnresolvedDependencies();
    }

    public Collection<Type> getCycles() {
        final CycleSearcher searcher = new CycleSearcher(typeGraph);
        return searcher.findCycles();
    }

    private static final class DependencyGraphBuilder {
        private final ProcessorContext processorContext;
        private final Map<Type, List<Type>> typeGraph = new HashMap<>();

        private DependencyGraphBuilder(final ProcessorContext processorContext) {
            this.processorContext = processorContext;
        }

        Map<Type, List<Type>> build() {
            for (final ModuleDescriptor module : processorContext.getModules()) {
                for (final MethodDescriptor providerMethod : module.getProviderMethods()) {
                    final Type returnType = providerMethod.getType().getReturnType();
                    addProviderMethodToGraph(returnType, providerMethod);
                }
            }

            for (final InjectionTargetDescriptor providableTarget : processorContext.getProvidableTargets()) {
                final MethodDescriptor constructor = providableTarget.getInjectableConstructors().get(0);
                addProviderMethodToGraph(providableTarget.getTargetType(), constructor);
            }

            return typeGraph;
        }

        private void addProviderMethodToGraph(final Type type, final MethodDescriptor providerMethod) {
            if (typeGraph.containsKey(type)) {
                processorContext.reportError(
                        new ProcessingException("Type has multiple providers: " + type));
            }

            if (providerMethod.isDefaultConstructor()) {
                typeGraph.put(type, Collections.<Type>emptyList());
            } else {
                final List<Type> argumentTypes = Arrays.asList(providerMethod.getType().getArgumentTypes());
                typeGraph.put(type, argumentTypes);
            }
        }
    }

    private static final class UnresolvedDependenciesSearcher {
        private final Map<Type, List<Type>> graph;
        private final Set<Type> visitedTypes = new HashSet<>();
        private final List<Type> unresolvedTypes = new ArrayList<>();

        private UnresolvedDependenciesSearcher(final Map<Type, List<Type>> graph) {
            this.graph = graph;
        }

        Collection<Type> findUnresolvedDependencies() {
            for (final Type type : graph.keySet()) {
                traverse(type);
            }
            return Collections.unmodifiableList(unresolvedTypes);
        }

        private void traverse(final Type type) {
            if (visitedTypes.add(type)) {
                final List<Type> dependencies = graph.get(type);
                if (dependencies == null) {
                    unresolvedTypes.add(type);
                } else {
                    for (final Type dependency : dependencies) {
                        traverse(dependency);
                    }
                }
            }
        }
    }


    private static final class CycleSearcher {
        private final Map<Type, List<Type>> graph;
        private final Map<Type, VertexColor> colors = new HashMap<>();
        private final Set<Type> cycles = new HashSet<>();

        private CycleSearcher(final Map<Type, List<Type>> graph) {
            this.graph = graph;
        }

        Collection<Type> findCycles() {
            for (final Type type : graph.keySet()) {
                traverse(type);
            }
            return Collections.unmodifiableSet(cycles);
        }

        private void traverse(final Type type) {
            final VertexColor color = colors.get(type);
            if (color == VertexColor.BLACK) {
                return;
            }

            if (color == VertexColor.GRAY) {
                cycles.add(type);
                return;
            }

            colors.put(type, VertexColor.GRAY);
            final List<Type> dependencies = graph.get(type);
            if (dependencies != null) {
                for (final Type dependency : dependencies) {
                    traverse(dependency);
                }
            }
            colors.put(type, VertexColor.BLACK);
        }

        private enum VertexColor {
            GRAY, BLACK
        }
    }
}
