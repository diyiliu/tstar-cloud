package com.tiza.op.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: JacksonUtil
 * Author: DIYILIU
 * Update: 2016-03-22 9:25
 */
public class JacksonUtil {
    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj){

        String rs = null;

        try {
            rs = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {

            e.printStackTrace();
        }

        return rs;
    }

    public static <T> T  toObject(String content, Class<T> clazz) throws IOException {

        return (T) mapper.readValue(content, clazz);
    }

    public static List toList(String content, Class clazz) throws IOException{
        JavaType javaType = getCollectionType(ArrayList.class, clazz);

        return mapper.readValue(content, javaType);
    }

    /**
     * 获取泛型的Collection Type
     * @param collectionClass 泛型的Collection
     * @param elementClasses 元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}
