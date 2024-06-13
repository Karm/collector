package com.redhat.quarkus.mandrel.collector.graalvm;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = com.fasterxml.jackson.module.jsonSchema.JsonSchemaIdResolver.class, methods = false, fields = false)
public class ReflectionConfiguration {
}
