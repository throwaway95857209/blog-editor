package com.example.blogeditor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.commands.HttpRequestCommand;
import com.example.commands.HttpResponseModel;

@SpringBootTest
class BlogEditorApplicationTests {

  @Test
  void test() throws Exception {
    HttpResponseModel response = new HttpRequestCommand("https://throwaway95857209.github.io/blog/").execute();
    int status = response.getStatus();
    assertEquals(200, status);
    System.out.println(new String(response.getBytes()));
    System.out.println(response.getHeaders());
  }

}
