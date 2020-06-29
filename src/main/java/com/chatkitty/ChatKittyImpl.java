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

import com.chatkitty.stompx.stompx.StompWebSocketClient;
import com.chatkitty.stompx.stompx.WebSocketConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.internal.Nullable;
import okhttp3.OkHttpClient;

public class ChatKittyImpl implements ChatKitty {
  private final String apiKey;
  @Nullable
  private StompWebSocketClient client;

  public ChatKittyImpl(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public void startSession(String username, ChatKittyCallback callback) {
    WebSocketConfiguration configuration = new WebSocketConfiguration(apiKey,
        username, "http://staging-api.chatkitty.com", "/stompx");

    client = new StompWebSocketClient(new OkHttpClient(), new ObjectMapper(), configuration);
    // TODO - Subscribe to client, when object receive use the ChatKittyCallback.
    client.start();
  }
}
