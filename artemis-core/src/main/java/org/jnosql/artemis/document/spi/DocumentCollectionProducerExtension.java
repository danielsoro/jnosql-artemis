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
package org.jnosql.artemis.document.spi;


import org.jnosql.artemis.CrudRepository;
import org.jnosql.artemis.CrudRepositoryAsync;
import org.jnosql.artemis.Database;
import org.jnosql.artemis.DatabaseType;
import org.jnosql.artemis.document.query.CrudRepositoryAsyncDocumentBean;
import org.jnosql.artemis.document.query.CrudRepositoryDocumentBean;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentCollectionManagerAsync;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Extension to start up the DocumentRepository, DocumentRepositoryAsync, CrudRepository and CrudRepositoryAsync
 * from the {@link Database} qualifier
 */
class DocumentCollectionProducerExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(DocumentCollectionProducerExtension.class.getName());

    private final List<Database> databases = new ArrayList<>();

    private final List<Database> databasesAsync = new ArrayList<>();

    private final Collection<Class<?>> crudTypes = new HashSet<>();

    private final Collection<Class<?>> crudAsyncTypes = new HashSet<>();


    <T extends CrudRepository> void onProcessAnnotatedType(@Observes final ProcessAnnotatedType<T> repo) {
        LOGGER.info("Starting the onProcessAnnotatedType");
        crudTypes.add(repo.getAnnotatedType().getJavaClass());
        LOGGER.info("Finished the onProcessAnnotatedType");
    }

    <T extends CrudRepositoryAsync> void onProcessAnnotatedTypeAsync(@Observes final ProcessAnnotatedType<T> repo) {
        LOGGER.info("Starting the onProcessAnnotatedType");
        Class<T> javaClass = repo.getAnnotatedType().getJavaClass();
        crudAsyncTypes.add(javaClass);
        LOGGER.info("Finished the onProcessAnnotatedType");
    }

    <T, X extends DocumentCollectionManager> void processProducer(@Observes final ProcessProducer<T, X> pp) {
        Set<Annotation> annotations = pp.getAnnotatedMember().getAnnotations();
        Optional<Database> databaseOptional = annotations.stream().filter(a -> a instanceof Database)
                .map(Database.class::cast).findFirst();
        if (databaseOptional.isPresent()) {
            Database database = databaseOptional.get();
            if (!DatabaseType.DOCUMENT.equals(database.value())) {
                String simpleName = pp.getAnnotatedMember().getDeclaringType().getJavaClass().getSimpleName();
                throw new IllegalStateException(String.format("The %s must produce DocumentCollectionManager with DOCUMENT type", simpleName));
            }
            databases.add(database);
        }

    }

    <T, X extends DocumentCollectionManagerAsync> void processProducerAsync(@Observes final ProcessProducer<T, X> pp) {
        Set<Annotation> annotations = pp.getAnnotatedMember().getAnnotations();
        Optional<Database> databaseOptional = annotations.stream().filter(a -> a instanceof Database)
                .map(Database.class::cast).findFirst();
        if (databaseOptional.isPresent()) {
            Database database = databaseOptional.get();
            if (!DatabaseType.DOCUMENT.equals(database.value())) {
                String simpleName = pp.getAnnotatedMember().getDeclaringType().getJavaClass().getSimpleName();
                throw new IllegalStateException(String.format("The %s must produce DocumentCollectionManager with DOCUMENT type", simpleName));
            }
            databasesAsync.add(database);
        }

    }

    void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
        LOGGER.info("Starting the onAfterBeanDiscovery with elements number: " + databases.size());
        databases.forEach(type -> {
            final DocumentRepositoryBean bean = new DocumentRepositoryBean(beanManager, type.provider());
            afterBeanDiscovery.addBean(bean);
        });

        databasesAsync.forEach(type -> {
            final DocumentRepositoryAsyncBean bean = new DocumentRepositoryAsyncBean(beanManager, type.provider());
            afterBeanDiscovery.addBean(bean);
        });

        crudTypes.forEach(type -> {
            afterBeanDiscovery.addBean(new CrudRepositoryDocumentBean(type, beanManager, ""));
            databases.forEach(database -> {
                final CrudRepositoryDocumentBean bean = new CrudRepositoryDocumentBean(type, beanManager, database.provider());
                afterBeanDiscovery.addBean(bean);
            });
        });

        crudAsyncTypes.forEach(type -> {
            afterBeanDiscovery.addBean(new CrudRepositoryAsyncDocumentBean(type, beanManager, ""));
            databasesAsync.forEach(database -> {
                final CrudRepositoryAsyncDocumentBean bean = new CrudRepositoryAsyncDocumentBean(type, beanManager, database.provider());
                afterBeanDiscovery.addBean(bean);
            });
        });

        LOGGER.info("Finished the onAfterBeanDiscovery");
    }

}
