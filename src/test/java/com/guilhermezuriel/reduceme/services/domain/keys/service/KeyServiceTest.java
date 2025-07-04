package com.guilhermezuriel.reduceme.services.domain.keys.service;

import com.guilhermezuriel.reduceme.application.Utils;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class KeyServiceTest {

    @Test
    @DisplayName("Should be able to reduce the url")
    public void should_be_able_to_reduce_url(){
        CreateReducedUrlForm form = new CreateReducedUrlForm("http://localhost:8080/", "description");
        var reducedUrl = Utils.digestUrl(form.url());
        Assertions.assertNotEquals(reducedUrl.length(), form.url().length());
    }

    @Test
    @DisplayName("Should not be able to reduce the same url to the same hash")
    public void should_not_be_able_to_reduce_url_into_the_same_hash_twice(){
        CreateReducedUrlForm form = new CreateReducedUrlForm("http://localhost:8080/", "description");
        var reducedUrl = Utils.digestUrl(form.url());
        System.out.println("url1: "+reducedUrl);
        var reducedUrl2 = Utils.digestUrl(form.url());
        System.out.println("url2: "+reducedUrl2);
        Assertions.assertNotEquals(reducedUrl, reducedUrl2);
    }
}
