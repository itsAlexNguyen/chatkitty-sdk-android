package com.chatkitty.configuration;

public final class ChatKittyConfiguration {

  private final String apiKey;
  private String webSocketBaseUrl = "wss://api.chatkitty.com";
  private String webSocketEndpoint = "/stompx/websocket";

  public ChatKittyConfiguration(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiKey() {
    return this.apiKey;
  }

  public String getWebSocketBaseUrl() {
    return webSocketBaseUrl;
  }

  public void setWebSocketBaseUrl(String webSocketBaseUrl) {
    this.webSocketBaseUrl = webSocketBaseUrl;
  }

  public String getWebSocketEndpoint() {
    return webSocketEndpoint;
  }

  public void setWebSocketEndpoint(String webSocketEndpoint) {
    this.webSocketEndpoint = webSocketEndpoint;
  }
}
