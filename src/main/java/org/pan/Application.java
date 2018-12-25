package org.pan;

import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.pan.module.MainStageView;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author panmingzhi
 */
@Slf4j
@SpringBootApplication
public class Application extends AbstractJavaFxApplicationSupport {

    private static ConfigurableApplicationContext context;
    private static EventBus bus = EventBus.builder().build();

    public static void main(String[] args) {
        log.info("系统启动");
        launch(Application.class, MainStageView.class, new CustomSplash(), args);
    }

    public static void switchView(Class<? extends AbstractFxmlView> from, Class<? extends AbstractFxmlView> to, Object object) {
        try {
            log.debug("从 {} 跳转到 {}", from, to);
            StopWatch started = StopWatch.createStarted();
            AbstractFxmlView fromViewer = context.getBean(from);
            AbstractFxmlView toViewer = context.getBean(to);

            if (!bus.isRegistered(fromViewer.getPresenter()) && hasSubscribe(fromViewer.getPresenter())) {
                bus.register(fromViewer.getPresenter());
                log.info("registered:{}", fromViewer.getPresenter().getClass());
            }

            if (!bus.isRegistered(toViewer.getPresenter()) && hasSubscribe(toViewer.getPresenter())) {
                bus.register(toViewer.getPresenter());
                log.info("registered:{}", toViewer.getPresenter().getClass());
            }

            if (bus.isRegistered(fromViewer.getPresenter())) {
                log.debug("发布隐藏事件");
                bus.post(new ViewEvent(ViewEvent.ViewEvenType.hide, fromViewer.getPresenter()));
            }

            AbstractJavaFxApplicationSupport.showView(to);

            if (bus.isRegistered(toViewer.getPresenter())) {
                log.debug("发布显示事件");
                bus.post(new ViewEvent(ViewEvent.ViewEvenType.show, toViewer.getPresenter()));
            }

            if (object != null) {
                log.debug("跳转参数:{}", object);
                bus.post(object);
            }

            log.debug("跳转页面耗时:{}", started.getTime());
        } catch (Exception e) {

        }
    }

    public static boolean hasSubscribe(Object object) {
        Method[] subscribes = MethodUtils.getMethodsWithAnnotation(object.getClass(), Subscribe.class);
        return subscribes != null && subscribes.length > 0;
    }

    @Override
    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
        Application.context = ctx;

        stage.setWidth(1280);
        stage.setHeight(1080);
        stage.setTitle("自助充值客户端");
        stage.initStyle(StageStyle.UNDECORATED);
//        stage.setAlwaysOnTop(true);

    }

    @Override
    public Collection<Image> loadDefaultIcons() {
        return Arrays.asList(new Image(this.getClass().getResourceAsStream("/image/logo.png")));
    }

}