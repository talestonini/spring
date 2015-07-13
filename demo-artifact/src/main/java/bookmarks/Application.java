package bookmarks;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@SpringBootApplication
public class Application {

    @Bean
    CommandLineRunner init(AccountRepository acctRepo, BookmarkRepository bookmarkRepo) {
        return (evt) -> Arrays.asList("jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
                .forEach(a -> {
                            Account account = acctRepo.save(new Account(a, "password"));
                            bookmarkRepo.save(new Bookmark(account, "http://bookmark.com/1/" + a, "A description"));
                            bookmarkRepo.save(new Bookmark(account, "http://bookmark.com/2/" + a, "A description"));
                        }
                );
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
