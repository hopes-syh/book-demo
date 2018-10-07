package com.syh.serialthreadconfinement_11;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-7
 * Time: 下午5:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTerminatableThread extends Thread {

    public void start(){
        super.start();
        try {
            doRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void doRun() throws  Exception;

    public void terminate(){
        doCleanUp();
    }

    protected abstract void doCleanUp();

}
