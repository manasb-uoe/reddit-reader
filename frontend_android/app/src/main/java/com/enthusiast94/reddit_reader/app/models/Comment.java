package com.enthusiast94.reddit_reader.app.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.enthusiast94.reddit_reader.app.utils.Helpers;

/**
 * Created by manas on 07-09-2015.
 */
public class Comment implements Parcelable {

    String fullName;
    String body;
    int score;
    int likes;
    String author;
    String created;
    int level;

    public Comment() {
        // empty constructor
    }

    public Comment(Parcel parcel) {
        readFromParcel(parcel);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(Boolean likes) {
        if (likes == null) {
            this.likes = 0;
        } else if (likes) {
            this.likes = 1;
        } else {
            this.likes = -1;
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = Helpers.humanizeTimestamp(created);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fullName);
        parcel.writeString(body);
        parcel.writeInt(score);
        parcel.writeInt(likes);
        parcel.writeString(author);
        parcel.writeString(created);
        parcel.writeInt(level);
    }

    private void readFromParcel(Parcel parcel) {
        fullName = parcel.readString();
        body = parcel.readString();
        score = parcel.readInt();
        likes = parcel.readInt();
        author = parcel.readString();
        created = parcel.readString();
        level = parcel.readInt();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {

        @Override
        public Comment createFromParcel(Parcel parcel) {
            return new Comment(parcel);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
