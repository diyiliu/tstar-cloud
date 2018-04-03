package com.tiza.process.protocol.m2.handler;

import cn.com.tiza.tstar.common.process.BaseHandle;
import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.model.IDataProcess;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.common.config.MStarConstant;
import com.tiza.process.protocol.m2.M2DataProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: M2ParseHandler
 * Author: DIYILIU
 * Update: 2017-09-14 14:24
 */
public class M2ParseHandler extends BaseHandle{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public RPTuple handle(RPTuple rpTuple) throws Exception {
        logger.info("收到终端[{}], 指令[{}]...", rpTuple.getTerminalID(), CommonUtil.toHex(rpTuple.getCmdID()));

        ICache cmdCacheProvider = SpringUtil.getBean("cmdCacheProvider");
        IDataProcess process = (IDataProcess) cmdCacheProvider.get(rpTuple.getCmdID());
        if (process == null) {
            logger.error("无法找到[{}]指令解析器!", CommonUtil.toHex(rpTuple.getCmdID()));
            return null;
        }

        // 将conf配置信息放入上下文中
        rpTuple.getContext().put(MStarConstant.Kafka.TRACK_TOPIC, processorConf.get("trackTopic"));
        rpTuple.getContext().put(MStarConstant.Kafka.WORK_TIME_TOPIC, processorConf.get("workTimeTopic"));

        M2Header header = (M2Header)process.dealHeader(rpTuple.getMsgBody());
        header.settStarData(rpTuple);
        process.parse(header.getContent(), header);

        return rpTuple;
    }

    @Override
    public void init() throws Exception {
        // 加载配置信息
        MStarConstant.init("init-sql.xml", processorConf);

        // 装载Spring容器
        SpringUtil.init();

        // 装载tstar handle
        M2DataProcess.setHandle(this);
    }
}
