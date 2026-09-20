package com.pisethjavaschool.property.facade.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pisethjavaschool.property.dto.CreatePropertyRequest;
import com.pisethjavaschool.property.dto.PropertyResponse;
import com.pisethjavaschool.property.facade.CreatePropertyFacade;
import com.pisethjavaschool.property.service.PropertyFactory;
import com.pisethjavaschool.property.service.PropertyImageWriter;
import com.pisethjavaschool.property.service.PropertyResponseBuilder;
import com.pisethjavaschool.property.service.PropertyValidator;
import com.pisethjavaschool.property.service.PropertyWriter;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CreatePropertyFacadeImpl implements CreatePropertyFacade {

    private final PropertyValidator validator;
    private final PropertyFactory factory;
    private final PropertyWriter writer;
    private final PropertyImageWriter imageWriter;
    private final PropertyResponseBuilder responseBuilder;

    @Override
    @Transactional
    public Mono<PropertyResponse> create(CreatePropertyRequest request) {
        return validator.validateForCreate(request)
                .thenReturn(factory.createDraft(request.ownerId(), request))
                .flatMap(writer::save)
                .flatMap(property -> imageWriter
                        .replaceForCreate(
                                property.getId(),
                                request.thumbnailMediaId(),
                                request.galleryMediaIds())
                        .thenReturn(property))
                .flatMap(responseBuilder::build);
    }
}
