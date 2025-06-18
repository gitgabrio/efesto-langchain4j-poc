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
package org.kie.dmn.core.lc4j;

import java.util.HashMap;
import java.util.Map;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.model.api.DMNElement;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LC4JInvocationEvaluatorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LC4JInvocationEvaluatorFactory.class);

    private static final Map<ModelLocalUriId, LC4JInvocationEvaluator> MAPPED_INSTANCES = new HashMap<>();

    private LC4JInvocationEvaluatorFactory() {
        // Constructing instances is not allowed for this Factory
    }

    public static LC4JInvocationEvaluator newInstance(DMNModelImpl model, DMNElement funcDef,
                                                      ModelLocalUriId lc4jModelLocalUriID, String lc4jEngine,
                                                      String lc4jModelName) {
        if (MAPPED_INSTANCES.containsKey(lc4jModelLocalUriID)) {
            return MAPPED_INSTANCES.get(lc4jModelLocalUriID);
        } else {
            LC4JInvocationEvaluator toReturn = createNewInstance(model, funcDef, lc4jModelLocalUriID, lc4jEngine,
                                                                 lc4jModelName);
            MAPPED_INSTANCES.put(lc4jModelLocalUriID, toReturn);
            return toReturn;
        }
    }

    static LC4JInvocationEvaluator createNewInstance(DMNModelImpl model, DMNElement funcDef,
                                                     ModelLocalUriId lc4jModelLocalUriID, String lc4jEngine,
                                                     String lc4jModelName) {
        return new LC4JInvocationEvaluator(model.getNamespace(), funcDef, lc4jModelLocalUriID, lc4jEngine,
                                           lc4jModelName);
    }
}