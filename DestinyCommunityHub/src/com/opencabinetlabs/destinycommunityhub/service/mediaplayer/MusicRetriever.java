/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opencabinetlabs.destinycommunityhub.service.mediaplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Retrieves and organizes media to play. Before being used, you must call {@link #prepare()},
 * which will retrieve all of the music on the user's device (by performing a query on a content
 * resolver). After that, it's ready to retrieve a random song, with its title and URI, upon
 * request.
 */
public class MusicRetriever {
    final String TAG = "MusicRetriever";

    ContentResolver mContentResolver;

    // the items (songs) we have queried
    List<Item> mItems = new ArrayList<Item>();

    public MusicRetriever(ContentResolver cr) {
        mContentResolver = cr;
    }

    public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    public static class Item {
        long id;
        String artist;
        String title;
        String album;
        long duration;
        int artwork; //drawable id

        public Item(long id, String artist, String title, String album, long duration, int artwork) {
            this.id = id;
            this.artist = artist;
            this.title = title;
            this.album = album;
            this.duration = duration;
            this.artwork = artwork;
        }

        public long getId() {
            return id;
        }

        public String getArtist() {
            return artist;
        }

        public String getTitle() {
            return title;
        }

        public String getAlbum() {
            return album;
        }

        public long getDuration() {
            return duration;
        }
        
        public int getArtwork() {
        	return artwork;
        }

        public Uri getURI() {
            return ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        }
    }
}
