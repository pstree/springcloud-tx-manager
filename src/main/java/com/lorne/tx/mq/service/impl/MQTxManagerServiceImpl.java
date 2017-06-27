package com.lorne.tx.mq.service.impl;

import com.lorne.tx.manager.service.TxManagerService;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.service.MQTxManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lorne on 2017/6/7.
 */
@Service
public class MQTxManagerServiceImpl implements MQTxManagerService {


    @Autowired
    private TxManagerService txManagerService;


    @Override
    public TxGroup createTransactionGroup(int port) {
        return txManagerService.createTransactionGroup(port);
    }

    @Override
    public TxGroup addTransactionGroup(String groupId,String taskId,int port) {
        return txManagerService.addTransactionGroup(groupId,taskId,port);
    }

    @Override
    public boolean closeTransactionGroup(String groupId) {
        return txManagerService.closeTransactionGroup(groupId);
    }


    @Override
    public boolean notifyTransactionInfo(String groupId, String kid, boolean state) {
        return txManagerService.notifyTransactionInfo(groupId, kid, state);
    }
}
