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
package org.kie.lc4j.efesto.runtime.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.EfestoAppRoot;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoLocalRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.kie.lc4j.efesto.api.identifiers.KieLC4JComponentRoot;
import org.kie.lc4j.efesto.api.identifiers.LC4JIdFactory;
import org.kie.lc4j.efesto.api.identifiers.LocalComponentIdLC4J;
import org.kie.lc4j.efesto.runtime.model.EfestoOutputLC4J;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"rawtypes", "unchecked"})
class KieRuntimeServiceLC4JMapInputTest {

    private static final String MODEL_NAME = "llama3.2";
    private static final String ENGINE_NAME = "ollama";
    private static KieRuntimeService kieRuntimeService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    private ModelLocalUriId modelLocalUriId;

    private EfestoInput<Map<String, List<String>>> inputLC4J;

    @BeforeAll
    public static void setup() {
        kieRuntimeService = new KieRuntimeServiceLC4JMapInput();
        memoryCompilerClassLoader =
                new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManageInput() {
        modelLocalUriId = getModelLocalUriIdFromLC4JIdFactory(ENGINE_NAME, MODEL_NAME);
        Map<String, List<String>> inputData = new HashMap<>();
        inputLC4J = new BaseEfestoInput<>(modelLocalUriId, inputData);
        assertThat(kieRuntimeService.canManageInput(inputLC4J,
                                                               getEfestoContext(memoryCompilerClassLoader))).isTrue();
    }

    @Test
    void evaluateInput() {
        modelLocalUriId = getModelLocalUriIdFromLC4JIdFactory(ENGINE_NAME, MODEL_NAME);
        Map<String, List<String>> inputData = Map.of("prompt", Collections.singletonList("Provide 3 short bullet points explaining why Java is awesome"));
        inputLC4J = new BaseEfestoInput<>(modelLocalUriId, inputData);
        EfestoLocalRuntimeContext efestoRuntimeContext = getEfestoContext(memoryCompilerClassLoader);
        Optional<EfestoOutputLC4J> retrieved = kieRuntimeService.evaluateInput(inputLC4J,
                                                                               efestoRuntimeContext);
        assertThat(retrieved).isNotNull().isPresent();
        List<String> chatResult = retrieved.get().getOutputData();
        assertThat(chatResult).isNotNull().hasSize(1);
        assertThat( chatResult.get(0)).contains("Here are three"); // Potentially flaky test
    }

    static LocalComponentIdLC4J getModelLocalUriIdFromLC4JIdFactory(String engineName, String modelName) {
        return new EfestoAppRoot()
                .get(KieLC4JComponentRoot.class)
                .get(LC4JIdFactory.class)
                .get(engineName, modelName);
    }

    static EfestoLocalRuntimeContext getEfestoContext(ClassLoader parenClassLoader) {
        return EfestoRuntimeContextUtils.buildWithParentClassLoader(parenClassLoader);
    }
}