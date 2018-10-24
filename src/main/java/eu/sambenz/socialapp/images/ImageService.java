package eu.sambenz.socialapp.images;

import eu.sambenz.socialapp.images.Image;
import eu.sambenz.socialapp.images.ImageRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {
    private static String UPLOAD_ROOT = "upload-dir";

    private final ResourceLoader resourceLoader;

    private final ImageRepository imageRepository;

    private final MeterRegistry meterRegistry;

    public ImageService(ResourceLoader resourceLoader, ImageRepository imageRepository, MeterRegistry meterRegistry) {
        this.resourceLoader = resourceLoader;
        this.imageRepository = imageRepository;
        this.meterRegistry = meterRegistry;
    }

//    @Bean
//    CommandLineRunner setUp() {
//        return (args) -> {
//            FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));
//
//            Files.createDirectory(Paths.get(UPLOAD_ROOT));
//
//            FileCopyUtils.copy("Test file",
//                    new FileWriter(UPLOAD_ROOT +
//                    "/learning-spring-boot-cover.jpg"));
//
//            FileCopyUtils.copy("Test file2",
//                    new FileWriter(UPLOAD_ROOT +
//                    "/learning-spring-boot-2nd-edition-cover.jpg"));
//
//            FileCopyUtils.copy("Test file2",
//                    new FileWriter(UPLOAD_ROOT +
//                    "/bazinga.jpg"));
//        };
//    }

    public Flux<Image> findAllImages() {
//        try {
//            return Flux.fromIterable(
//                Files.newDirectoryStream(Paths.get(UPLOAD_ROOT)))
//                .map(path -> new Image(String.valueOf(path.hashCode()),
//                    path.getFileName().toString()));
//        } catch (IOException e) {
//            return Flux.empty();
//        }

        return imageRepository.findAll();
    }

    public Mono<Resource> findOneImage(String filename) {
        return Mono.fromSupplier(() -> resourceLoader.getResource(
                "file:" + UPLOAD_ROOT + "/" + filename));
    }

    public Mono<Void> createImage(Flux<FilePart> files) {
//        return files.flatMap(file -> file.transferTo(
//                Paths.get(UPLOAD_ROOT, file.filename()).toFile())).then();

        return files
            .flatMap(file -> {
                Mono<Image> saveDatabase = imageRepository.save(
                    new Image(
                        UUID.randomUUID().toString(),
                        file.filename()
                    )
                );

                Mono<Void> copyFile = Mono.just(
                        Paths.get(UPLOAD_ROOT, file.filename()).toFile()
                    )
                    .log("createImage-picktarget")
                    .map(destFile -> {
                        try {
                            destFile.createNewFile();
                            return destFile;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .log("createImage-newfile")
                    .flatMap(file::transferTo)
                    .log("createImage-copy");

                Mono<Void> countFile = Mono.fromRunnable(() -> {
                   meterRegistry
                       .summary("files.uploaded.bytes")
                       .record(Paths.get(UPLOAD_ROOT, file.filename()).toFile().length());
                });

                return Mono.when(saveDatabase, copyFile, countFile);
            })
            .log("createImage-flatMap")
            .then()
            .log("createImage-done");
    }

    public Mono<Void> deleteImage(String filename) {
        Mono<Void> deleteDatabaseImage = imageRepository
            .findByName(filename)
            .log("deleteImage-find")
            .flatMap(imageRepository::delete)
            .log("deleteImage-record");

        Mono<Object> deleteFile = Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(Paths.get(UPLOAD_ROOT, filename));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        })
        .log("deleteImage-file");

        return Mono.when(deleteDatabaseImage, deleteFile)
            .log("deleteImage-when")
            .then()
            .log("deleteImage-done");
    }
}
