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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoLocalRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.lc4j.efesto.runtime.model.EfestoOutputLC4J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.lc4j.efesto.runtime.utils.Lc4jRuntimeHelper.canManage;
import static org.kie.lc4j.efesto.runtime.utils.Lc4jRuntimeHelper.execute;
import org.kie.lc4j.efesto.api.identifiers.LocalComponentIdLC4J;

public class KieRuntimeServiceLC4JMapInput implements KieRuntimeService<Map<String, List<String>>, List<String>, EfestoInput<Map<String, List<String>>>, EfestoOutputLC4J, EfestoLocalRuntimeContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieRuntimeServiceLC4JMapInput.class);

    @Override
    public EfestoClassKey getEfestoClassKeyIdentifier() {
        return new EfestoClassKey(BaseEfestoInput.class, HashMap.class);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean canManageInput(EfestoInput toEvaluate, EfestoLocalRuntimeContext context) {
        return canManage(toEvaluate, context)
                && toEvaluate.getModelLocalUriId().model().equals(LocalComponentIdLC4J.PREFIX);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Optional<EfestoOutputLC4J> evaluateInput(EfestoInput<Map<String, List<String>>> toEvaluate,
                                                   EfestoLocalRuntimeContext context) {
        if (!canManageInput(toEvaluate, context)) {
            throw new KieRuntimeServiceException("Wrong parameters " + toEvaluate + " " + context);
        }
        return execute(toEvaluate,  context);
    }

    @Override
    public String getModelType() {
        return LocalComponentIdLC4J.PREFIX;
    }

    @Override
    public Optional<EfestoInput<Map<String, List<String>>>> parseJsonInput(String modelLocalUriIdString, String inputDataString) {
        return Optional.empty();
    }

}
