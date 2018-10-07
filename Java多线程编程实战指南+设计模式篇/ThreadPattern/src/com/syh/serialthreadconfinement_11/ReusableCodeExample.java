package com.syh.serialthreadconfinement_11;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-7
 * Time: 下午7:09
 * To change this template use File | Settings | File Templates.
 */
public class ReusableCodeExample {

    private static class Task{
        public final String message;
        public final int id;

        private Task(String message, int id) {
            this.message = message;
            this.id = id;
        }
    }

    private static class SomeSerivce extends AbstractSerializer<Task, String> {
        public SomeSerivce(){
            super(new ArrayBlockingQueue<Runnable>(100),
                    new TaskProcessor<Task, String>() {
                        @Override
                        public String doProcess(Task task) throws Exception {
                            System.out.println("["+ task.id +"]:"+ task.message);
                            return task.message;
                        }
                    });
        }


        @Override
        protected Task makeTask(Object... params) {
            String message = (String) params[0];
            int id = (Integer) params[1];
            return new Task(message, id);
        }

        public Future<String> downloadFile(String message, int id) throws InterruptedException {
            return service(message, id);
        }
    }
}
