package we.fizz.plugin.header.request;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class RouterConfig {
    private List<PluginConfig.Header> headers = Lists.newArrayList();
}
