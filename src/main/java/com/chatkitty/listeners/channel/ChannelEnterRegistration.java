package com.chatkitty.listeners.channel;

public class ChannelEnterRegistration {

  private final Subscribe subscribe;

  public interface Subscribe {

    void onExit();
  }

  private ChannelEnterRegistration(Subscribe subscribe) {
    this.subscribe = subscribe;
  }

  public static ChannelEnterRegistration create(Subscribe subscribe) {
    return new ChannelEnterRegistration(subscribe);
  }

  public void exitChannel() {
    subscribe.onExit();
  }
}
