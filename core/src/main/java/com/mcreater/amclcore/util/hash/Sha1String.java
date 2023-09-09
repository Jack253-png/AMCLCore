package com.mcreater.amclcore.util.hash;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sha1String {
    @Getter
    private final String raw;

    public static Sha1String create(String s) {
        return new Sha1String(s);
    }

    public boolean validate(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[1024 * 1024 * 10];

            int len;
            while ((len = in.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }
            String sha1 = new BigInteger(1, digest.digest()).toString(16);
            int length = 40 - sha1.length();
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    sha1 = "0" + sha1;
                }
            }
            return sha1.equals(raw);
        } catch (IOException e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.IO);
        } catch (NoSuchAlgorithmException e) {
            ExceptionReporter.report(e);
        }
        return false;
    }

    public String toString() {
        return raw;
    }
}
