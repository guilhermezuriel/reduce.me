package com.guilhermezuriel.reduceme.application.services.cleanup;

import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService implements CommandLineRunner {

    private final KeyRepository keyRepository;

    @Override
    public void run(String... args){
        log.info("----------------- Cleanup Service started -----------------");

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
                try {
                    var keysExpired = this.keyRepository.findAllWithMoreThanExpirationDate();
                    if(!Objects.isNull(keysExpired) && !keysExpired.isEmpty()) {
                        this.keyRepository.deleteAll(keysExpired);
                    }
                } catch (RuntimeException e) {
                    log.error("Some error ocurred on cleanup:{}", String.valueOf(e));}
                }, 0, 10, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            log.info("----------------- Cleanup Service stopped -----------------");
        }));


    }

}
