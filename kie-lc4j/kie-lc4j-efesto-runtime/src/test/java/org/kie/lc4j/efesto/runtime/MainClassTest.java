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
package org.kie.lc4j.efesto.runtime;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Test;

import static org.kie.lc4j.efesto.runtime.MainClass.MODEL_NAME;
import static org.kie.lc4j.efesto.runtime.MainClass.OLLAMA_BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;

class MainClassTest {

    @Test
    void mainTest() {
        System.out.println( "mainTest" );
        ChatModel chatModel = OllamaChatModel.builder()
                .baseUrl(OLLAMA_BASE_URL)
                .modelName(MODEL_NAME)
                .logRequests(true)
                .build();
        System.out.println( "chatModel " + chatModel );

        String answer = chatModel.chat("Provide 3 short bullet points explaining why Java is awesome");
        System.out.println(answer);

        assertThat(answer).isNotBlank();
    }
}