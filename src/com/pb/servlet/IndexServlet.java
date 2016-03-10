package com.pb.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.pb.entity.OrderMessage;
import com.pb.entity.QueryOrder;
import com.pb.util.HttpRequest;
import com.pb.util.Sign;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * 接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
 */
@WebServlet("/IndexServlet")
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public IndexServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// 测试查询订单接口
		QueryOrder o = new QueryOrder();
		// 读取配置文件
		Prop prop = PropKit.use("config.properties");
		String appid = prop.get("appid");
		String mch_id = prop.get("mch_id");
		String nonce_str = UUID.randomUUID().toString().trim()
				.replaceAll("-", "");
		String out_trade_no = "20160310114157104661";
		o.setAppid(appid);
		o.setMch_id(mch_id);
		o.setNonce_str(nonce_str);
		o.setOut_trade_no(out_trade_no);
		SortedMap<Object, Object> p = new TreeMap<Object, Object>();
		p.put("appid", appid);
		p.put("mch_id", mch_id);
		p.put("nonce_str", nonce_str);
		p.put("out_trade_no", out_trade_no);
		String key = prop.get("key");
		// 得到签名
		String sign = Sign.createSign("utf-8", p, key);
		o.setSign(sign);
		// 转换为XML
		XStream xstream = new XStream(new XppDriver(new XmlFriendlyNameCoder(
				"_-", "_")));
		xstream.alias("xml", QueryOrder.class);
		String xml = xstream.toXML(o);
		System.out.println(xml);
		String url = "https://api.mch.weixin.qq.com/pay/orderquery";
		String returnXml = HttpRequest.sendPost(url, xml);
		System.out.println(returnXml);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// 支付成功后，接受微信反馈的参数
		XStream xstream = new XStream(new XppDriver(new XmlFriendlyNameCoder(
				"_-", "_")));
		ServletInputStream s = request.getInputStream();
		xstream.alias("xml", OrderMessage.class);
		OrderMessage om = (OrderMessage) xstream.fromXML(s);
		// 加入自己的业务处理，如更新订单状态
		System.out.println(om.toString());
		// 支付完成后，微信会把相关支付结果和用户信息发送给商户，商户需要接收处理，并返回应答。
		String rs = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
		PrintWriter pw = response.getWriter();
		pw.print(rs);
		pw.flush();
		pw.close();
	}

}
