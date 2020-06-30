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
package com.chatkitty.model.channel.response;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.chatkitty.model.Channel;
import com.chatkitty.pagination.PageIterator;

public class GetChannelsResult {

  private final Iterator<Channel> iterator;

  @Nullable private String nextDestination;

  public GetChannelsResult(List<Channel> channels, @Nullable String nextDestination) {
    this.iterator = channels.iterator();
    this.nextDestination = nextDestination;
  }

  public PageIterator<Channel> iterator() {
    return new PageIterator<Channel>() {
      @Override
      public boolean hasNext() {
        if (!iterator.hasNext() && nextDestination != null) {
          // TODO - Call pagination socket.
        }
        return iterator.hasNext();
      }

      @Override
      public Channel next() {
        return iterator.next();
      }
    };
  }
}
