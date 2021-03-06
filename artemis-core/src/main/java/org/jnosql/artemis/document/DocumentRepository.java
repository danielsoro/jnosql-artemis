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
package org.jnosql.artemis.document;


import org.jnosql.diana.api.NonUniqueResultException;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentQuery;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * This interface that represents the common operation between an entity and DocumentCollectionEntity.
 * @see org.jnosql.diana.api.document.DocumentCollectionManager
 */
public interface DocumentRepository {

    /**
     * Saves entity
     *
     * @param entity entity to be saved
     * @param <T>    the instance type
     * @return the entity saved
     * @throws NullPointerException when document is null
     */
    <T> T save(T entity) throws NullPointerException;

    /**
     * Saves entity with time to live
     *
     * @param entity entity to be saved
     * @param <T>    the instance type
     * @param ttl    the time to live
     * @return the entity saved
     */
    <T> T save(T entity, Duration ttl);

    /**
     * Saves entity, by default it's just run for each saving using
     * {@link DocumentRepository#save(Object)},
     * each NoSQL vendor might replace to a more appropriate one.
     *
     * @param entities entities to be saved
     * @param <T>      the instance type
     * @return the entity saved
     * @throws NullPointerException when entities is null
     */
    default <T> Iterable<T> save(Iterable<T> entities) throws NullPointerException {
        Objects.requireNonNull(entities, "entities is required");
        return StreamSupport.stream(entities.spliterator(), false).map(this::save).collect(toList());
    }

    /**
     * Saves documents collection entity with time to live, by default it's just run for each saving using
     * {@link DocumentRepository#save(Object, Duration)},
     * each NoSQL vendor might replace to a more appropriate one.
     *
     * @param entities entities to be saved
     * @param <T>      the instance type
     * @param ttl      time to live
     * @return the entity saved
     * @throws NullPointerException when entities is null
     */
    default <T> Iterable<T> save(Iterable<T> entities, Duration ttl) throws NullPointerException {
        Objects.requireNonNull(entities, "entities is required");
        Objects.requireNonNull(ttl, "ttl is required");
        return StreamSupport.stream(entities.spliterator(), false).map(d -> save(d, ttl)).collect(toList());
    }

    /**
     * Updates a entity
     *
     * @param entity entity to be updated
     *               @param <T>      the instance type
     * @return the entity updated
     */
    <T> T update(T entity);

    /**
     * Updates entity, by default it's just run for each saving using
     * {@link DocumentRepository#update(Object)},
     * each NoSQL vendor might replace to a more appropriate one.
     *
     * @param entities entities to be saved
     * @param <T>      the instance type
     * @return the entity saved
     * @throws NullPointerException when entities is null
     */
    default <T> Iterable<T> update(Iterable<T> entities) throws NullPointerException {
        Objects.requireNonNull(entities, "entities is required");
        return StreamSupport.stream(entities.spliterator(), false).map(this::update).collect(toList());
    }

    /**
     * Deletes an entity
     *
     * @param query query to delete an entity
     */
    void delete(DocumentDeleteQuery query);

    /**
     * Finds entities from query
     *
     * @param query - query to figure out entities
     * @param <T>   the instance type
     * @return entities found by query
     * @throws NullPointerException when query is null
     */
    <T> List<T> find(DocumentQuery query) throws NullPointerException;

    /**
     * Returns a single entity from query
     *
     * @param query - query to figure out entities
     * @param <T>   the instance type
     * @return an entity on {@link Optional} or {@link Optional#empty()} when the result is not found.
     * @throws NonUniqueResultException when the result has more than 1 entity
     * @throws NullPointerException     when query is null
     */
    default <T> Optional<T> singleResult(DocumentQuery query) throws NonUniqueResultException {
        List<T> entities = find(query);
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        if (entities.size() == 1) {
            return Optional.of(entities.get(0));
        }

        throw new NonUniqueResultException("The query returns more than one entity, query: " + query);
    }

}
