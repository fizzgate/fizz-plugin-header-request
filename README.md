---
home: false
title: HTTP请求头管理插件
---

## 概述

基于fizz的插件机制，HTTP请求头管理插件，方便更改网关请求头参数及值

## 插件说明

Header Request Plugin插件，是一个非常简单的插件，API请求是指客户端到网关的请求，HTTP请求头是API请求中的一部分。您可以自定义HTTP请求头，在API请求中指定您配置的请求头。

## 使用说明
I、gateway项目pom文件中引入以下依赖：

```xml
<dependency>
  <groupId>com.fizzgate</groupId>
  <artifactId>fizz-plugin-header-request</artifactId>
  <version>2.3.4-beta2</version>
</dependency>
```

II. 管理后台导入以下SQL

 ```sql
insert into `tb_plugin` (`fixed_config`, `id`, `eng_name`, `chn_name`, `config`, `order`, `instruction`, `type`, `create_user`, `create_dept`, `create_time`, `update_user`, `update_time`, `status`, `is_deleted`) values('{\"configs\":[{\"gwGroup\":\"default\",\"headers\":[{\"name\":\"hdr1\",\"value\":\"val1\",\"action\":\"ADD\"}]}]}','50','fizz_plugin_header_request','HTTP请求头管理插件','[{\"field\":\"config\",\"label\":\"json配置\",\"component\":\"textarea\",\"dataType\":\"string\",\"desc\":\"json配置\",\"rules\":[]}]','100',NULL,'2','1123598821738675201','1260823335286165505','2022-04-30 22:38:32','1123598821738675201','2022-04-30 22:38:32','1','0');
 ```

更多网关二次开发请参考[网关快速开发](https://www.fizzgate.com/fizz/guide/fast-dev/fast-dev.html) 、[插件开发样例](https://www.fizzgate.com/fizz/guide/plugin/)

III、可在Fizz Admin管理系统中网关管理->插件管理中配置headerRequestPlugin插件来实现请求头参数值更换。

## HTTP请求头管理插件配置规则说明


表单定义->插件级别的自定义配置信息
```json
{
  "configs": [
    {
      "gwGroup": "fizz-gateway-header-request",
      "headers": [
	{
	  "name": "fizz-header-override",
	  "value": "Override",
	  "action": "OVERRIDE"
	},
	{
	  "name": "fizz-header-append",
	  "value": "Append",
	  "action": "APPEND"
	},
	{
	  "name": "fizz-header-delete",
	  "value": "Delete",
	  "action": "DELETE"
	},
	{
	  "name": "fizz-header-skip",
	  "value": "Skip",
	  "action": "SKIP"
	},
	{
	  "name": "fizz-header-add",
	  "value": "Add",
	  "action": "ADD"
	}
      ]
    }
  ]
}
```

##### 参数说明

|参数名|类型|说明|
|:---- |:----- |-----   |
| configs |array  | 插件级别的自定义配置信息  |

##### config参数说明

|参数名|类型|说明|
|:---- |:----- |-----   |
| name | string  | 响应头名称。每个插件中不能添加重复名称的响应头（不区分大小写），且最多添加10条响应头。  |
| value | string  | 响应头的值。当“Action”为“Delete”时响应头的值不生效，可为空。  |
| action | string  | 响应头操作，您可以覆盖(OVERRIDE)、添加(APPEND)、删除(DELETE)、跳过(SKIP)或新增(ADD)指定的响应头。  |
 
 ####action值说明
```
 Override：覆盖
 当API响应中存在指定的响应头时，使用当前响应头的值覆盖已有响应头的值。
 当API响应中存在多个与指定响应头相同名称的响应头时，该操作只会按当前响应头的值返回一条响应头记录。
 当API响应中不存在指定的响应头时，添加当前响应头。
 
 Append：添加
 当API响应中存在指定的响应头时，使用当前响应头的值覆盖已有响应头的值。
 当API响应中存在多个与指定响应头相同名称的响应头时，会将多个响应头的值用“，”拼接后，再添加当前响应头的值。
 当API响应中不存在指定的响应头时，添加当前响应头。
 
 Delete：删除
 当API响应中存在指定的响应头时，删除当前响应头。
 当API响应中存在多个与指定响应头相同名称的响应头时，删除所有相同名称的响应头。
 
 Skip：跳过
 当API响应中存在指定的响应头时，跳过当前响应头。
 当API响应中存在多个与指定响应头相同名称的响应头时，均不作处理直接返回。
 当API响应中不存在指定的响应头时，添加当前响应头。
 
 Add：新增
 无论API响应中是否存在指定的响应头，都添加当前响应头。
```



