package bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hateoas/{username}/bookmarks")
public class BookmarkHateoasRestController {

    private final AccountRepository acctRepo;

    private final BookmarkRepository bookmarkRepo;

    @Autowired
    BookmarkHateoasRestController(AccountRepository acctRepo, BookmarkRepository bookmarkRepo) {
        this.acctRepo = acctRepo;
        this.bookmarkRepo = bookmarkRepo;
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@PathVariable String username, @RequestBody Bookmark input) {
        validateUser(username);
        return acctRepo.findByUsername(username).map(account -> {
            Bookmark result = bookmarkRepo.save(new Bookmark(account, input.getUri(), input.getDescription()));

            HttpHeaders httpHeaders = new HttpHeaders();
            Link forOneBookmark = new BookmarkResource(result).getLink("self");
            httpHeaders.setLocation(URI.create(forOneBookmark.getHref()));

            return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
        }).get();
    }

    @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
    BookmarkResource readBookmark(@PathVariable String username, @PathVariable Long bookmarkId) {
        validateUser(username);
        return new BookmarkResource(bookmarkRepo.findOne(bookmarkId));
    }

    @RequestMapping(method = RequestMethod.GET)
    Resources<BookmarkResource> readBookmarks(@PathVariable String username) {
        validateUser(username);
        List<BookmarkResource> bookmarkResources = bookmarkRepo.findByAccountUsername(username).stream()
                .map(BookmarkResource::new).collect(Collectors.toList());
        return new Resources<BookmarkResource>(bookmarkResources);
    }

    private void validateUser(String username) {
        acctRepo.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }
}
