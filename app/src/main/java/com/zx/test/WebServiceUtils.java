package com.zx.test;

/**
 * Created by Administrator on 2017-03-07.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Handler;
import android.os.Message;

public class WebServiceUtils {
    public static final String WEB_SERVER_URL = "http://101.200.160.247:5653/eway/UtilImplPort?wsdl";

    // public static final String WEB_SERVER_URL = "http://192.168.0.104:7001/ewayface/EwayfaceServiceService?wsdl";

    // 含有3个线程的线程池
    private static final ExecutorService executorService = Executors
            .newFixedThreadPool(3);

    // 命名空间
    private static final String NAMESPACE = "http://itfc.eway.com/";

    /**
     * @param url                WebService服务器地址
     * @param methodName         WebService的调用方法名
     * @param properties         WebService的参数
     * @param webServiceCallBack 回调接口
     */
    public static void callWebService(String url, final String methodName,
                                      HashMap<String, String> properties,
                                      final WebServiceCallBack webServiceCallBack) {
        // 创建HttpTransportSE对象，传递WebService服务器地址
        final HttpTransportSE httpTransportSE = new HttpTransportSE(url);
        // 创建SoapObject对象
        SoapObject soapObject = new SoapObject(NAMESPACE, methodName);

        // SoapObject添加参数
        if (methodName.equals("findFace")) {
            soapObject.addProperty("arg0", properties.get("arg0"));
        } else if (methodName.equals("matchPhoto")) {
            soapObject.addProperty("arg0", properties.get("arg0"));
            soapObject.addProperty("arg1", properties.get("arg1"));
            soapObject.addProperty("arg2", properties.get("arg2"));
        }
        // 实例化SoapSerializationEnvelope，传入WebService的SOAP协议的版本号
        final SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        soapEnvelope.bodyOut = soapObject;
        // 设置是否调用的是.Net开发的WebService
        //soapEnvelope.setOutputSoapObject(soapObject);
        /*
        soapEnvelope.dotNet = true;
        httpTransportSE.debug = true;
        */
        // 用于子线程与主线程通信的Handler
        final Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 将返回值回调到callBack的参数中
                webServiceCallBack.callBack((SoapObject) msg.obj);
            }

        };

        // 开启线程去访问WebService
        executorService.submit(new Runnable() {

            @Override
            public void run() {
                SoapObject resultSoapObject = null;
                try {
                    httpTransportSE.call(NAMESPACE + methodName, soapEnvelope);
                    if (soapEnvelope.getResponse() != null) {
                        // 获取服务器响应返回的SoapObject
                        resultSoapObject = (SoapObject) soapEnvelope.bodyIn;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } finally {
                    // 将获取的消息利用Handler发送到主线程
                    mHandler.sendMessage(mHandler.obtainMessage(0,
                            resultSoapObject));

                }
            }
        });
    }

    public interface WebServiceCallBack {
        void callBack(SoapObject result);
    }


}
