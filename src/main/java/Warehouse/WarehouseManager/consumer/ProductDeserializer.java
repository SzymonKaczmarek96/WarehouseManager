package Warehouse.WarehouseManager.consumer;

import Warehouse.WarehouseManager.product.ProductDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class ProductDeserializer implements Deserializer<ProductDto> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public ProductDto deserialize(String s, byte[] data) {
        try {
            if (data == null) {
                System.out.println("Null received at deserializing");
                return null;
            }
            System.out.println("Deserializing...");
            return objectMapper.readValue(new String(data, "UTF-8"), ProductDto.class);
        } catch (Exception message) {
            throw new SerializationException("Error when deserializing byte[] to MessageDto", message);
        }
    }
    @Override
    public void close() {
    }
}
