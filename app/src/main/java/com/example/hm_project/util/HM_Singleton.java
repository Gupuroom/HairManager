

package com.example.hm_project.util;

/***
 *  클래스안에 클래스(Holder)를 두어 JVM의 Class Loader 매커니즘과 Class가 로드되는 시점을 이용한 방식
 *  싱글톤 객체가 필요할 때 인스턴스를 얻을 수 있고  Thread간 동기화문제를 해결한 방식
 */
public class HM_Singleton {
    // Sample code
    private static class SingletonHolder {
        public static final HM_Singleton INSTANCE = new HM_Singleton();
    }

    public static HM_Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
    // JsonParser 싱글톤 화
    private static class JsonParserHolder {
        public static final  JsonParser INSTANCE = new JsonParser();
    }
    public static JsonParser getInstance(JsonParser jp) {
        return JsonParserHolder.INSTANCE;
    }
}
