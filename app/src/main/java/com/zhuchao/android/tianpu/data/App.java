package com.zhuchao.android.tianpu.data;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.zhuchao.android.tianpu.utils.AppMain;

/**
 * Created by Oracle on 2017/12/1.
 */

public class App implements Parcelable {

    private String name;
    private Drawable icon;
    private String packageName;

    public App() {}

    protected App(Parcel in) {
        name = in.readString();
        packageName = in.readString();
        Bitmap bitmap = in.readParcelable(getClass().getClassLoader());
        icon = new BitmapDrawable(AppMain.res(), bitmap);
    }

    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App createFromParcel(Parcel in) {
            return new App(in);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return String.format("APP[name:%s packageName:%s]", name, packageName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(packageName);
        dest.writeParcelable(((BitmapDrawable)icon).getBitmap(), flags);
    }
}
