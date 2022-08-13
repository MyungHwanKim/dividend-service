package com.zerobase.dividend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zerobase.dividend.serializer.CustomLocalDateTimeDeserializer;
import com.zerobase.dividend.serializer.CustomLocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Configuration
public class JacksonConfig {

    public ObjectMapper objectMapper() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(simpleModule);

        StdTypeResolverBuilder builder = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.EVERYTHING,
                objectMapper.getPolymorphicTypeValidator());
        builder = builder.init(JsonTypeInfo.Id.CLASS, null);
        builder = builder.inclusion(JsonTypeInfo.As.PROPERTY);

        objectMapper.setDefaultTyping(builder);

        return objectMapper;
    }
}
