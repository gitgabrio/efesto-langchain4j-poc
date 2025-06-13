/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.lc4j.efesto.runtime.utils;

import dev.langchain4j.model.chat.ChatModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.model.GeneratedExecutableResource;
import org.kie.efesto.common.api.model.GeneratedModelResource;
import org.kie.efesto.common.utils.PackageClassNameUtils;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoLocalRuntimeContext;
import org.kie.lc4j.efesto.runtime.model.EfestoOutputLC4J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.fail;
import static org.kie.efesto.runtimemanager.api.utils.GeneratedResourceUtils.getGeneratedExecutableResource;
import static org.kie.efesto.runtimemanager.api.utils.GeneratedResourceUtils.isPresentExecutableOrModelOrRedirect;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Lc4jRuntimeHelper {

    static final String KIE_LC4J_OLLAMA_CHATMODEL_CLASS = "KieLC4JOllamaChatModel";
    static final String KIE_LC4J_OLLAMA_CHATMODEL_CLASS_TEMPLATE = KIE_LC4J_OLLAMA_CHATMODEL_CLASS + "_%s";

    private static final Logger logger = LoggerFactory.getLogger(Lc4jRuntimeHelper.class.getName());

    private Lc4jRuntimeHelper() {
    }

    public static boolean canManage(EfestoInput toEvaluate, EfestoLocalRuntimeContext context) {
        return isPresentExecutableOrModelOrRedirect(toEvaluate.getModelLocalUriId(), context);
    }

    public static Optional<EfestoOutputLC4J> execute(EfestoInput<Map<String, List<String>>> toEvaluate, EfestoLocalRuntimeContext runtimeContext) {
        ModelLocalUriId modelLocalUriId = toEvaluate.getModelLocalUriId();
        Optional<GeneratedExecutableResource> generatedModelResource = getGeneratedExecutableResource(modelLocalUriId, runtimeContext.getGeneratedResourcesMap());
        return generatedModelResource.map(it -> execute(modelLocalUriId, toEvaluate.getInputData(), it.getFullClassNames().get(0), runtimeContext));
    }

    static EfestoOutputLC4J execute(ModelLocalUriId modelLocalUriId, Map<String, List<String>> inputData, String fullClassName, EfestoLocalRuntimeContext runtimeContext) {
        try {
            Class<? extends ChatModel> chatModelClass = runtimeContext.loadClass(fullClassName);
            ChatModel chatModel = chatModelClass.getDeclaredConstructor().newInstance();
            return execute(chatModel, modelLocalUriId, inputData);
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }

    static EfestoOutputLC4J execute(ChatModel model, ModelLocalUriId modelLocalUriId, Map<String, List<String>> inputData) {
        List<String> toChat = inputData.get("chat");

        List<String> output =  new ArrayList<>();
        for (String chat : toChat) {
            output.add(executeChat(model, chat));
        }
        return new EfestoOutputLC4J(modelLocalUriId, output);
    }

    static String executeChat(ChatModel chatModel, String chat) {
        return chatModel.chat(chat);
    }

}
