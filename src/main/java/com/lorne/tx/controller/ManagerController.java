package com.lorne.tx.controller;

import com.lorne.tx.manager.service.TxManagerService;
import com.lorne.tx.mq.model.TxGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lorne on 2017/6/27.
 */
@RestController
@RequestMapping("/tx/manager")
public class ManagerController {


    @Autowired
    private TxManagerService managerService;

    /**
     * 创建事务组
     * @return
     */
    @RequestMapping("/createTransactionGroup")
    public TxGroup createTransactionGroup(@RequestParam("port") int port){
       return managerService.createTransactionGroup(port);
    }


    /**
     * 添加事务组子对象
     * @return
     */
    @RequestMapping("/addTransactionGroup")
    public TxGroup addTransactionGroup(@RequestParam("groupId") String groupId,@RequestParam("taskId") String taskId,@RequestParam("port") int port){
        return managerService.addTransactionGroup(groupId, taskId,port);
    }


    /**
     * 关闭事务组-进入事务提交第一阶段
     * @param groupId
     * @return
     */
    @RequestMapping("/closeTransactionGroup")
    public boolean closeTransactionGroup(@RequestParam("groupId") String groupId){
        return managerService.closeTransactionGroup(groupId);
    }


    /**
     * 通知事务组事务执行状态
     * @param groupId
     * @param kid
     * @param state
     * @return
     */
    @RequestMapping("/notifyTransactionInfo")
    public boolean notifyTransactionInfo(@RequestParam("groupId") String groupId,@RequestParam("kid")  String kid,@RequestParam("state")  boolean state){
        return managerService.notifyTransactionInfo(groupId,kid,state);
    }


}
