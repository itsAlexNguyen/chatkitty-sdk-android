/*
 * Copyright 2020 ChatKitty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chatkitty;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.chatkitty.model.session.SessionStartResult;
import com.chatkitty.stompx.stompx.StompWebSocketClient;
import com.chatkitty.stompx.stompx.StompWebSocketClientCallBack;
import com.chatkitty.stompx.stompx.WebSocketConfiguration;
import com.chatkitty.stompx.stompx.stomp.StompSubscription;
import com.chatkitty.stompx.stompx.stomp.WebSocketEvent;
import com.chatkitty.stompx.stompx.stomp.stompframe.StompServerFrame;

import okhttp3.OkHttpClient;

public class ChatKittyImpl implements ChatKitty {

  private final String apiKey;
  @Nullable private StompWebSocketClient client;

  public ChatKittyImpl(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public void startSession(String username, ChatKittyCallback callback) {
    WebSocketConfiguration configuration =
        new WebSocketConfiguration(
            apiKey, username, "https://staging-api.chatkitty.com", "/stompx");

    client = new StompWebSocketClient(new OkHttpClient(), new ObjectMapper(), configuration);
    client.start();

    client.subscribeRelay(
        "/application/users.me.relay",
        new WebSocketClientCallBack<SessionStartResult>(SessionStartResult.class) {
          @Override
          void onParsedMessage(
              SessionStartResult resource,
              StompWebSocketClient client,
              StompSubscription subscription) {
            callback.onSuccess(resource);
          }
        });
  }

  private abstract static class WebSocketClientCallBack<T> implements StompWebSocketClientCallBack {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> type;

    WebSocketClientCallBack(Class<T> type) {
      this.type = type;
    }

    @Override
    public void onNewMessage(
        StompWebSocketClient client, StompServerFrame frame, StompSubscription subscription) {
      try {
        JavaType javaType =
            objectMapper.getTypeFactory().constructParametricType(WebSocketEvent.class, type);
        WebSocketEvent<T> response = objectMapper.readValue(frame.body, javaType);
        onParsedMessage(response.resource, client, subscription);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }

    abstract void onParsedMessage(
        T resource, StompWebSocketClient client, StompSubscription subscription);
  }
}
