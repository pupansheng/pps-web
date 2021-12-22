
package com.pps.base;

import com.pps.base.EventHander;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Pu PanSheng, 2021/12/22
 * @version OPRA v1.0
 */
public class EventHanderFactory {

    private static Map<String, EventHander> eventHanderMap=new HashMap<>();


    public static EventHander getEventHander(String type){

        EventHander eventHander = eventHanderMap.get(type);
        if(eventHander!=null){
            return  eventHander;
        }

        throw new RuntimeException(type+" Event  没有配置 该事件处理器！");

    }

    public static void initEventHander(Server server) {

        ServiceLoader<EventHander> load = ServiceLoader.load(EventHander.class);
        load.forEach(s->{
            s.init(server);
            eventHanderMap.put(s.support(),s);
        });


    }
}
