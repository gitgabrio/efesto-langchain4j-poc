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
package org.kie.lc4j.efesto.compiler;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.EfestoAppRoot;
import org.kie.efesto.common.api.identifiers.LocalUri;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.model.EfestoCompilationContext;
import org.kie.efesto.common.api.model.GeneratedResources;
import org.kie.efesto.compilationmanager.api.model.EfestoFileSetResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.efesto.compilationmanager.api.utils.SPIUtils;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextUtils;
import org.kie.lc4j.efesto.api.identifiers.KieLC4JComponentRoot;
import org.kie.lc4j.efesto.api.identifiers.LC4JIdFactory;
import org.kie.lc4j.efesto.api.identifiers.LocalComponentIdLC4J;
import org.kie.lc4j.efesto.compiler.model.EfestoLC4JResource;
import org.kie.lc4j.efesto.compiler.model.EngineModelDefinition;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.lc4j.efesto.compiler.service.KieCompilerServiceLC4JOllama.OLLAMA;

class CompilationTest {

    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;
    private static CompilationManager compilationManager;

    private static final String MODEL_NAME = "llama3.2";
    private static final String ENGINE_NAME = "ollama";

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
        compilationManager =
                SPIUtils.getCompilationManager(true).orElseThrow(() -> new RuntimeException("Compilation Manager not " +
                                                                                                    "available"));
    }

    @Test
    void generateAndDumpSources() {
        EfestoResource toProcessLc4j = new EfestoLC4JResource(new EngineModelDefinition(ENGINE_NAME, MODEL_NAME));
        EfestoCompilationContext compilationContext = EfestoCompilationContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
        compilationManager.processResource(compilationContext, toProcessLc4j);
        Map<String, GeneratedResources> generatedResourcesMap = compilationContext.getGeneratedResourcesMap();
        assertThat(generatedResourcesMap).isNotNull().hasSize(1);
    }

    static LocalComponentIdLC4J getModelLocalUriIdFromLC4JIdFactory(String engineName, String modelName) {
        return new EfestoAppRoot()
                .get(KieLC4JComponentRoot.class)
                .get(LC4JIdFactory.class)
                .get(engineName, modelName);
    }

}