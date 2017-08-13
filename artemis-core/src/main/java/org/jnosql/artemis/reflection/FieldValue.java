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
package org.jnosql.artemis.reflection;


/**
 * The tuple between the instance value and {@link FieldRepresentation}
 */
public interface FieldValue {


    /**
     * Creates a {@link FieldValue} instance
     *
     * @param value the value
     * @param field the field
     */
    static FieldValue of(Object value, FieldRepresentation field) {
        return new DefaultFieldValue(value, field);
    }

    /**
     * Returns the instance
     *
     * @return the instance
     */
    Object getValue();

    /**
     * returns the Field representation
     *
     * @return the {@link FieldRepresentation} instance
     */
    FieldRepresentation getField();

    /**
     * Returns true if {@link FieldValue#getValue()} is different of null
     *
     * @return if {@link FieldValue#getValue()} is different of null
     */
    boolean isNotEmpty();

}
