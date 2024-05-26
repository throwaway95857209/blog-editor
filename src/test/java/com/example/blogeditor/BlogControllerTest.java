package com.example.blogeditor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class BlogControllerTest {
    @Test
    void test() throws IOException {
      BlogPost blogPost = new BlogPost("<h1>hello</h1>", "<h1>hello</h1>");
      BlogController blogController = new BlogController();

        BlogPost post = blogController.post(blogPost);
        assertNotNull(post);
    }
}
