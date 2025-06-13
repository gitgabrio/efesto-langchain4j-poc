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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.kie.efesto.common.api.identifiers.EfestoAppRoot;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.lc4j.efesto.api.identifiers.KieLC4JComponentRoot;
import org.kie.lc4j.efesto.api.identifiers.LC4JIdFactory;
import org.kie.lc4j.efesto.api.identifiers.LocalComponentIdLC4J;
import org.kie.efesto.common.api.model.EfestoCompilationContext;
import org.kie.efesto.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilerService;
import org.kie.lc4j.efesto.compiler.factories.KieLC4JOllamaChatModelFactory;
import org.kie.lc4j.efesto.compiler.model.EfestoCallableOutputLC4JClassesContainer;
import org.kie.lc4j.efesto.compiler.model.EfestoLC4JResource;

@SuppressWarnings({"rawtypes", "unchecked"})
public class KieCompilerServiceLC4JOllama implements KieCompilerService<EfestoCompilationOutput, EfestoCompilationContext> {

    public static final String OLLAMA = "ollama";

    @Override
    public boolean canManageResource(EfestoResource toProcess) {
        return toProcess instanceof EfestoLC4JResource && ((EfestoLC4JResource) toProcess).getContent().getEngineName().equalsIgnoreCase(OLLAMA);
    }

    @Override
    public List<EfestoCompilationOutput> processResource(EfestoResource toProcess, EfestoCompilationContext context) {
        if (!canManageResource(toProcess)) {
            throw new KieCompilerServiceException(String.format("%s can not process %s",
                                                                this.getClass().getName(),
                                                                toProcess.getClass().getName()));
        }
        EfestoLC4JResource  resource = (EfestoLC4JResource) toProcess;
        return getEfestoCompilationOutputLC4J(resource.getContent().getModelName(), context);
    }

    @Override
    public String getModelType() {
        return LocalComponentIdLC4J.class.getName();
    }


    static List<EfestoCompilationOutput> getEfestoCompilationOutputLC4J(String modelName, EfestoCompilationContext context) {
        Map<String, String> allSourcesMap = KieLC4JOllamaChatModelFactory.getKieLC4JOllamaChatModelSourcesMap(modelName);
        Map<String, byte[]> compiledClasses = context.compileClasses(allSourcesMap);
        String fullResourceClassName = allSourcesMap.keySet().iterator().next();
        ModelLocalUriId modelLocalUriId = getModelLocalUriIdFromLC4JIdFactory(modelName);
        return Collections.singletonList(new EfestoCallableOutputLC4JClassesContainer(modelLocalUriId, fullResourceClassName,
                                                                      compiledClasses));
    }

    static LocalComponentIdLC4J getModelLocalUriIdFromLC4JIdFactory(String modelName) {
        return new EfestoAppRoot()
                .get(KieLC4JComponentRoot.class)
                .get(LC4JIdFactory.class)
                .get("ollama", modelName);
    }








}