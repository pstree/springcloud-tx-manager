package com.lorne.tx.manager.service.impl;

import com.lorne.core.framework.utils.KidUtils;
import com.lorne.core.framework.utils.redis.RedisUtil;
import com.lorne.tx.manager.service.TransactionConfirmService;
import com.lorne.tx.manager.service.TxManagerService;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.model.TxInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by lorne on 2017/6/7.
 */
@Service
public class TxManagerServiceImpl implements TxManagerService {


    private  static  int redis_save_max_time ;

    private static  int transaction_wait_max_time ;

    private final static String key_prefix = "tx_manager_";

    @Autowired
    private TransactionConfirmService transactionConfirmService;

    @Autowired
    private Environment env;


    public TxManagerServiceImpl() {
        try {
            redis_save_max_time = env.getProperty("redis_save_max_time",Integer.class);
            transaction_wait_max_time = env.getProperty("transaction_wait_max_time",Integer.class);
        }catch (Exception e){
             redis_save_max_time = 30;
              transaction_wait_max_time = 5;
        }
    }

    private String getRemoteUrl(int port){
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        String url = "http://"+request.getRemoteHost()+":"+port;
        return url;
    }

    @Override
    public TxGroup createTransactionGroup(int port) {

        String groupId = KidUtils.getKid();
        TxGroup txGroup = new TxGroup();
        txGroup.setGroupId(groupId);
        txGroup.setWaitTime(transaction_wait_max_time);
        txGroup.setConsumerUrl(getRemoteUrl(port));

        Jedis jedis = RedisUtil.getJedis();
        String key = key_prefix+groupId;
        jedis.setex(key, redis_save_max_time, txGroup.toJsonString());
        RedisUtil.returnResource(jedis);

        return txGroup;
    }

    @Override
    public TxGroup addTransactionGroup(String groupId,String taskId,int port) {
        Jedis jedis = RedisUtil.getJedis();
        String key = key_prefix+groupId;
        String json = jedis.get(key);
        try {
            TxGroup txGroup =  TxGroup.parser(json);
            TxInfo txInfo = new TxInfo();
            txInfo.setKid(taskId);
            txInfo.setUrl(getRemoteUrl(port));
            if(txGroup !=null){
                txGroup.addTransactionInfo(txInfo);
                jedis.setex(key, redis_save_max_time, txGroup.toJsonString());
                return txGroup;
            }
        } catch (Exception e) {
            return null;
        }finally {
            RedisUtil.returnResource(jedis);
        }
        return null;
    }

    @Override
    public boolean closeTransactionGroup(String groupId) {
        Jedis jedis = RedisUtil.getJedis();
        String key = key_prefix+groupId;
        String json = jedis.get(key);
        try {
            TxGroup txGroup =  TxGroup.parser(json);
            txGroup.hasOvered();

            jedis.del(groupId);

            transactionConfirmService.confirm(txGroup);

            return true;
        } catch (Exception e) {
            return false;
        }finally {
            RedisUtil.returnResource(jedis);
        }
    }


    @Override
    public boolean notifyTransactionInfo(String groupId, String kid, boolean state) {
        Jedis jedis = RedisUtil.getJedis();
        String key = key_prefix+groupId;
        String json = jedis.get(key);
        try {
            TxGroup txGroup =  TxGroup.parser(json);

            List<TxInfo> list =  txGroup.getList();

              for(TxInfo info:list){
                  if(info.getKid().equals(kid)){
                      info.setState(state?1:0);
                  }
              }
            jedis.setex(key, redis_save_max_time, txGroup.toJsonString());
          return true;
        } catch (Exception e) {
            return false;
        }finally {
            RedisUtil.returnResource(jedis);
        }
    }
}
