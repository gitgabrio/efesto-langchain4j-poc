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
import java.util.HashMap;
import java.util.Map;
import org.kie.lc4j.efesto.api.exceptions.KieLC4JException;
import org.kie.efesto.common.utils.PackageClassNameUtils;
import org.kie.lc4j.efesto.compiler.utils.JavaParserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.lc4j.efesto.compiler.utils.JavaParserUtils.MAIN_CLASS_NOT_FOUND;
import static org.kie.lc4j.efesto.compiler.utils.JavaParserUtils.getFullClassName;

public class KieLC4JOllamaChatModelFactory {

    public static final String OLLAMA_BASE_URL = "http://localhost:11434/"; // This should come from resource/configuration
    private static final Logger logger = LoggerFactory.getLogger(KieLC4JOllamaChatModelFactory.class.getName());
    static final String KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE_JAVA = "KieLC4JOllamaChatModelTemplate.tmpl";
    static final String KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE = "KieLC4JOllamaChatModelTemplate";
    static final String KIE_LC4J_OLLAMA_CHATMODEL_CLASS = "KieLC4JOllamaChatModel";
    static final String KIE_LC4J_OLLAMA_CHATMODEL_CLASS_TEMPLATE = KIE_LC4J_OLLAMA_CHATMODEL_CLASS + "_%s";

    static final ClassOrInterfaceDeclaration OLLAMA_CHATMODEL_TEMPLATE;

    private KieLC4JOllamaChatModelFactory() {
    }

    static {
        CompilationUnit cloneCU = JavaParserUtils.getFromFileName(KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE_JAVA);
        OLLAMA_CHATMODEL_TEMPLATE = cloneCU.getClassByName(KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE)
                .orElseThrow(() -> new KieLC4JException(MAIN_CLASS_NOT_FOUND + ": " + KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE));
    }

    public static ChatModel getKieLC4JOllamaChatModelClass(String modelName) {
        return OllamaChatModel.builder()
                .baseUrl(OLLAMA_BASE_URL) // should come from configuration
                .modelName(modelName)
                .logRequests(true)
                .build();

    }

    // Source code generation
    public static Map<String, String> getKieLC4JOllamaChatModelSourcesMap(String modelName) {
        logger.trace("getKieLC4JOllamaChatModelSourcesMap");
        String className = PackageClassNameUtils.getSanitizedClassName(String.format(KIE_LC4J_OLLAMA_CHATMODEL_CLASS_TEMPLATE, modelName));
        CompilationUnit cloneCU = JavaParserUtils.getKiePMMLModelCompilationUnit(className,
                                                                                 "",
                                                                                 KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE_JAVA, KIE_LC4J_OLLAMA_CHATMODEL_TEMPLATE);
        ClassOrInterfaceDeclaration modelTemplate = cloneCU.getClassByName(className)
                .orElseThrow(() -> new KieLC4JException(MAIN_CLASS_NOT_FOUND + ": " + className));
        setConstructorName(modelTemplate, className);
        setModelName(modelTemplate, modelName);
        Map<String, String> toReturn = new HashMap<>();
        toReturn.put(getFullClassName(cloneCU), cloneCU.toString());
        return toReturn;
    }

    static void setConstructorName(ClassOrInterfaceDeclaration modelTemplate, String constructorName) {
        ConstructorDeclaration constructorDeclaration = modelTemplate.getDefaultConstructor()
                .orElseThrow(() -> new KieLC4JException("Default constructor not found"));
        constructorDeclaration.setName(constructorName);
    }

    static void setModelName(ClassOrInterfaceDeclaration modelTemplate, String modelName) {
        logger.trace("setModelName");
        final VariableDeclarator toModify = modelTemplate.getFields().stream()
                .filter(field -> field.hasModifier(Modifier.Keyword.STATIC) && field.getVariables().size() ==1)
                .map(fieldDeclaration -> fieldDeclaration.getVariable(0))
                .filter(variableDeclarator -> variableDeclarator.getName().asString().equals("MODEL_NAME"))
                .findFirst()
                .orElseThrow(() -> new KieLC4JException("static MODEL_NAME field not found"));
        toModify.setInitializer(new StringLiteralExpr(modelName));
    }

}