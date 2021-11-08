package com.technology.apollo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * apollo配置项样例
 */
@Data
@ConfigurationProperties(prefix = "segment-online.change")
public class SegmentOnlineChangeStepProperties {

    private List<ChangeStepParam> steps;

}
