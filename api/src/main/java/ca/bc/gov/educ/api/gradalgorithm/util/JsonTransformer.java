package ca.bc.gov.educ.api.gradalgorithm.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.TimeZone;

@Slf4j
@Component
public class JsonTransformer implements Transformer {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDate.class, new GradLocalDateSerializer());
        simpleModule.addDeserializer(LocalDate.class, new GradLocalDateDeserializer());
        OBJECT_MAPPER
                .findAndRegisterModules()
                .registerModule(simpleModule)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.INDENT_OUTPUT)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(JsonGenerator.Feature.ESCAPE_NON_ASCII)
                .setTimeZone(TimeZone.getDefault())
        //        .enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS)
        ;
    }

    @Override
    public Object unmarshall(byte[] input, Class<?> clazz) throws TransformerException {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = OBJECT_MAPPER.readValue(input, clazz);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.debug("Time taken for unmarshalling response from bytes to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    public Object unmarshallWithWrapper(String input, Class<?> clazz) throws TransformerException {
        final ObjectReader reader = OBJECT_MAPPER.readerFor(clazz);
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = reader
                    .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                    .with(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
                    .readValue(input);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.debug("Time taken for unmarshalling response from String to {} is {} ms", clazz.getSimpleName(), (System.currentTimeMillis() - start));
        return result;
    }

    public String marshallWithWrapper(Object input) throws TransformerException {
        ObjectWriter prettyPrinter = OBJECT_MAPPER.writer();
        String result = null;
        try {
            result = prettyPrinter
                    .with(SerializationFeature.WRAP_ROOT_VALUE)
                    .writeValueAsString(input);
        } catch (IOException e) {
            throw new TransformerException(e);
        }

        return result;
    }

    @Override
    public Object unmarshall(String input, Class<?> clazz) throws TransformerException {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = OBJECT_MAPPER.readValue(input, clazz);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.debug("Time taken for unmarshalling response from String to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Object unmarshall(InputStream input, Class<?> clazz) throws TransformerException {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = OBJECT_MAPPER.readValue(input, clazz);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.debug("Time taken for unmarshalling response from stream to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public String marshall(Object input) throws TransformerException {
        ObjectWriter prettyPrinter = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
        String result = null;
        try {
            result = prettyPrinter.writeValueAsString(input);
        } catch (IOException e) {
            throw new TransformerException(e);
        }

        return result;
    }

    @Override
    public String getAccept() {
        return "application/json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
