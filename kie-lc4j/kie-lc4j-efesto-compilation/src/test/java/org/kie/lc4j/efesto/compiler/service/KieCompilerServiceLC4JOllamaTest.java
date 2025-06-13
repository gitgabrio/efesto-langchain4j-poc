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
package org.kie.lc4j.efesto.compiler.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.model.EfestoCompilationContext;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilerService;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextImpl;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextUtils;
import org.kie.lc4j.efesto.compiler.model.EfestoCallableOutputLC4JClassesContainer;
import org.kie.lc4j.efesto.compiler.model.EfestoLC4JResource;
import org.kie.lc4j.efesto.compiler.model.EngineModelDefinition;
import org.kie.lc4j.efesto.compiler.model.Lc4jCompilationContextImpl;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.kie.lc4j.efesto.compiler.service.KieCompilerServiceLC4JOllama.OLLAMA;

@SuppressWarnings({"rawtypes", "unchecked"})
class KieCompilerServiceLC4JOllamaTest {

    private static KieCompilerService kieCompilationService;

    @BeforeAll
    static void setUp() {
        kieCompilationService = new KieCompilerServiceLC4JOllama();
    }

    @Test
    void canManageResource() {
        String modelName = "modelName";
        EfestoResource toProcess = new EfestoLC4JResource(new EngineModelDefinition(OLLAMA, modelName));
        assertTrue(kieCompilationService.canManageResource(toProcess));
        toProcess = new EfestoLC4JResource(new EngineModelDefinition("NOT_OLLAMA", modelName));
        assertFalse(kieCompilationService.canManageResource(toProcess));
        toProcess = new EfestoFileResource(new File("somefile"));
        assertFalse(kieCompilationService.canManageResource(toProcess));
    }

    @Test
    void processResource() {
        String modelName = "modelName";
        EfestoResource toProcess = new EfestoLC4JResource(new EngineModelDefinition(OLLAMA, modelName));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(classLoader);
        EfestoCompilationContext contex = new Lc4jCompilationContextImpl(memoryClassLoader);
        List<EfestoCompilationOutput> retrieved = kieCompilationService.processResource(toProcess,
                                                                                        contex);
        assertThat(retrieved).isNotNull().hasSize(1);
        EfestoCompilationOutput efestoCompilationOutput = retrieved.get(0);
        assertThat(efestoCompilationOutput).isNotNull().isInstanceOf(EfestoCallableOutputLC4JClassesContainer.class);
        Map<String, byte[]> compiledClasses =
                ((EfestoCallableOutputLC4JClassesContainer) efestoCompilationOutput).getCompiledClassesMap();
        String expectedKey = String.format("org.kie.lc4j.efesto.runtime.KieLC4JOllamaChatModel%s", modelName);
        assertThat(compiledClasses).hasSize(1).containsOnlyKeys(expectedKey);
        try {
            contex.loadClasses(compiledClasses);
            Class<? extends ChatModel> chatModelClass = memoryClassLoader.loadClass(expectedKey).asSubclass(ChatModel.class);
            ChatModel chatModel = chatModelClass.getDeclaredConstructor().newInstance();
            assertThat(chatModel).isNotNull();
            assertThat(chatModel).isNotNull().isInstanceOf(OllamaChatModel.class);
            assertThat(chatModel.defaultRequestParameters().modelName()).isEqualTo(modelName);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getModelType() {

    }

    @Test
    void getEfestoCompilationOutputLC4J() {
    }
}