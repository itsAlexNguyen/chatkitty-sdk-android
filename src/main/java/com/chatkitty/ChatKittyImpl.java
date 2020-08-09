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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.chatkitty.configuration.ChatKittyConfiguration;
import com.chatkitty.listeners.channel.ChannelEnterRegistration;
import com.chatkitty.model.channel.response.EnterChannelResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.chatkitty.listeners.channel.ChannelEventListener;
import com.chatkitty.listeners.channel.ChannelEventListenerRegistration;
import com.chatkitty.model.Channel;
import com.chatkitty.model.CurrentUser;
import com.chatkitty.model.PagedResource;
import com.chatkitty.model.SessionNotStartedException;
import com.chatkitty.model.channel.event.MessageReceivedEvent;
import com.chatkitty.model.channel.response.GetChannelResponse;
import com.chatkitty.model.channel.response.GetChannelsResult;
import com.chatkitty.model.message.Message;
import com.chatkitty.model.message.TextMessage;
import com.chatkitty.model.message.request.CreateTextMessageRequest;
import com.chatkitty.model.message.response.CreateMessageResult;
import com.chatkitty.model.message.response.GetMessageResponse;
import com.chatkitty.model.message.response.GetMessagesResult;
import com.chatkitty.model.session.response.SessionStartResult;
import com.chatkitty.model.user.response.GetCurrentUserResult;
import com.chatkitty.stompx.stompx.StompWebSocketClient;
import com.chatkitty.stompx.stompx.StompWebSocketClientCallBack;
import com.chatkitty.stompx.stompx.WebSocketConfiguration;
import com.chatkitty.stompx.stompx.stomp.StompSubscription;
import com.chatkitty.stompx.stompx.stomp.WebSocketEvent;
import com.chatkitty.stompx.stompx.stomp.stompframe.StompServerFrame;

import okhttp3.OkHttpClient;

public class ChatKittyImpl implements ChatKitty {

  private final ChatKittyConfiguration configuration;

  @Nullable private StompWebSocketClient client;

  @Nullable private SessionStartResult session;

  public ChatKittyImpl(String apiKey) {
    this.configuration = new ChatKittyConfiguration(apiKey);
  }

  public ChatKittyImpl(ChatKittyConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void startSession(
      String username, String challengeToken, ChatKittyCallback<SessionStartResult> callback) {
    Map<String, String> connectionHeaders = new HashMap<>();
    connectionHeaders.put("Api-Key", configuration.getApiKey());
    connectionHeaders.put("StompX-User", username);
    if (challengeToken != null) {
      connectionHeaders.put("StompX-Challenge-Token", challengeToken);
    }
    WebSocketConfiguration configuration =
        new WebSocketConfiguration(
            this.configuration.getWebSocketBaseUrl(), this.configuration.getWebSocketEndpoint(),
            connectionHeaders);

    client = new StompWebSocketClient(new OkHttpClient(), new ObjectMapper(), configuration);
    client.start();

    client.subscribeRelay(
        "/application/v1/users/me.relay",
        new WebSocketClientCallBack<CurrentUser>(CurrentUser.class) {
          @Override
          void onParsedMessage(
              CurrentUser resource, StompWebSocketClient client, StompSubscription subscription) {
            SessionStartResult result = new SessionStartResult();
            result.setCurrentUser(resource);
            session = result;
            callback.onSuccess(result);
          }
        });
  }

  @Override
  public void startSession(String username, ChatKittyCallback<SessionStartResult> callback) {
    startSession(username, null, callback);
  }

  @Override
  public void getCurrentUser(ChatKittyCallback<GetCurrentUserResult> callback) {
    if (client == null || session == null) {
      // TODO - Should add more potential scenarios here.
      callback.onError(new SessionNotStartedException());
      return;
    }
    client.subscribeRelay(
        session.getCurrentUser().get_relays().getSelf(),
        new WebSocketClientCallBack<CurrentUser>(CurrentUser.class) {
          @Override
          void onParsedMessage(
              CurrentUser resource, StompWebSocketClient client, StompSubscription subscription) {
            GetCurrentUserResult result = new GetCurrentUserResult();
            result.setCurrentUser(resource);
            callback.onSuccess(result);
          }
        });
  }

  @Override
  public void getChannels(ChatKittyCallback<GetChannelsResult> callback) {
    if (client == null || session == null) {
      // TODO - Should add more potential scenarios here.
      callback.onError(new SessionNotStartedException());
      return;
    }

    client.subscribeRelay(
        session.getCurrentUser().get_relays().getChannels(),
        new WebSocketPagedClientCallBack<GetChannelResponse>(GetChannelResponse.class) {
          @Override
          void onParsedMessage(
              PagedResource<GetChannelResponse> resource,
              StompWebSocketClient client,
              StompSubscription subscription) {
            GetChannelsResult result =
                new GetChannelsResult(
                    resource.get_embedded().getChannels(), resource.get_relays().getNext());
            callback.onSuccess(result);
          }
        });
  }

  @Override
  public ChannelEnterRegistration enterChannel(
      Channel channel,
      ChatKittyCallback<EnterChannelResult> callback) {
    StompSubscription subscription =
        client.subscribe(
            channel.get_topics().getSelf(), (client, frame, subscription1) -> {
              EnterChannelResult result = new EnterChannelResult();
              callback.onSuccess(result);
            });
    return ChannelEnterRegistration.create(() -> client.unsubscribe(subscription));
  }

  @Override
  public void getChannelMessages(Channel channel, ChatKittyCallback<GetMessagesResult> callback) {
    if (client == null || session == null) {
      // TODO - Should add more potential scenarios here.
      callback.onError(new SessionNotStartedException());
      return;
    }

    client.subscribeRelay(
        channel.get_relays().getMessages(),
        new WebSocketPagedClientCallBack<GetMessageResponse>(GetMessageResponse.class) {
          @Override
          void onParsedMessage(
              PagedResource<GetMessageResponse> resource,
              StompWebSocketClient client,
              StompSubscription subscription) {
            // TODO - should do this mapping somewhere else.
            List<Message> messages = new ArrayList<>(resource.get_embedded().getMessages());
            GetMessagesResult result =
                new GetMessagesResult(messages, resource.get_relays().getNext());
            callback.onSuccess(result);
          }
        });
  }

  @Override
  public ChannelEventListenerRegistration registerChannelEventListener(
      Channel channel, ChannelEventListener listener) {
    if (client == null || session == null) {
      // TODO - Proper Error Handling
      return ChannelEventListenerRegistration.create();
    }

    // TODO - The message model should be able to support multiple messages.
    StompSubscription subscription =
        client.subscribe(
            channel.get_topics().getMessages(),
            new WebSocketClientCallBack<TextMessage>(TextMessage.class) {
              @Override
              void onParsedMessage(
                  TextMessage resource,
                  StompWebSocketClient client,
                  StompSubscription subscription) {
                MessageReceivedEvent event = new MessageReceivedEvent();
                event.setMessage(resource);
                listener.onMessageReceived(event);
              }
            });
    return ChannelEventListenerRegistration.create(() -> client.unsubscribe(subscription));
  }

  @Override
  public void sendChannelMessage(
      Channel channel,
      CreateTextMessageRequest request,
      ChatKittyCallback<CreateMessageResult> callback) {
    if (client == null || session == null) {
      // TODO - Proper Error Handling
      callback.onError(new SessionNotStartedException());
      return;
    }

    client.subscribe(
        channel.get_topics().getMessages(),
        new WebSocketClientCallBack<TextMessage>(TextMessage.class) {
          @Override
          void onParsedMessage(
              TextMessage resource, StompWebSocketClient client, StompSubscription subscription) {
            CreateMessageResult result = new CreateMessageResult();
            result.setMessage(resource);
            callback.onSuccess(result);
            client.unsubscribe(subscription);
          }
        });

    // TODO - We should move object mapping to outside the StompX library.
    try {
      client.sendPayload(channel.get_actions().getMessage(), request, false);
    } catch (JsonProcessingException exception) {
      exception.printStackTrace();
    }
  }

  private abstract static class WebSocketPagedClientCallBack<T>
      implements StompWebSocketClientCallBack {

    private final ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Class<T> type;

    WebSocketPagedClientCallBack(Class<T> type) {
      this.type = type;
    }

    @Override
    public void onNewMessage(
        StompWebSocketClient client, StompServerFrame frame, StompSubscription subscription) {
      try {
        JavaType pagedType =
            objectMapper.getTypeFactory().constructParametricType(PagedResource.class, type);

        JavaType javaType =
            objectMapper.getTypeFactory().constructParametricType(WebSocketEvent.class, pagedType);

        WebSocketEvent<PagedResource<T>> response = objectMapper.readValue(frame.body, javaType);
        onParsedMessage(response.getResource(), client, subscription);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }

    abstract void onParsedMessage(
        PagedResource<T> resource, StompWebSocketClient client, StompSubscription subscription);
  }

  private abstract static class WebSocketClientCallBack<T> implements StompWebSocketClientCallBack {

    private final ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
        onParsedMessage(response.getResource(), client, subscription);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }

    abstract void onParsedMessage(
        T resource, StompWebSocketClient client, StompSubscription subscription);
  }
}
