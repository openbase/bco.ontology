package org.openbase.bco.ontology.lib.utility.sparql;

/*-
 * #%L
 * BCO Ontology Library
 * %%
 * Copyright (C) 2016 - 2019 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

/**
 * @author agatting on 23.12.16.
 */
public class RdfTriple {
    private final String subject;
    private final String predicate;
    private final String object;

    /**
     * Methods creates a triple with subject, predicate and object.
     *
     * @param subject is the SPARQL subject.
     * @param predicate is the SPARQL predicate.
     * @param object is the SPARQL object.
     */
    public RdfTriple(final String subject, final String predicate, final String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * Getter for SPARQL subject.
     *
     * @return the SPARQL subject.
     */
    public String getSubject() {
        return subject; }

    /**
     * Getter for SPARQL predicate.
     *
     * @return the SPARQL predicate.
     */
    public String getPredicate() {
        return predicate; }

    /**
     * Getter for SPARQL object.
     *
     * @return the SPARQL object.
     */
    public String getObject() {
        return object; }
}
