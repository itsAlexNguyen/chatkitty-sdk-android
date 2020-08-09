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
package com.chatkitty.model.message;

import com.chatkitty.model.user.response.UserProperties;
import org.jetbrains.annotations.Nullable;

public abstract class Message {
  private int id;

  private String type;

  @Nullable
  private UserProperties user;

  private Relays _relays;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Relays get_relays() {
    return _relays;
  }

  public void set_relays(Relays _relays) {
    this._relays = _relays;
  }

  @Nullable
  public UserProperties getUser() {
    return user;
  }

  public void setUser(@Nullable UserProperties user) {
    this.user = user;
  }

  public static class Relays {
    private String self;

    private String user;

    public String getSelf() {
      return self;
    }

    public void setSelf(String self) {
      this.self = self;
    }

    public String getUser() {
      return user;
    }

    public void setUser(String user) {
      this.user = user;
    }
  }
}
