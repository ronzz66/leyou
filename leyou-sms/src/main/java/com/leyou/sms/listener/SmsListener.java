package com.leyou.sms.listener;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.Map;

@Component
public class SmsListener {
    @Autowired
    private SmsUtils smsUtils;//发送短信的utils

    @Autowired
    private SmsProperties smsProperties;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.sms.queue",durable = "true"),//指定队列，持久化
            exchange = @Exchange(value = "leyou.sms.exchange",ignoreDeclarationExceptions = "true",//指定交换机，忽略异常
                                                    type = ExchangeTypes.TOPIC),//指定消息类型
            key = {"verifycode.sms"}//指定routingkey

    ))
    //前提是发送方必须携带 手机号码和验证码
    public void sendSms(Map<String,String> msg) throws ClientException {

        if (CollectionUtils.isEmpty(msg)){
            return;
        }

        String phone = msg.get("phone");//获取手机号
        String code = msg.get("code");//获取验证码

        if (StringUtils.isNoneBlank(phone) && StringUtils.isNoneBlank(code)){//都不为空才发短信
            //发送验证码短信，最后两个为签名和模板
            smsUtils.sendSms(phone,code,smsProperties.getSignName(),smsProperties.getVerifyCodeTemplate());

        }

    }
}
