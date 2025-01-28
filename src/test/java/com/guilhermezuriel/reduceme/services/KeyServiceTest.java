package com.guilhermezuriel.reduceme.services;

import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import com.guilhermezuriel.reduceme.application.services.keygen.KeyGenerationService;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KeyServiceTest {

    @InjectMocks
    private KeyGenerationService keyService;

    @Mock
    private KeyRepository keyRepository;

    @Test
    @DisplayName("Should be able to reduce the url ")
    public void should_be_able_to_reduce_url(){
        CreateReducedUrlForm form = new CreateReducedUrlForm("https://app.rocketseat.com.br/classroom/testes-e-qualidade-de-codigo/group/testes-da-aplicacao/lesson/testando-excecoes");
        var keyGenerated = this.keyService.generateKeys(form);
        Assertions.assertNotEquals(keyGenerated.getOriginalUrl().length(), keyGenerated.getKeyHash().length());
    }

    @Test
    @DisplayName("Should not be able to reduce the same url to the same hash")
    public void should_not_be_able_to_reduce_url_into_the_same_hash_twice(){
        CreateReducedUrlForm form = new CreateReducedUrlForm("http://localhost:8080/");
        var keyGenerated1 = this.keyService.generateKeys(form);
        var keyGenerated2 = this.keyService.generateKeys(form);
        Assertions.assertNotEquals(keyGenerated1.getKeyHash(), keyGenerated2.getKeyHash() );
    }
}
