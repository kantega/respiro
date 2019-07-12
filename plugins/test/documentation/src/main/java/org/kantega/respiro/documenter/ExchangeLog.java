/*
 * Copyright 2019 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
