package com.mcreater.amclcore.util;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;

public class ImageUtil {
    public static boolean isValidImage(File path) {
        try {
            ImageIO.read(path);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isValidImage(InputStream path) {
        try {
            ImageIO.read(path);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
