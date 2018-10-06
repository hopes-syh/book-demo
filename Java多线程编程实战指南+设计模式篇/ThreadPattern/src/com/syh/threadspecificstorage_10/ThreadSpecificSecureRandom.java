package com.syh.threadspecificstorage_10;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-5
 * Time: 下午9:49
 * To change this template use File | Settings | File Templates.
 */
public class ThreadSpecificSecureRandom {
    // 该类的唯一实例
    public static final ThreadSpecificSecureRandom INSTANCE = new ThreadSpecificSecureRandom();

    /**
     *  SECURE_RANDOM 相当于模式角色：ThreadSpecificStorage.TSObjectProxy;
     *  SecureRandom 相当于模式角色：ThreadSpecificStorage.TSObject;
     */
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM = new ThreadLocal<SecureRandom>(){
        @Override
        protected SecureRandom initialValue(){
            SecureRandom srnd;
            try {
                srnd = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                srnd = new SecureRandom();
            }
            return srnd;
        }
    };

    private ThreadSpecificSecureRandom(){}

    public int nextInt(int upperBound){
        SecureRandom secureRandom = SECURE_RANDOM.get();
        return secureRandom.nextInt(upperBound);
    }

    public void setSeed(long seed){
        SecureRandom secureRandom = SECURE_RANDOM.get();
        secureRandom.setSeed(seed);
    }

}
