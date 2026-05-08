package com.example.demo;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;

import java.net.URI;
import java.time.Instant;

@Context(ClassInContext.s_CLASS_IRI)
@OWLClass(iri = ClassInContext.s_CLASS_IRI)
public class ClassInContext {

    public static final String s_CLASS_IRI = "https://example.com/classInContext";
    public static final URI u_CLASS_IRI = URI.create(s_CLASS_IRI);

    @Id(generated = true)
    private URI id;

    public static final String s_DATE_IRI = "http://purl.org/dc/terms/date";
    public static final URI u_DATE_IRI = URI.create(s_DATE_IRI);

    @OWLDataProperty(iri = s_DATE_IRI)
    private Instant date;

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public ClassInContext() {
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

}
