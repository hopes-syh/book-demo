package com.syh.serialthreadconfinement_11;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-7
 * Time: 下午6:41
 * To change this template use File | Settings | File Templates.
 */
public interface TaskProcessor<T, V> {

    V doProcess(T task) throws Exception;
}
