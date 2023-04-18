package com.example.redisdemo.utils;

import java.util.Random;

public class ExpireUtil {

    private static final Random random = new Random();


    /**
     * 生成随机过期时间
     *
     * @return 随机过期时间
     */
    public static int getExpireTime() {
        // 生成[30, 60)之间的数字
        return random.nextInt(30) + 30;
    }
}
