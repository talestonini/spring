package bookmarks;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class BookmarkRestControllerTest {

    private static final MediaType CONTENT_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private static final String USERNAME = "talestonini";

    @Autowired
    private AccountRepository acctRepo;

    @Autowired
    private BookmarkRepository bookmarkRepo;

    @Autowired
    private WebApplicationContext webAppContext;

    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Account acct;

    private List<Bookmark> bookmarkList = new ArrayList<>();

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {
        mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null", mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        mockMvc = webAppContextSetup(webAppContext).build();

        bookmarkRepo.deleteAllInBatch();
        acctRepo.deleteAllInBatch();

        acct = acctRepo.save(new Account(USERNAME, "password"));
        bookmarkList.add(bookmarkRepo.save(new Bookmark(acct, "http://bookmark.com/1/" + USERNAME, "A description")));
        bookmarkList.add(bookmarkRepo.save(new Bookmark(acct, "http://bookmark.com/2/" + USERNAME, "A description")));
    }

    @Test
    public void userNotFound() throws Exception {
        mockMvc.perform(post("/george/bookmarks/").content(json(new Bookmark())).contentType(CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void readSingleBookmark() throws Exception {
        mockMvc.perform(get("/" + USERNAME + "/bookmarks/" + bookmarkList.get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.id", is(bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.uri", is("http://bookmark.com/1/" + USERNAME)))
                .andExpect(jsonPath("$.description", is("A description")));
    }

    @Test
    public void readBookmarks() throws Exception {
        mockMvc.perform(get("/" + USERNAME + "/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].uri", is("http://bookmark.com/1/" + USERNAME)))
                .andExpect(jsonPath("$[0].description", is("A description")))
                .andExpect(jsonPath("$[1].id", is(bookmarkList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].uri", is("http://bookmark.com/2/" + USERNAME)))
                .andExpect(jsonPath("$[1].description", is("A description")));
    }

    @Test
    public void createBookmark() throws Exception {
        String bookmarkJson = json(new Bookmark(acct, "http://spring.io", "a bookmark to the best resource of spring "
                + "news and information"));
        mockMvc.perform(post("/" + USERNAME + "/bookmarks").contentType(CONTENT_TYPE).content(bookmarkJson))
                .andExpect(status().isCreated());
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
