package com.lorne.tx.manager.service.impl;

import com.lorne.tx.Constants;
import com.lorne.core.framework.utils.thread.CountDownLatchHelper;
import com.lorne.core.framework.utils.thread.IExecute;
import com.lorne.tx.manager.service.TransactionConfirmService;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.model.TxInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;


/**
 * Created by lorne on 2017/6/9.
 */
@Service
public class TransactionConfirmServiceImpl implements TransactionConfirmService {

    private Logger logger = LoggerFactory.getLogger(TransactionConfirmServiceImpl.class);


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void confirm(TxGroup txGroup) {
        logger.info("end:"+txGroup.toJsonString());

        boolean checkState = true;

        //检查事务是否正常
        for(TxInfo info:txGroup.getList()){
            if(info.getState()==0){
                checkState = false;
            }
        }

        //事务不满足直接回滚事务
        if(!checkState){
            transaction(txGroup.getList(),0);
            return;
        }


        //检查网络状态是否正常
        boolean netState  = checkRollback(txGroup.getList());

        if(netState){
            //通知事务
            transaction(txGroup.getList(),1);
        }else{
            transaction(txGroup.getList(),-1);
        }

    }



    /**
     * 检查事务是否提交
     * @param list
     */
    private boolean checkRollback(List<TxInfo> list){
        boolean isOK = true;
        CountDownLatchHelper<Boolean> countDownLatchHelper = new CountDownLatchHelper<>();
        for(final TxInfo info:list){
            final  String url = info.getUrl();
            if (StringUtils.isNotEmpty(url)) {
                countDownLatchHelper.addExecute(new IExecute<Boolean>() {
                    @Override
                    public Boolean execute() {
                        String serverUrl =  url+"/tx/transaction/checkRollback?kid={kid}";
                        String  res = restTemplate.getForObject(serverUrl,String.class,info.getKid());
                        return res!=null?res.contains("true"):false;
                    }
                });
            }else{
                isOK = false;
                break;
            }
        }
        if(isOK){
            List<Boolean> resList =  countDownLatchHelper.execute().getData();
            for(Boolean bool:resList){
                if(bool){
                    return false;
                }
            }
            return true;
        }else {
            return false;
        }
    }



    /**
     * 事务提交或回归
     * @param list
     * @param checkSate
     */
    private void transaction(List<TxInfo> list,final int checkSate){
        for(final TxInfo info:list){
            final  String url = info.getUrl();
            if (StringUtils.isNotEmpty(url)) {
                Constants.threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        String serverUrl =  url+"/tx/transaction/notify?kid={kid}&state={state}";
                        String res = restTemplate.getForObject(serverUrl,String.class,info.getKid(),checkSate);
                        logger.info(serverUrl+"->"+res);
                    }
                });
            }

        }
    }


}
