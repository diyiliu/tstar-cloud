package com.diyiliu.common.map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MapUtil {
	private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);

	private static Object lock = new Object();
	static TTransport transport;
	static TProtocol protocol;
	static ILocationService.Client client;

	public MapUtil(String map_loaction) {
		int thriftPort = 9999;
		String thriftIp = map_loaction;
		// TTransport transport;
		// transport = new TSocket(thriftIp, thriftPort, 3000);
		// protocol = new TCompactProtocol(transport);
		// client = new ILocationService.Client(protocol);
		// 使用非阻塞方式，按块的大小进行传输，类似于Java中的NIO。记得调用close释放资源
		transport = new TFramedTransport(new TSocket(thriftIp, thriftPort, 3000));
		// 高效率的、密集的二进制编码格式进行数据传输协议
		protocol = new TCompactProtocol(transport);
		client = new ILocationService.Client(protocol);
	}

	/**
	 * 初始化地图服务器参数
	 * @param thriftIp
	 */
	public static void init(String thriftIp){
		// 使用非阻塞方式，按块的大小进行传输，类似于Java中的NIO。记得调用close释放资源
		transport = new TFramedTransport(new TSocket(thriftIp, 9999, 3000));
		// 高效率的、密集的二进制编码格式进行数据传输协议
		protocol = new TCompactProtocol(transport);
		client = new ILocationService.Client(protocol);

		logger.info("初始化[thrift]地图服务器...");
	}

	/**
	 *
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static MapLocation getArea(double latitude, double longitude) {
		synchronized (lock) {
			MapLocation result;
			try {
				transport.open();
				result = client.getLocation(longitude, latitude);
				transport.close();
			} catch (TException e) {
				System.out.println("获取省市区失败：" + e.getMessage());
				result = new MapLocation();
				result.setCountry("");
				result.setProvince("");
				result.setCity("");
				result.setTown("");
				result.setLatitude(0);
				result.setLongtitude(0);
			}

			return result;
		}
	}


	/**
	 * 
	 * @param latitude
	 * @param longitude
	 * @param isS
	 * @param isW
	 * @return
	 */
	public static MapLocation getArea(double latitude, double longitude, boolean isS, boolean isW) {
		synchronized (lock) {
			if (isS) {
				latitude = -latitude;
			}
			if (isW) {
				longitude = -longitude;
			}

			return getArea(latitude, longitude);
		}
	}
}
