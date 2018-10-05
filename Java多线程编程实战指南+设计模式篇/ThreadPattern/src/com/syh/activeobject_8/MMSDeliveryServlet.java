package com.syh.activeobject_8;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-5
 * Time: 上午10:09
 * To change this template use File | Settings | File Templates.
 */
public class MMSDeliveryServlet {

    public void doPost(){
        Recipient shortNumberRecipient = new Recipient();
        Recipient originalNumberRecipient = null;

        try{
            // 将接收方短号转为长号
            originalNumberRecipient = convertShortNumber(shortNumberRecipient);

        } catch (SQLException e){

            // 接收方短号转换为长号时发生数据库异常，触发请求消息的缓存
            AsyncRequestPersistence.getInstance().store(shortNumberRecipient);
        }
    }

    private Recipient convertShortNumber(Recipient shortNumberRecipient) throws SQLException{
        return null;
    }
}
