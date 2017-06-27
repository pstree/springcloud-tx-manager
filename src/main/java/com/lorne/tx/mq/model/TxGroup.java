package com.lorne.tx.mq.model;

import com.lorne.core.framework.model.JsonModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lorne on 2017/6/7.
 */
public class TxGroup extends JsonModel{


    private String groupId;


    private String consumerUrl;

    private boolean hasOver = false;

    private int waitTime;

    public boolean isHasOver() {
        return hasOver;
    }

    public void hasOvered() {
        this.hasOver = true;
    }

    private List<TxInfo> list;

    public TxGroup() {
        list = new ArrayList<>();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<TxInfo> getList() {
        return list;
    }

    public void setHasOver(boolean hasOver) {
        this.hasOver = hasOver;
    }

    public void setList(List<TxInfo> list) {
        this.list = list;
    }

    public void addTransactionInfo(TxInfo info){
        if(!hasOver){
            list.add(info);
        }
    }


    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }


    public String getConsumerUrl() {
        return consumerUrl;
    }

    public void setConsumerUrl(String consumerUrl) {
        this.consumerUrl = consumerUrl;
    }


    public static TxGroup parser(String json){
        try {
            JSONObject jsonObject = JSONObject.fromObject(json);

            TxGroup txGroup = new TxGroup();

            txGroup.setConsumerUrl(jsonObject.getString("consumerUrl"));
            txGroup.setGroupId(jsonObject.getString("groupId"));
            txGroup.setHasOver(jsonObject.getBoolean("hasOver"));
            txGroup.setWaitTime(jsonObject.getInt("waitTime"));
            JSONArray array  =  jsonObject.getJSONArray("list");
            int length = array.size();
            for(int i=0;i<length;i++){
                JSONObject object = array.getJSONObject(i);

                TxInfo info = new TxInfo();
                info.setState(object.getInt("state"));
                info.setKid(object.getString("kid"));
                info.setUrl(object.getString("url"));
                txGroup.getList().add(info);
            }
            return txGroup;

        }catch (Exception e){
            return null;
        }


    }
}
