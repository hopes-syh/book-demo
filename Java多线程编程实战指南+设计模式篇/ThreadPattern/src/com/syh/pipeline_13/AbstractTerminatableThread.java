package com.syh.pipeline_13;

import com.syh.pipeline_13.decorator.TerminationToken;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-21
 * Time: 下午8:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTerminatableThread extends Thread {

    public AbstractTerminatableThread(TerminationToken token){

    }

    protected abstract void doRun() throws Exception;


    public void terminate() {

    }
}
