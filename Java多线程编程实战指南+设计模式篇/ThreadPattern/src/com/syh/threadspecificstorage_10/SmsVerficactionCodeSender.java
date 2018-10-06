package com.syh.threadspecificstorage_10;

import java.text.DecimalFormat;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-5
 * Time: 下午10:07
 * To change this template use File | Settings | File Templates.
 */
public class SmsVerficactionCodeSender {

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 60,
            TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "VerfCodeSender");
            t.setDaemon(true);
            return t;
        }
    }, new ThreadPoolExecutor.DiscardPolicy());

    public static void main(String[] args){
        SmsVerficactionCodeSender client = new SmsVerficactionCodeSender();
        client.sendVerificationSms("15920320202");
        client.sendVerificationSms("15920320203");
        client.sendVerificationSms("15920320204");
    }

    private void sendVerificationSms(final String msisdn) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                int verificationCode = ThreadSpecificSecureRandom.INSTANCE.nextInt(999999);
                DecimalFormat df = new DecimalFormat("000000");
                String txtVerCode = df.format(verificationCode);

                sendSms(msisdn, txtVerCode);
            }
        };

        EXECUTOR.submit(task);
    }

    private void sendSms(String msisdn, String txtVerCode) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
