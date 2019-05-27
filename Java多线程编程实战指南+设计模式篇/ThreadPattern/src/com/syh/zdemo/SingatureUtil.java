package com.syh.zdemo;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class SingatureUtil {

    private static final String FANS_SALT = "382700b563f4";

    public static String genSignature(Map<String,String> params,String salt) {
        if(params == null){
            return null;
        }
        String sign = "";
        StringBuffer sb = new StringBuffer();
        try {
            // 1. 字典升序排序
            SortedMap<String,String> sortedMap = new TreeMap<String,String>(params);
            // 2. 拼按URL键值对
            Set<String> keySet = sortedMap.keySet();
            for(String key : keySet){
                //sign不参与算法
                if(key.equals("sig") || key.equals("__NStokensig")){
                    continue;
                }
                String value = sortedMap.get(key);
                sb.append(key + "=" + URLDecoder.decode(value, "UTF-8"));
            }
            String uriString = sb.toString();
            uriString = uriString + salt;
            System.out.println("My String: \n" + uriString);
            // 3. MD5运算得到请求签名
            sign = MD5Util.MD5(uriString);
            System.out.println("My Sign:\n" +sign.toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    public static Map<String,String> getMapFromStr(String str){
        if(str == null || "".equals(str.trim())){
            return null;
        }
        String[] arr = str.split("\\&");
        Map<String,String> map = new HashMap<String,String>();
        for(String item : arr){
            String[] itemArr = item.split("=",2);
            map.put(itemArr[0],itemArr[1]);
        }
        return map;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        //String srcStr = "app=0&lon=104.073269&did_gt=1551777466213&c=XIAOMI&sys=ANDROID_4.4.4&isp=&mod=Xiaomi%28MI%203%29&did=ANDROID_b07d34ee8ff226b0&hotfix_ver=&ver=6.1&net=WIFI&country_code=cn&iuid=&appver=6.1.2.8197&max_memory=192&oc=XIAOMI&ftt=&kpn=KUAISHOU&ud=1273257807&language=zh-cn&kpf=ANDROID_PHONE&lat=30.537794&user=74476707&token=6f8b8954c34e4462a1c0117ac5a5af21-1273257807&os=android&client_key=3c2cd3f3&sig=8ab207f1762b17b47d1ca0cc26ce6576&__NStokensig=334b7f77f9fec536c1dce00467f8cf79bed4f66cd8a24ffc205b3e1a151ab1e7";
        //String srcStr = "app=0&lon=110.568709&did_gt=1554783665296&c=APPCHINA_CPD&sys=ANDROID_4.4.2&isp=CMCC&mod=Xiaomi%28M688C%29&did=ANDROID_c07323016e34408e&hotfix_ver=&ver=6.3&net=WIFI&country_code=cn&iuid=&appver=6.3.0.8671&max_memory=192&oc=APPCHINA_CPD&ftt=&kpn=KUAISHOU&ud=276954325&language=zh-cn&kpf=ANDROID_PHONE&lat=33.999613&user=894928412&token=b674089fbda94e89b77e9adae5f42a18-276954325&__NStokensig=a0da558433d2fe01af530b134c0938bbe9d080b619167db03f23a4663afb0c73&os=android&client_key=3c2cd3f3&sig=c5ee77625fd451a3deba34c197d2641a";
        String srcStr = "app=0&lon=110.568709&did_gt=1554783665296&c=APPCHINA_CPD&sys=ANDROID_4.4.2&isp=CMCC&mod=Xiaomi%28M688C%29&did=ANDROID_c07323016e34408e&hotfix_ver=&ver=6.3&net=WIFI&country_code=cn&iuid=&appver=6.3.0.8671&max_memory=192&oc=APPCHINA_CPD&ftt=&kpn=KUAISHOU&ud=276954325&language=zh-cn&kpf=ANDROID_PHONE&lat=33.999613&user=240080877&token=b674089fbda94e89b77e9adae5f42a18-276954325&__NStokensig=a0da558433d2fe01af530b134c0938bbe9d080b619167db03f23a4663afb0c73&os=android&client_key=3c2cd3f3&sig=c5ee77625fd451a3deba34c197d2641a";
        genSignature(getMapFromStr(srcStr),FANS_SALT);
    }
}