package com.brucetoo.wifitransport.HotPot.image;

/**
 * Created by Bruce Too
 * On 7/8/16.
 * At 14:56
 */
public class Image {

    public String path;
    public String name;
    public long time;

    public Image(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Image path:" + path + "  name:" + name + "  time:" + time;
    }
}
