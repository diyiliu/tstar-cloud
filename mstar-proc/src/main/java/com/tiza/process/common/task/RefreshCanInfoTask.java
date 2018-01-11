package com.tiza.process.common.task;

import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.task.ITask;
import com.diyiliu.common.util.CommonUtil;
import com.tiza.process.common.dao.FunctionDao;
import com.tiza.process.common.model.CanPackage;
import com.tiza.process.common.model.FunctionInfo;
import com.tiza.process.common.model.NodeItem;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: RefreshCanInfoTask
 * Author: DIYILIU
 * Update: 2017-10-09 14:29
 */

public class RefreshCanInfoTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache functionCacheProvider;

    @Resource
    private FunctionDao functionDao;

    @Override
    public void execute() {
        logger.info("刷新车辆功能集信息...");

        List<FunctionInfo> functionInfoList = functionDao.selectCanInfo();
        refresh(functionInfoList, functionCacheProvider);
    }

    private void refresh(List<FunctionInfo> functionInfoList, ICache functionCache) {

        if (functionInfoList == null || functionInfoList.size() < 1) {
            logger.warn("功能集数据为空！");
            return;
        }

        Set oldKeys = functionCacheProvider.getKeys();
        Set tempKeys = new HashSet<>(functionInfoList.size());

        for (FunctionInfo functionInfo : functionInfoList) {
            //logger.warn("FunctionInfo:[{}]", functionInfo.getSoftVersion());
            dealCan(functionInfo);
            dealStatus(functionInfo);

            functionCache.put(functionInfo.getSoftVersion(), functionInfo);
            tempKeys.add(functionInfo.getSoftVersion());
        }

        // 被删除的
        Collection subKeys = CollectionUtils.subtract(oldKeys, tempKeys);
        for (Iterator iterator = subKeys.iterator(); iterator.hasNext(); ) {
            int key = (int) iterator.next();
            functionCache.remove(key);
        }
    }

    private void dealCan(FunctionInfo functionInfo) {
        String xml = functionInfo.getFunctionXml();
        if (CommonUtil.isEmpty(xml)) {
            return;
        }

        try {
            Document document = DocumentHelper.parseText(xml);
            List<Node> rootPackageNodes = document.selectNodes("root/can/package");

            Map<String, CanPackage> canPackages = new HashedMap();
            Map emptyValues = new HashedMap();

            int pidLength = 0;
            for (Node node : rootPackageNodes) {
                CanPackage canPackage = dealPackage(node);
                canPackages.put(canPackage.getPackageId(), canPackage);

                emptyValues.putAll(canPackage.getEmptyValues());
                pidLength = canPackage.getIdLength();
            }
            functionInfo.setCanPackages(canPackages);

            functionInfo.setPidLength(pidLength);
            functionInfo.setEmptyValues(emptyValues);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void dealStatus(FunctionInfo functionInfo) {

        String xml = functionInfo.getFunctionXml();
        if (CommonUtil.isEmpty(xml)) {
            return;
        }

        try {
            Document document = DocumentHelper.parseText(xml);
            List<Node> nodes = document.selectNodes("root/status/item");

            List<NodeItem> list = new ArrayList<>();
            if (nodes != null && nodes.size() > 0) {
                for (Node node : nodes) {
                    NodeItem nodeItem = dealItem(node);
                    if (nodeItem != null) {
                        list.add(nodeItem);
                    }
                }
            }
            functionInfo.setStatusItems(list);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


    private CanPackage dealPackage(Node packageNode) {
        String packageId = packageNode.valueOf("@id");
        int length = Integer.parseInt(packageNode.valueOf("@length"));

        CanPackage canPackage = new CanPackage(packageId, length);
        List<Node> nodeItems = packageNode.selectNodes("item");

        if (nodeItems != null && nodeItems.size() > 0) {
            List<NodeItem> itemList = new ArrayList();
            Map emptyValues = new HashedMap();
            for (Node node : nodeItems) {
                NodeItem nodeItem = dealItem(node);
                if (nodeItem != null) {
                    itemList.add(nodeItem);
                    emptyValues.put(nodeItem.getField().toUpperCase(), null);
                }
            }
            canPackage.setItemList(itemList);
            canPackage.setEmptyValues(emptyValues);
        }

        return canPackage;
    }

    private NodeItem dealItem(Node itemNode) {
        NodeItem itemBean = null;
        try {
            String field = itemNode.selectSingleNode("field").getText();
            String nameKey = itemNode.selectSingleNode("nameKey").getText();
            String name = itemNode.selectSingleNode("name").getText();
            String type = itemNode.selectSingleNode("type").getText();
            String endian = itemNode.selectSingleNode("endian") == null ? "big" : itemNode.selectSingleNode("endian").getText();
            Node position = itemNode.selectSingleNode("position");
            Node byteNode = position.selectSingleNode("byte");
            Node bitNode = byteNode.selectSingleNode("bit");
            String byteStart = byteNode.valueOf("@start");
            String byteLen = byteNode.valueOf("@length");
            String expression = itemNode.selectSingleNode("expression").getText();

            itemBean = new NodeItem();
            if (null == bitNode) {
                itemBean.setOnlyByte(true);
            } else {
                itemBean.setOnlyByte(false);
                String bitStart = bitNode.valueOf("@start");
                String bitLen = bitNode.valueOf("@length");
                itemBean.setBitStart(Integer.parseInt(bitStart));
                itemBean.setBitLen(Integer.parseInt(bitLen));
            }
            itemBean.setNameKey(nameKey);
            itemBean.setName(name);
            itemBean.setType(type);
            itemBean.setEndian(endian);
            itemBean.setByteStart(Integer.parseInt(byteStart));
            itemBean.setByteLen(Integer.parseInt(byteLen));
            itemBean.setExpression(expression);
            itemBean.setField(field);
        } catch (Exception e) {
            logger.error("解析功能集错误![{}]", e.getMessage());
            e.printStackTrace();
        }

        return itemBean;
    }
}
