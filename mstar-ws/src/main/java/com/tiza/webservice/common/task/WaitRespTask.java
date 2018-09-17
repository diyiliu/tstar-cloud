package com.tiza.webservice.common.task;

import cn.com.tiza.tstar.datainterface.client.TStarStandardClient;
import cn.com.tiza.tstar.datainterface.client.entity.ClientCmdCheckResult;
import cn.com.tiza.tstar.datainterface.client.entity.ClientCmdSendResult;
import cn.com.tiza.tstar.datainterface.service.ServerException;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.model.IDataProcess;
import com.diyiliu.common.task.ITask;
import com.diyiliu.common.util.CommonUtil;
import com.tiza.webservice.common.config.Constant;
import com.tiza.webservice.common.model.SendMSG;
import com.tiza.webservice.protocol.bean.M2Header;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: WaitRespTask
 * Author: DIYILIU
 * Update: 2016-03-22 9:17
 */
public class WaitRespTask implements ITask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${tstar.waitTime}")
    private int waitTime = 3;

    @Value("${tstar.waitCount}")
    private int waitCount = 5;

    @Resource
    private ICache waitRespCacheProvider;

    @Resource
    private IDataProcess m2DataProcess;

    @Resource
    private ICache m2CMDCacheProvider;

    @Resource
    private TStarStandardClient tStarClient;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void execute() {
        Date now = new Date();

        Set keys = waitRespCacheProvider.getKeys();
        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            // 下发ID
            Integer key = (Integer) iterator.next();

            SendMSG sendMSG = (SendMSG) waitRespCacheProvider.get(key);
            int count = sendMSG.getWaitCount() + 1;

            // 超时未响应
            if (count > waitCount) {
                logger.info("指令流水号[{}]，超时未响应！", sendMSG.getSerial());
                waitRespCacheProvider.remove(key);

                // 持久化数据库
                String sql = "UPDATE bs_instructionlog t" +
                        " SET t.sendstatus = ?" +
                        " WHERE t.id = ?";

                jdbcTemplate.update(sql, new Object[]{Constant.SendStatus.TIMEOUT, sendMSG.getId()});
                continue;
            }

            if ((now.getTime() - sendMSG.getSendTime().getTime()) > count * waitTime * 1000) {
                ClientCmdSendResult sendResult = sendMSG.getSendResult();
                sendMSG.setWaitCount(count);

                try {
                    String checkId = sendResult.getCmdCheckId();
                    ClientCmdCheckResult checkResult = tStarClient.cmdCheck(checkId);

                    boolean respIsSuccess = checkResult.getIsSuccess();
                    if (respIsSuccess) {
                        String replyBody = checkResult.getCmdReplyBody();

                        if (CommonUtil.isEmpty(replyBody)) {
                            if (waitCount > count) {

                                logger.info("指令流水号[{}]，还剩下[{}]秒响应时间...", sendMSG.getSerial(), (waitCount - count) * waitTime);
                            }
                        } else {

                            // 返回数据
                            byte[] content = Base64.decodeBase64(replyBody);
                            M2Header m2Header = (M2Header) m2DataProcess.dealHeader(content);
                            int cmd = m2Header.getCmd();

                            logger.info("指令流水号[{}]，收到响应指令[{}]...", sendMSG.getSerial(), CommonUtil.toHex(cmd));

                            if (m2CMDCacheProvider.containsKey(cmd)) {
                                m2DataProcess = (IDataProcess) m2CMDCacheProvider.get(m2Header.getCmd());
                                m2DataProcess.parse(m2Header.getContent(), m2Header);
                            }else {
                                waitRespCacheProvider.remove(sendMSG.getSerial());
                            }
                        }
                    }
                } catch (ServerException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 应答超时
     *
     * @param id
     * @param cmd
     */
    public void toDB(int id, int cmd) {

        Map valueMap = new HashMap() {
            {
                this.put("ResponseStatus", 10);
            }
        };

        Map whereMap = new HashMap();
        whereMap.put("Id", id);

//        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER, Constant.DBInfo.DB_CLOUD_INSTRUCTION, valueMap, whereMap);
    }
}
