package com.etyc.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "etyc.xxljob")
@ConditionalOnClass(XxlJobSpringExecutor.class)
@Slf4j
@Data
public class XxlJobConfig {

    /**
     * 调度中心部署根地址
     */
    private String adminAddresses;

    /**
     * 执行器通讯TOKEN
     */
    private String accessToken;

    /**
     * 执行器AppName
     */
    private String appname;

    /**
     * 执行器注册
     */
    private String address;

    /**
     * 执行器IP
     */
    private String ip;

    /**
     * 执行器端口
     */
    private int port;

    /**
     * 执行器运行日志文件存储磁盘路径
     */
    private String logPath;

    /**
     * 执行器日志文件保存天数
     */
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        return xxlJobSpringExecutor;
    }
}