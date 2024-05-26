package com.example.blogeditor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
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

    String name = getName();
    String message = getMessage(name);

    Map<String, Object> post = createOrUpdatePost(blogPost, repository, name, message);

    updateData(repository, message, post);

    return blogPost;
  }

  private String getMessage(String name) {
    String message = "Add blog post ${name}".replace("${name}", name);
    return message;
  }

  private String getName() {
    String name = "post-${name}".replace("${name}",
        new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    return name;
  }

  private Map<String, Object> createOrUpdatePost(BlogPost blogPost, GHRepository repository, String name, String message)
      throws IOException {
    String htmlPath = "docs/" + name + ".html";
    String sourcePath = "docs/source/" + name + ".html-source";
    repository.createContent().path(htmlPath).content(blogPost.getHtml()).message(message).commit();
    repository.createContent().path(sourcePath).content(blogPost.getSource()).message(message).commit();

    Map<String, Object> post = new LinkedHashMap<>();
    post.put("name", name);
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
