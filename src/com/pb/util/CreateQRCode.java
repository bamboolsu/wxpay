package com.pb.util;

import java.net.URLEncoder;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.pb.entity.Order;
import com.pb.entity.OrderReturn;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class CreateQRCode {

	public static void main(String[] args) throws Exception {
		// 读取配置文件
		Prop prop = PropKit.use("config.properties");
		Order o = new Order();
		String appid = prop.get("appid");
		String mch_id = prop.get("mch_id");
		String nonce_str = UUID.randomUUID().toString().trim()
				.replaceAll("-", "");
		String body = "商品或支付单简要描述";
		// java web项目乱码
		body = URLEncoder.encode(body);
		String out_trade_no = new Date().getTime() + "";
		int total_fee = 1;
		String spbill_create_ip = prop.get("spbill_create_ip");
		String notify_url = prop.get("notify_url");
		String trade_type = prop.get("trade_type");
		String key = prop.get("key");
		o.setAppid(appid);
		o.setBody(body);
		o.setMch_id(mch_id);
		o.setNotify_url(notify_url);
		o.setOut_trade_no(out_trade_no);
		o.setTotal_fee(total_fee);
		o.setNonce_str(nonce_str);
		o.setTrade_type(trade_type);
		o.setSpbill_create_ip(spbill_create_ip);
		SortedMap<Object, Object> p = new TreeMap<Object, Object>();
		p.put("appid", appid);
		p.put("mch_id", mch_id);
		p.put("body", body);
		p.put("nonce_str", nonce_str);
		p.put("out_trade_no", out_trade_no);
		p.put("total_fee", total_fee);
		p.put("spbill_create_ip", spbill_create_ip);
		p.put("notify_url", notify_url);
		p.put("trade_type", trade_type);
		// 得到签名
		String sign = Sign.createSign("utf-8", p, key);
		System.out.println("nonce_str=" + nonce_str);
		System.out.println("sign=" + sign);
		o.setSign(sign);
		// 转换为XML
		XStream xstream = new XStream(new XppDriver(new XmlFriendlyNameCoder(
				"_-", "_")));
		xstream.alias("xml", Order.class);
		String xml = xstream.toXML(o);
		System.out.println(xml);
		// 统一下单
		String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		// String returnXml = HttpUtil.doPostStr(url, xml);
		String returnXml = HttpRequest.sendPost(url, xml);
		XStream xstream2 = new XStream(new DomDriver());
		xstream2.alias("xml", OrderReturn.class);
		OrderReturn or = (OrderReturn) xstream2.fromXML(returnXml);
		System.out.println(or.toString());
		// 创建支付二维码
		QRCode q = new QRCode();
		String imgPath = prop.get("imgPath");
		q.encoderQRCode(or.getCode_url(), imgPath);
	}
}
