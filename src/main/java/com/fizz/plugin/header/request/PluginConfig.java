package com.fizz.plugin.header.request;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class PluginConfig {
    private List<Item> configs = Lists.newArrayList();

    @Data
    public static class Item {
        private String gwGroup;
        private List<Header> headers = Lists.newArrayList();
    }

    @Data
    public static class Header {
        private String name;
        private String value;
        private Action action;
    }

    /**
     * @see <a href="https://support.huaweicloud.com/usermanual-apig/apig-ug-0005.html">
     * https://support.huaweicloud.com/usermanual-apig/apig-ug-0005.html</a>
     */
    public enum Action {
        /**
         * 覆盖
         * <p>
         * 当API响应中存在指定的响应头时，使用当前响应头的值覆盖已有响应头的值。
         * 当API响应中存在多个与指定响应头相同名称的响应头时，该操作只会按当前响应头的值返回一条响应头记录。
         * 当API响应中不存在指定的响应头时，添加当前响应头。
         */
        OVERRIDE,
        /**
         * 添加
         * <p>
         * 当API响应中存在指定的响应头时，使用当前响应头的值覆盖已有响应头的值。
         * 当API响应中存在多个与指定响应头相同名称的响应头时，会将多个响应头的值用“，”拼接后，再添加当前响应头的值。
         * 当API响应中不存在指定的响应头时，添加当前响应头。
         */
        APPEND,
        /**
         * 删除
         * <p>
         * 当API响应中存在指定的响应头时，删除当前响应头。
         * 当API响应中存在多个与指定响应头相同名称的响应头时，删除所有相同名称的响应头。
         */
        DELETE,
        /**
         * 跳过
         * <p>
         * 当API响应中存在指定的响应头时，跳过当前响应头。
         * 当API响应中存在多个与指定响应头相同名称的响应头时，均不作处理直接返回。
         * 当API响应中不存在指定的响应头时，添加当前响应头。
         */
        SKIP,
        /**
         * 新增
         * <p>
         * 无论API响应中是否存在指定的响应头，都添加当前响应头。
         */
        ADD

    }

    public interface FieldName {
        String CONFIG = "config";
    }
}
