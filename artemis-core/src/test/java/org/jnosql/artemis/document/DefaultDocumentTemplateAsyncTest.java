/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.artemis.document;

import org.jnosql.artemis.WeldJUnit4Runner;
import org.jnosql.artemis.model.Person;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCollectionManagerAsync;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(WeldJUnit4Runner.class)
public class DefaultDocumentTemplateAsyncTest {

    private Person person = Person.builder().
            withAge().
            withPhones(Arrays.asList("234", "432")).
            withName("Name")
            .withId(19)
            .withIgnore().build();

    private Document[] documents = new Document[]{
            Document.of("age", 10),
            Document.of("phones", Arrays.asList("234", "432")),
            Document.of("name", "Name"),
            Document.of("id", 19L),
    };


    @Inject
    private DocumentEntityConverter converter;

    private DocumentCollectionManagerAsync managerMock;

    private DefaultDocumentTemplateAsync subject;

    private ArgumentCaptor<DocumentEntity> captor;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        managerMock = Mockito.mock(DocumentCollectionManagerAsync.class);
        DocumentEventPersistManager documentEventPersistManager = Mockito.mock(DocumentEventPersistManager.class);
        captor = ArgumentCaptor.forClass(DocumentEntity.class);
        Instance<DocumentCollectionManagerAsync> instance = Mockito.mock(Instance.class);
        when(instance.get()).thenReturn(managerMock);
        this.subject = new DefaultDocumentTemplateAsync(converter, instance);
    }

    @Test
    public void shouldSave() {
        DocumentEntity document = DocumentEntity.of("Person");
        document.addAll(Stream.of(documents).collect(Collectors.toList()));


        subject.insert(this.person);
        verify(managerMock).insert(captor.capture(), Mockito.any(Consumer.class));
        DocumentEntity value = captor.getValue();
        assertEquals("Person", value.getName());
        assertEquals(4, value.getDocuments().size());
    }

    @Test
    public void shouldSaveTTL() {

        Duration twoHours = Duration.ofHours(2L);

        DocumentEntity document = DocumentEntity.of("Person");
        document.addAll(Stream.of(documents).collect(Collectors.toList()));


        subject.insert(this.person, twoHours);
        verify(managerMock).insert(captor.capture(), Mockito.eq(twoHours), Mockito.any(Consumer.class));
        DocumentEntity value = captor.getValue();
        assertEquals("Person", value.getName());
        assertEquals(4, value.getDocuments().size());
    }


    @Test
    public void shouldUpdate() {
        DocumentEntity document = DocumentEntity.of("Person");
        document.addAll(Stream.of(documents).collect(Collectors.toList()));


        subject.update(this.person);
        verify(managerMock).update(captor.capture(), Mockito.any(Consumer.class));
        DocumentEntity value = captor.getValue();
        assertEquals("Person", value.getName());
        assertEquals(4, value.getDocuments().size());
    }


    @Test
    public void shouldDelete() {
        DocumentDeleteQuery query = DocumentDeleteQuery.of("delete");
        subject.delete(query);
        verify(managerMock).delete(query);
    }
}