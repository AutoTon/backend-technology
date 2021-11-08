package com.technology.apollo;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 自动刷新指定配置项
 */
@Slf4j
@Component
public class SegmentOnlineApolloRefreshConfig {

    private final RefreshScope refreshScope;

    public SegmentOnlineApolloRefreshConfig(final RefreshScope refreshScope) {
        this.refreshScope = refreshScope;
    }

    @ApolloConfigChangeListener(interestedKeyPrefixes = {"segment-online.change."})
    public void onSegmentOnlineChangeStepChange(ConfigChangeEvent changeEvent) {
        refreshScope.refresh("segmentOnlineChangeStepProperties");
    }

}
