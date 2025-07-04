package com.guilhermezuriel.reduceme.application.services.keygen.dto;

import com.guilhermezuriel.reduceme.application.config.infra.InfraProperties;
import com.guilhermezuriel.reduceme.application.model.Key;

import java.time.format.DateTimeFormatter;

public record CompleteKeyResponse(
        String keyHash,
        String completeUrl,
        String createdAt,
        Integer counter,
        String description
){


    public static CompleteKeyResponse build(Key key){
        String completeUrl = InfraProperties.getSTATIC_API_URL_BASE() + "r/" + key.getKeyHash();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return new CompleteKeyResponse(
                key.getKeyHash(),
                completeUrl,
                formatter.format(key.getCreatedAt()),
                key.getCounter(),
                key.getDescription()
        );
    }
}
