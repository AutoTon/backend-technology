package com.technology.feign;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 自定义解码
 */
public class TextHtmlResultDecoder implements Decoder {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response.body() == null) {
            return null;
        }
        String body = Util.toString(response.body().asReader(Util.UTF_8));
        JavaType javaType = objectMapper.constructType(type);
        return objectMapper.readValue(body, javaType);
    }

}
