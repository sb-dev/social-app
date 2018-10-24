package eu.sambenz.socialapp.comments;

import org.springframework.data.repository.Repository;
import reactor.core.publisher.Mono;

public interface CommentWriterRepository extends Repository<Comment, String> {
    Mono<Comment> save(Comment newComment);

    //Needed to support save
    Mono<Comment> findById(String id);
}
