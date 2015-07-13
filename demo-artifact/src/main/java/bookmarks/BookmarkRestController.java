package bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

@RestController
@RequestMapping("/{username}/bookmarks")
public class BookmarkRestController {

    private final AccountRepository acctRepo;

    private final BookmarkRepository bookmarkRepo;

    @Autowired
    BookmarkRestController(AccountRepository acctRepo, BookmarkRepository bookmarkRepo) {
        this.acctRepo = acctRepo;
        this.bookmarkRepo = bookmarkRepo;
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@PathVariable String username, @RequestBody Bookmark input) {
        validateUser(username);
        return acctRepo.findByUsername(username).map(account -> {
            Bookmark result = bookmarkRepo.save(new Bookmark(account, input.getUri(), input.getDescription()));

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(result.getId()).toUri());

            return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
        }).get();
    }

    @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
    Bookmark readBookmark(@PathVariable String username, @PathVariable Long bookmarkId) {
        validateUser(username);
        return bookmarkRepo.findOne(bookmarkId);
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Bookmark> readBookmarks(@PathVariable String username) {
        validateUser(username);
        return bookmarkRepo.findByAccountUsername(username);
    }

    private void validateUser(String username) {
        acctRepo.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }
}
