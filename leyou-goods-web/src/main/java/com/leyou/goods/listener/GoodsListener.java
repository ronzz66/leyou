package com.leyou.goods.listener;


import com.leyou.goods.service.GoodsHtmlService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsListener {
    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.ITEM.SAVE.QUEUE",durable = "true"),//指定队列名，持久化
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",//指定交换机，
                    ignoreDeclarationExceptions = "true",//忽略异常
                    type = ExchangeTypes.TOPIC),//指定消息类型为TOPIC
            key = {"item.insert","item.update"}//指定通配符(消息发送端的通配符在其中就会运行这个方法)
    ))
    public void save(Long id){
        if (id == null){
            return;
        }
        goodsHtmlService.creareEngine(id);
    }


    //删除html
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.ITEM.DELETE.QUEUE",durable = "true"),//指定队列名，持久化
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",//指定交换机，
                    ignoreDeclarationExceptions = "true",//忽略异常
                    type = ExchangeTypes.TOPIC),//指定消息类型为TOPIC
            key = {"item.delete"}//指定通配符
    ))
    public void delete(Long id){
        if (id == null){
            return;
        }
        goodsHtmlService.deleteEngine(id);
    }



}
