package com.syh.pipeline_13.pipeline;

import com.syh.pipeline_13.pipe.Pipe;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-21
 * Time: 下午9:33
 * To change this template use File | Settings | File Templates.
 */
public interface Pipeline<IN, OUT> extends Pipe<IN, OUT> {

    /**
     * 往该Pipeline中，增加一个pipe实例
     * @param pipe
     */
    void addPipe(Pipe<?, ?> pipe);
}
