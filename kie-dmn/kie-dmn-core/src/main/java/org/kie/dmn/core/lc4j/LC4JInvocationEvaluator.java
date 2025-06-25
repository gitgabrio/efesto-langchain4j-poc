/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.dmn.core.lc4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.api.core.EvaluatorResult;
import org.kie.dmn.api.core.event.DMNRuntimeEventManager;
import org.kie.dmn.core.api.DMNExpressionEvaluator;
import org.kie.dmn.core.ast.DMNFunctionDefinitionEvaluator;
import org.kie.dmn.core.ast.DMNFunctionDefinitionEvaluator.FormalParameter;
import org.kie.dmn.core.ast.EvaluatorResultImpl;
import org.kie.dmn.model.api.DMNElement;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.model.EfestoCompilationContext;
import org.kie.efesto.common.core.storage.ContextStorage;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.efesto.compilationmanager.api.utils.SPIUtils;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextUtils;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoLocalRuntimeContext;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.RuntimeManager;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.kie.lc4j.efesto.compiler.model.EfestoLC4JResource;
import org.kie.lc4j.efesto.compiler.model.EngineModelDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LC4JInvocationEvaluator implements DMNExpressionEvaluator {

    private static final CompilationManager compilationManager =
            SPIUtils.getCompilationManager(true).orElseThrow(() -> new RuntimeException("Compilation Manager not " +
                                                                                                "available"));
    private static final RuntimeManager runtimeManager =
            org.kie.efesto.runtimemanager.api.utils.SPIUtils.getRuntimeManager(false).orElseThrow(() -> new EfestoRuntimeManagerException("Failed to find an instance of RuntimeManager: please check classpath and dependencies"));

    private static final Logger LOG = LoggerFactory.getLogger(LC4JInvocationEvaluator.class);
    private static final String CHECK_CLASSPATH = "check classpath and dependencies!";

    protected final String dmnNS;
    protected final DMNElement node;
    protected final List<FormalParameter> parameters = new ArrayList<>();
    protected final ModelLocalUriId lc4jModelLocalUriID;
    protected final String lc4jEngine;
    protected final String lc4jModelName;

    public LC4JInvocationEvaluator(String dmnNS, DMNElement node, ModelLocalUriId lc4jModelLocalUriID,
                                   String lc4jEngine, String lc4jModelName) {
        this.dmnNS = dmnNS;
        this.node = node;
        this.lc4jModelLocalUriID = lc4jModelLocalUriID;
        this.lc4jEngine = lc4jEngine;
        this.lc4jModelName = lc4jModelName;
    }

    public void addParameter(String name, DMNType dmnType) {
        this.parameters.add(new FormalParameter(name, dmnType));
    }

    @Override
    public EvaluatorResult evaluate(DMNRuntimeEventManager eventManager, DMNResult result) {
        Map<String, List<String>> inputData = getLC4JRequestData(result);
        EfestoInput<Map<String, List<String>>> inputLC4J = new BaseEfestoInput<>(lc4jModelLocalUriID, inputData);
        ClassLoader parentClassloader = eventManager.getRuntime().getRootClassLoader();
        EfestoCompilationContext efestoCompilationContext = getEfestoCompilationContext(lc4jModelLocalUriID,
                                                                                        lc4jEngine, lc4jModelName,
                                                                                        parentClassloader);

        EfestoLocalRuntimeContext runtimeContext = EfestoRuntimeContextUtils.buildWithParentClassLoader(parentClassloader,
                                                                           efestoCompilationContext.getGeneratedResourcesMap());
        Map<String, Object> mapResult = evaluate(runtimeContext, inputLC4J);
        Object coercedResult = mapResult.size() > 1 ? mapResult : mapResult.values().iterator().next();
        return new EvaluatorResultImpl(coercedResult, EvaluatorResult.ResultType.SUCCESS);
    }

    protected Map<String, Object> evaluate(EfestoLocalRuntimeContext runtimeContext, EfestoInput<Map<String, List<String>>> inputLC4J) {
        Collection<EfestoOutput> retrieved = evaluateInput(inputLC4J, runtimeContext);
        if (retrieved.isEmpty()) {
            String errorMessage = String.format("Failed to get result for %s: please %s",
                                                inputLC4J.getModelLocalUriId(),
                                                CHECK_CLASSPATH);
            LOG.error(errorMessage);
            throw new KieRuntimeServiceException(errorMessage);
        }
        return Map.of("chat", retrieved.iterator().next().getOutputData());
    }

    /**
     * This method retrieves the previously built <code>EfestoCompilationContext</code> for the given
     * <code>ModelLocalUriId</code> or, eventually, recompile it from scratch
     * @param lc4jModelLocalUriID
     * @param parentClassloader
     * @return
     */
    private EfestoCompilationContext getEfestoCompilationContext(ModelLocalUriId lc4jModelLocalUriID,
                                                                 String lc4jEngine, String lc4jModelName,
                                                                 ClassLoader parentClassloader) {
        EfestoCompilationContext toReturn = ContextStorage.getEfestoCompilationContext(lc4jModelLocalUriID);
        if (toReturn == null) {
            toReturn = EfestoCompilationContextUtils.buildWithParentClassLoader(parentClassloader);
            EfestoResource toProcessLc4j = new EfestoLC4JResource(new EngineModelDefinition(lc4jEngine, lc4jModelName));
            compilationManager.processResource(toReturn, toProcessLc4j);
            ContextStorage.putEfestoCompilationContext(lc4jModelLocalUriID, toReturn);
            toReturn = ContextStorage.getEfestoCompilationContext(lc4jModelLocalUriID);
        }
        return toReturn;
    }

    private Collection<EfestoOutput> evaluateInput(EfestoInput<Map<String, List<String>>> inputLC4J,
                                                   EfestoLocalRuntimeContext runtimeContext) {
        try {
            return runtimeManager.evaluateInput(runtimeContext, inputLC4J);
        } catch (Exception t) {
            String errorMessage = String.format("Evaluation error for %s using %s due to %s: please %s",
                                                inputLC4J.getModelLocalUriId(),
                                                inputLC4J,
                                                t.getMessage(),
                                                CHECK_CLASSPATH);
            LOG.error(errorMessage);
            throw new KieRuntimeServiceException(errorMessage, t);
        }
    }

    private Map<String, List<String>> getLC4JRequestData(DMNResult dmnr) {
        Map<String, List<String>> toReturn = new HashMap<>();
        List<String> chats = new ArrayList<>();
        for (DMNFunctionDefinitionEvaluator.FormalParameter p : parameters) {
            String pValue = getValueForLC4JInput(dmnr, p.name);
            chats.add(pValue);
        }
        toReturn.put("chat", chats);
        return toReturn;
    }

    private String getValueForLC4JInput(DMNResult r, String name) {
        Object pValue = r.getContext().get(name);
        return pValue != null ? pValue.toString() : null;
    }
}
