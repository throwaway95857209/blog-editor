package com.example.blogeditor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController()
@RequestMapping("/blog")
public class BlogController {
  private static final Logger log = Logger.getLogger(BlogController.class.getName());

  @PostMapping("posts")
  public BlogPost post(@RequestBody BlogPost blogPost) throws IOException {
    String token = System.getenv("GITHUB_ACCESS_TOKEN");
    GitHub github = GitHub.connectUsingOAuth(token);

    GHRepository repository = github.getRepository("throwaway95857209/blog");

    String name = getName(blogPost.getTitle());
    String message = getMessage(name);

    Map<String, Object> post = createOrUpdatePost(blogPost, repository, name, message);

    updateData(repository, message, post);

    return blogPost;
  }

  private String getMessage(String name) {
    return "Add blog post ${name}".replace("${name}", name);
  }

  private String getName(String title) {
    return title.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
  }

  private String template = """
    <div id="blog-post" data-name="${name}" data-title="${title}" data-subtitle="${subtitle}" data-created="${created}" data-author="${author}">
      ${content}
    </div>
      """;

  private Map<String, Object> createOrUpdatePost(BlogPost blogPost, GHRepository repository, String name, String message)
      throws IOException {
    Map<String, Object> post = new LinkedHashMap<>();
    post.put("name", name);
    post.put("title", blogPost.getTitle());
    post.put("subtitle", blogPost.getSubtitle());
    post.put("author", blogPost.getAuthor());
    post.put("created", new Date().getTime());

    String content = blogPost.getHtml();
    String html = template
        .replace("${name}", (CharSequence) post.get("name"))
        .replace("${title}", (CharSequence) post.get("title"))
        .replace("${subtitle}", (CharSequence) post.get("subtitle"))
        .replace("${author}", (CharSequence) post.get("author"))
        .replace("${created}", "" + post.get("created"))
        .replace("${content}", content);

    String htmlPath = "docs/" + name + ".html";
    String sourcePath = "docs/source/" + name + ".html-source";
    repository.createContent().path(htmlPath).content(html).message(message).commit();
    repository.createContent().path(sourcePath).content(blogPost.getSource()).message(message).commit();

    post.put("html", htmlPath.substring(5));
    post.put("source", sourcePath.substring(5));

    return post;
  }

  private void updateData(GHRepository repository, String message, Map<String, Object> post)
      throws IOException, JsonProcessingException {
    try {
      updateDataMap(repository, message, post);
    } catch (FileNotFoundException fnfe) {
      createDataMap(repository, message, post);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void createDataMap(GHRepository repository, String message, Map<String, Object> post)
      throws IOException, JsonProcessingException {
    Map<String, Object> dataMap = new LinkedHashMap<>();
    Map<String, Object> postMap = new LinkedHashMap<>();
    dataMap.put("posts", postMap);
    postMap.put((String) post.get("name"), post);

    repository.createContent().path("docs/data/data.json")
        .content(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(dataMap))
        .message(message).commit();
  }

  private void updateDataMap(GHRepository repository, String message, Map<String, Object> post)
      throws IOException, StreamReadException, DatabindException, JsonProcessingException {
    GHContent dataContent = repository.getFileContent("docs/data/data.json");
    HashMap<String, Object> dataMap = new ObjectMapper().readValue(dataContent.read(),
        new TypeReference<HashMap<String, Object>>() {
        });
    @SuppressWarnings("unchecked")
    Map<String, Object> postMap = (Map<String, Object>) dataMap.get("posts");
    postMap.put((String) post.get("name"), post);
    dataContent.update(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(dataMap), message,
        "main");
  }
}
