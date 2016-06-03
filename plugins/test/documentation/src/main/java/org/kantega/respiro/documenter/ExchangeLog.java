package org.kantega.respiro.documenter;

import fj.Show;
import fj.data.List;
import org.kantega.respiro.collector.ExchangeInfo;
import org.kantega.respiro.collector.ExchangeMessage;

import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;

public class ExchangeLog {

    public final static Show<ExchangeMessage> msgShow =
      Show.showS(ex -> ex.getProtocol() + " "+ex.getMethod()+ " " + ( ex.getAddress()==null?"":ex.getAddress() ));

    public final static Show<ExchangeInfo> exInfoShow =
      Show.showS(info ->
        msgShow.showS(info.getInMessage()) + " -> " +
          join(info.getBackendMessages().stream().map(msgShow::showS).collect(Collectors.toList()), " -> ") + " -> " +
          msgShow.showS(info.getOutMessage()));

    private Deque<ExchangeInfo> exchangeLog = new ConcurrentLinkedDeque<>();

    private AtomicLong counter = new AtomicLong();


    public void addExchange(ExchangeInfo message) {
        while (exchangeLog.size() >= 50) {
            exchangeLog.removeLast();
        }
        exchangeLog.addFirst(message);
    }

    public ArrayList<ExchangeInfo> getExchangeLog() {
        return new ArrayList<>(exchangeLog);
    }

    public List<ExchangeInfo> asList(){
        return List.iterableList(exchangeLog);
    }

    public void clear() {
        exchangeLog.clear();
    }
}
