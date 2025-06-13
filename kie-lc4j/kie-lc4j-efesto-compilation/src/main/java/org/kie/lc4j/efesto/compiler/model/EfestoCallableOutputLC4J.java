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
package org.kie.lc4j.efesto.compiler.model;

import dev.langchain4j.model.chat.ChatModel;
import org.kie.lc4j.efesto.api.identifiers.KieLC4JComponentRoot;
import org.kie.lc4j.efesto.api.identifiers.LC4JIdFactory;
import org.kie.efesto.common.api.identifiers.EfestoAppRoot;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.compilationmanager.api.model.EfestoCallableOutputModelContainer;

import java.util.Collections;
import java.util.List;

public class EfestoCallableOutputLC4J extends EfestoCallableOutputModelContainer<ChatModel> {

    public EfestoCallableOutputLC4J(String engineName, String modelName, ChatModel chatModel) {
        super(new EfestoAppRoot()
                .get(KieLC4JComponentRoot.class)
                .get(LC4JIdFactory.class)
                .get(engineName, modelName),
              null,
              chatModel);
    }

    public EfestoCallableOutputLC4J(ModelLocalUriId modelLocalUriId, ChatModel chatModel) {
        super(modelLocalUriId,
              null,
              chatModel);
    }

    @Override
    public List<String> getFullClassNames() {
        return Collections.emptyList();
    }

    @Override
    public String getModelSource() {
        return "";
    }
}
