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
package org.kie.lc4j.efesto.compiler.factories;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.util.Map;
import org.kie.lc4j.efesto.api.exceptions.KieLC4JException;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.utils.PackageClassNameUtils;
import org.kie.lc4j.efesto.compiler.utils.JavaParserUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.kie.lc4j.efesto.compiler.factories.KieLC4JOllamaChatModelFactory.KIE_LC4J_OLLAMA_CHATMODEL_CLASS_TEMPLATE;
import static org.kie.lc4j.efesto.compiler.factories.KieLC4JOllamaChatModelFactory.KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE;
import static org.kie.lc4j.efesto.compiler.factories.KieLC4JOllamaChatModelFactory.KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE_JAVA;
import static org.kie.lc4j.efesto.compiler.utils.JavaParserUtils.MAIN_CLASS_NOT_FOUND;

class KieLC4JOllamaChatModelFactoryTest {

    @Test
    void getKieLC4JOllamaChatModelClass() {
        String modelName = "modelName";
        ChatModel retrieved = KieLC4JOllamaChatModelFactory.getKieLC4JOllamaChatModelClass(modelName);
        assertThat(retrieved).isNotNull().isInstanceOf(OllamaChatModel.class);
        assertThat(retrieved.defaultRequestParameters().modelName()).isEqualTo(modelName);
    }

    @Test
    void getKieLC4JOllamaChatModelSourcesMap() {
        String modelName = "modelName";
        Map<String, String> retrieved  = KieLC4JOllamaChatModelFactory.getKieLC4JOllamaChatModelSourcesMap(modelName);
        String expectedKey = "org.kie.lc4j.efesto.runtime." + PackageClassNameUtils.getSanitizedClassName(String.format(KIE_LC4J_OLLAMA_CHATMODEL_CLASS_TEMPLATE, modelName));
        assertThat(retrieved).isNotNull().isNotEmpty().containsOnlyKeys(expectedKey);
        assertThat(retrieved.get(expectedKey)).isNotNull().isNotEmpty();
    }

    @Test
    void setConstructorName() {
        String className = "KieLC4JOllamaChatModel";
        CompilationUnit cloneCU = JavaParserUtils.getKiePMMLModelCompilationUnit(className,
                                                                                 "",
                                                                                 KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE_JAVA, KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE);
        ClassOrInterfaceDeclaration modelTemplate = cloneCU.getClassByName(className)
                .orElseThrow(() -> new KieLC4JException(MAIN_CLASS_NOT_FOUND + ": " + className));
        ConstructorDeclaration toCheck = modelTemplate.getDefaultConstructor().orElseThrow(() -> new RuntimeException("Constructor not found"));
        assertThat(toCheck).isNotNull();
        assertThat(toCheck.getName().asString()).isEqualTo(KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE);
        KieLC4JOllamaChatModelFactory.setConstructorName(modelTemplate, className);
        assertThat(toCheck).isNotNull();
        assertThat(toCheck.getName().asString()).isEqualTo(className);
    }

    @Test
    void setModelName() {
        String className = "KieLC4JOllamaChatModel";
        CompilationUnit cloneCU = JavaParserUtils.getKiePMMLModelCompilationUnit(className,
                                                                                 "",
                                                                                 KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE_JAVA, KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE);
        ClassOrInterfaceDeclaration modelTemplate = cloneCU.getClassByName(className)
                .orElseThrow(() -> new KieLC4JException(MAIN_CLASS_NOT_FOUND + ": " + className));
        final VariableDeclarator toCheck = modelTemplate.getFields().stream()
                .filter(field -> field.hasModifier(Modifier.Keyword.STATIC) && field.getVariables().size() ==1)
                .map(fieldDeclaration -> fieldDeclaration.getVariable(0))
                .filter(variableDeclarator -> variableDeclarator.getName().asString().equals("MODEL_NAME"))
                .findFirst()
                .orElseThrow(() -> new KieLC4JException("static MODEL_NAME field not found"));
        assertThat(toCheck.getInitializer()).isNotEmpty();
        assertThat(toCheck.getInitializer().get()).isInstanceOf(StringLiteralExpr.class);
        StringLiteralExpr stringLiteralExpr = (StringLiteralExpr) toCheck.getInitializer().get();
        assertThat(stringLiteralExpr.getValue()).isEqualTo("modelName");
        String modelName = "llama3.2";
        KieLC4JOllamaChatModelFactory.setModelName(modelTemplate, modelName);
        assertThat(toCheck.getInitializer()).isNotEmpty();
        assertThat(toCheck.getInitializer().get()).isInstanceOf(StringLiteralExpr.class);
        stringLiteralExpr = (StringLiteralExpr) toCheck.getInitializer().get();
        assertThat(stringLiteralExpr.getValue()).isEqualTo(modelName);
    }
}