package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoodsListener {
    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.SAVE.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"intem.insert","item.update"}
    ))
    public void save(Long id) throws IOException {//同步保存elasticsearch
        if (id==null){
            return;
        }
        searchService.save(id);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.DELETE.QUEUE",durable = "true"),//队列
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",//交换机
                    ignoreDeclarationExceptions = "true",//忽略异常
                    type = ExchangeTypes.TOPIC),//消息类型
            key = {"intem.delete"}
    ))
    public void delete(Long id) throws IOException {//同步删除elasticsearch
        if (id==null){
            return;
        }
        searchService.delete(id);
    }

}
