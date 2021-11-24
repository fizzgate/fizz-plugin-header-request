package com.ol.fizz.plugin.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import we.FizzAppContext;
import we.config.AggregateRedisConfig;
import we.log.LogSendAppender;

/**
 * 启动类
 *
 * @author huahuang
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {
        "we.config", "we.fizz", "we.plugin", "we.filter", "we.proxy", "com.ol.fizz.plugin.request"
})
public class Application {

    public static void main(String[] args) {
        log.info("开始启动");
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.setApplicationContextClass(CustomReactiveWebServerApplicationContext.class);
        FizzAppContext.appContext = springApplication.run(args);
        log.info("启动成功");
    }

    private static class CustomReactiveWebServerApplicationContext
            extends AnnotationConfigReactiveWebServerApplicationContext {
        @Override
        protected void onClose() {
            super.onClose();
            if (AggregateRedisConfig.proxyLettuceConnectionFactory != null) {
                log.info("Application stopped.");
                // set LogSendAppender.logEnabled to false to stop send log to fizz-manager
                LogSendAppender.logEnabled = Boolean.FALSE;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignore
                }
                // the ProxyLettuceConnectionFactory remove DisposableBean interface, so invoke destroy method here
                AggregateRedisConfig.proxyLettuceConnectionFactory.destroy();
            }
        }
    }
}
