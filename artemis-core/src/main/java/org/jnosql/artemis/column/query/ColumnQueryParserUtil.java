/*
 * Copyright 2017 Otavio Santana and others
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jnosql.artemis.column.query;

import org.jnosql.artemis.DynamicQueryException;
import org.jnosql.diana.api.column.Column;
import org.jnosql.diana.api.column.ColumnCondition;

import java.util.Arrays;

/**
 * Utilitarian class to dynamic query from method on interface
 * {@link ColumnQueryDeleteParser} and {@link ColumnQueryParser}
 */
final class ColumnQueryParserUtil {

    static final String AND = "AND";
    static final String OR = "OR";
    static final String EMPTY = "";

    static final String ORDER_BY = "OrderBy";
    private static final String BETWEEN = "Between";
    private static final String LESS_THAN = "LessThan";
    private static final String GREATER_THAN = "GreaterThan";
    private static final String LESS_THAN_EQUAL = "LessEqualThan";
    private static final String GREATER_THAN_EQUAL = "GreaterEqualThan";
    private static final String LIKE = "Like";

    private ColumnQueryParserUtil() {
    }

    static ColumnCondition toCondition(String token, int index, Object[] args, String methodName) {

        boolean containsBetween = token.contains(BETWEEN);

        if (containsBetween) {
            checkContents(index, args.length, 2, methodName);
            String name = getName(token).replace(BETWEEN, EMPTY);
            return ColumnCondition.between(Column.of(name, Arrays.asList(args[index], args[++index])));
        }

        checkContents(index, args.length, 1, methodName);

        if (token.contains(LESS_THAN)) {
            String name = getName(token).replace(LESS_THAN, EMPTY);
            return ColumnCondition.lt(Column.of(name, args[index]));
        }

        if (token.contains(GREATER_THAN)) {
            String name = getName(token).replace(GREATER_THAN, EMPTY);
            return ColumnCondition.gt(Column.of(name, args[index]));
        }

        if (token.contains(LESS_THAN_EQUAL)) {
            String name = getName(token).replace(LESS_THAN_EQUAL, EMPTY);
            return ColumnCondition.lte(Column.of(name, args[index]));
        }

        if (token.contains(GREATER_THAN_EQUAL)) {
            String name = getName(token).replace(GREATER_THAN_EQUAL, EMPTY);
            return ColumnCondition.gte(Column.of(name, args[index]));
        }

        if (token.contains(LIKE)) {
            String name = getName(token).replace(LIKE, EMPTY);
            return ColumnCondition.like(Column.of(name, args[index]));
        }

        String name = getName(token);
        return ColumnCondition.eq(Column.of(name, args[index]));
    }

    private static void checkContents(int index, int argSize, int required, String method) {
        if ((index + required) <= argSize) {
            return;
        }
        throw new DynamicQueryException(String.format("There is a missed argument in the method %s",
                method));
    }


    private static String getName(String token) {
        return String.valueOf(Character.toLowerCase(token.charAt(0)))
                .concat(token.substring(1));
    }
}
