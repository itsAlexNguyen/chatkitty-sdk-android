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
package com.chatkitty.model;

import org.jetbrains.annotations.Nullable;

public class PagedResource<Model> {

  private Model _embedded;

  private Relays _relays;

  public Relays get_relays() {
    return _relays;
  }

  public void set_relays(Relays _relays) {
    this._relays = _relays;
  }

  public Model get_embedded() {
    return _embedded;
  }

  public void set_embedded(Model _embedded) {
    this._embedded = _embedded;
  }

  public class Relays {

    private String self;
    @Nullable private String next;

    public String getSelf() {
      return self;
    }

    public void setSelf(String self) {
      this.self = self;
    }

    @Nullable
    public String getNext() {
      return next;
    }

    public void setNext(@Nullable String next) {
      this.next = next;
    }
  }
}
