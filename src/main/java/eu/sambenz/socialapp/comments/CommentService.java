package eu.sambenz.socialapp.comments;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private CommentWriterRepository repository;

    public CommentService(CommentWriterRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue,
        exchange = @Exchange(value = "social-app"),
        key = "comments.new"
    ))
    public void save(Comment newComment) {
        repository
            .save(newComment)
            .log("commentService-save")
            .subscribe();
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    CommandLineRunner setUp(MongoOperations operations) {
        return args -> {
            operations.dropCollection(Comment.class);
        };
    }
}
