package eu.sambenz.socialapp;

import eu.sambenz.socialapp.images.Image;
import eu.sambenz.socialapp.images.ImageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
public class EmbeddedImageRepositoryTests implements ImageRepositoryTest {
    @Autowired
    ImageRepository repository;

    @Autowired
    MongoOperations operations;

    @Before
    public void setUp() {
        setUp(operations);
    }

    @Test
    public void testFindAll() {
        Flux<Image> images = repository.findAll();

        StepVerifier.create(images)
            .recordWith(ArrayList::new)
            .expectNextCount(3)
            .consumeRecordedWith(results -> {
                assertThat(results).hasSize(3);
                assertThat(results)
                    .extracting(Image::getName)
                    .contains(
                        "test-image-1.jpg",
                        "test-image-2.jpg",
                        "test-image-3.jpg"
                    );
            })
            .expectComplete()
            .verify();
    }

    @Test
    public void testFindByName() {
        Mono<Image> image = repository.findByName("test-image-3.jpg");

        StepVerifier.create(image)
            .expectNextMatches(results -> {
                assertThat(results.getName()).isEqualTo("test-image-3.jpg");
                assertThat(results.getId()).isEqualTo("3");
                return true;
            });
    }
}
