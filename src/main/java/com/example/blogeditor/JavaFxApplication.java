package com.example.blogeditor;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class JavaFxApplication extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer = context -> {
            context.registerBean(Application.class, () -> JavaFxApplication.this);
            context.registerBean(Parameters.class, this::getParameters);
            context.registerBean(HostServices.class, this::getHostServices);
        };
        this.context = new SpringApplicationBuilder()
                .sources(BlogEditorApplication.class)
                .initializers(initializer)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.context.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() throws Exception {
        this.context.close();
        Platform.exit();
    }
}

@Component
class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Autowired
    private ServerPortService serverPortService;

    private final String applicationTitle;
    private final ApplicationContext applicationContext;

    StageInitializer(@Value("${spring.application.ui.title}") String applicationTitle,
            ApplicationContext applicationContext) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        // try {
        Stage stage = stageReadyEvent.getStage();
        stage.setTitle(this.applicationTitle);

        StackPane root = new StackPane();
        WebView webView = new WebView();
        webView.getEngine()
                .load("http://localhost:${port}".replace("${port}", String.valueOf(serverPortService.getPort())));
        root.getChildren().add(webView);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // Stage stage = stageReadyEvent.getStage();
        // stage.setTitle(this.applicationTitle);

        // WebView webView = new WebView();
        // webView.getEngine()
        //         .load("http://localhost:${port}".replace("${port}", String.valueOf(serverPortService.getPort())));

        // VBox vBox = new VBox(webView);
        // Scene scene = new Scene(vBox, "100%", "100%");

        // stage.setScene(scene);
        // stage.show();

        // ClassPathResource fxml = new ClassPathResource("/ui.fxml");
        // FXMLLoader fxmlLoader = new FXMLLoader(fxml.getURL());
        // fxmlLoader.setControllerFactory(this.applicationContext::getBean);
        // Parent root = fxmlLoader.load();
        // Scene scene = new Scene(root, 800, 600);
        // stage.setScene(scene);
        // stage.setTitle(this.applicationTitle);
        // stage.show();
        // } catch (IOException e) {
        // throw new RuntimeException(e);
        // }
    }
}

class StageReadyEvent extends ApplicationEvent {

    private final Stage stage;

    StageReadyEvent(Stage stage) {
        super(stage);
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}