package com.syh.pipeline_13.pipe;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-21
 * Time: 下午10:14
 * To change this template use File | Settings | File Templates.
 */
public interface PipeContext {

    void handleError(Exception e);
}
