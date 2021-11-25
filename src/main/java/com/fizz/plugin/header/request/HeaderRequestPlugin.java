package com.fizz.plugin.header.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import we.plugin.FizzPluginFilter;
import we.plugin.FizzPluginFilterChain;
import we.plugin.auth.ApiConfig;
import we.plugin.auth.ApiConfigService;
import we.util.WebUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fizz.plugin.header.request.HeaderRequestPlugin.PLUGIN_NAME;

/**
 * @author hua.huang
 */
@Slf4j
@Component(value = PLUGIN_NAME)
public class HeaderRequestPlugin implements FizzPluginFilter {
    public static final String PLUGIN_NAME = "fizz-plugin-header-request";
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private ApiConfigService apiConfigService;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> filter(ServerWebExchange exchange, Map<String, Object> config) {
        List<PluginConfig.Header> routerConfig = routerConfig(config);
        ApiConfig apiConfig = apiConfig(exchange);
        List<PluginConfig.Item> pluginConfig = pluginConfig(config);
        List<PluginConfig.Item> pluginConfigAllList = pluginConfig == null ? Lists.newArrayList() : pluginConfig;
        Set<String> gatewayGroups = apiConfig.gatewayGroups;
        gatewayGroups = gatewayGroups == null ? Sets.newHashSet() : gatewayGroups;
        List<PluginConfig.Header> headConfigs = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(pluginConfigAllList)) {
            for (PluginConfig.Item item : pluginConfigAllList) {
                if (gatewayGroups.contains(item.getGwGroup())) {
                    if (CollectionUtils.isNotEmpty(item.getHeaders())) {
                        headConfigs.addAll(item.getHeaders());
                    }
                }
            }
        }

        ServerHttpRequest originRequest = exchange.getRequest();
        HttpHeaders headers = HttpHeaders.writableHttpHeaders(originRequest.getHeaders());
        processHeader(headers, routerConfig, headConfigs);
        ServerWebExchange newExchange = exchange.mutate().request(new ServerHttpRequestDecorator(originRequest) {
            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        }).build();
        return FizzPluginFilterChain.next(newExchange);
    }

    private void processHeader(HttpHeaders headers, List<PluginConfig.Header> routerConfig, List<PluginConfig.Header> pluginHeadConfigs) {
        // 因为路由配置优先于插件配置，所以先处理插件配置，再处理路由配置
        for (PluginConfig.Header pluginHeadConfig : pluginHeadConfigs) {
            processHeader(headers, pluginHeadConfig);
        }
        if (CollectionUtils.isNotEmpty(routerConfig)) {
            for (PluginConfig.Header head : routerConfig) {
                processHeader(headers, head);
            }
        }
    }

    private void processHeader(HttpHeaders headers, PluginConfig.Header head) {
        if (head == null) {
            return;
        }
        PluginConfig.Action action = head.getAction();
        String name = head.getName();
        String value = head.getValue();
        if (action == null || StringUtils.isBlank(name)) {
            return;
        }
        switch (action) {
            case OVERRIDE:
                headers.set(name, value);
                break;
            case APPEND:
                List<String> values = headers.get(name);
                if (CollectionUtils.isNotEmpty(values)) {
                    if (values.size() == 1) {
                        headers.set(name, value);
                    } else {
                        headers.add(name, value);
                    }
                } else {
                    headers.set(name, value);
                }
                break;
            case DELETE:
                headers.remove(name);
                break;
            case SKIP:
                if (!headers.containsKey(name)) {
                    headers.set(name, value);
                }
                break;
            case ADD:
                headers.add(name, value);
                break;
        }
    }

    private List<PluginConfig.Header> routerConfig(Map<String, Object> config) {
        String configStr = (String) config.getOrDefault(PluginConfig.FieldName.CONFIG, StringUtils.EMPTY);
        if (StringUtils.isBlank(configStr)) {
            return Lists.newArrayList();
        }
        try {
            RouterConfig routerConfig = objectMapper.readValue(configStr, RouterConfig.class);
            if (routerConfig != null && routerConfig.getHeaders() != null) {
                return routerConfig.getHeaders();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return Lists.newArrayList();
    }

    private List<PluginConfig.Item> pluginConfig(Map<String, Object> config) {
        String fixedConfig = (String) config.get(we.plugin.PluginConfig.CUSTOM_CONFIG);
        try {
            PluginConfig pluginConfig = objectMapper.readValue(fixedConfig, PluginConfig.class);
            if (pluginConfig != null && pluginConfig.getConfigs() != null) {
                return pluginConfig.getConfigs();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return Lists.newArrayList();
    }

    private ApiConfig apiConfig(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest();
        return apiConfigService.getApiConfig(WebUtils.getAppId(exchange),
                WebUtils.getClientService(exchange), req.getMethod(), WebUtils.getClientReqPath(exchange));
    }

}
