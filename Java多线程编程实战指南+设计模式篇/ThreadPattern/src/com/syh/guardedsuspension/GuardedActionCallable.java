package com.syh.guardedsuspension;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-9-24
 * Time: 下午9:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class GuardedActionCallable<V> implements Callable<V> {
    protected final Predicate guard;

    protected GuardedActionCallable(Predicate guard) {
        this.guard = guard;
    }
}
