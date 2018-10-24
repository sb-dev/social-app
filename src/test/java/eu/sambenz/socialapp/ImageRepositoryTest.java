package eu.sambenz.socialapp;

import eu.sambenz.socialapp.images.Image;
import org.springframework.data.mongodb.core.MongoOperations;

public interface ImageRepositoryTest {
    void testFindAll();
    void testFindByName();

    default void setUp(MongoOperations operations) {
        operations.dropCollection(Image.class);
        operations.insert(new Image("1", "test-image-1.jpg"));
        operations.insert(new Image("2", "test-image-2.jpg"));
        operations.insert(new Image("3", "test-image-3.jpg"));
        operations.findAll(Image.class).forEach(image -> System.out.println(image.toString()));
    }
}
