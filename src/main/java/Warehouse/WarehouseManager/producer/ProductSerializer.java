package Warehouse.WarehouseManager.producer;

import Warehouse.WarehouseManager.product.ProductDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.hibernate.type.SerializationException;

import java.util.Map;

public class ProductSerializer implements Serializer<ProductDto> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, ProductDto productDto) {
        try{
            if(productDto == null){
                System.out.println("Null received at serializing");
                return null;
            }
            System.out.println("Serializing...");
            return objectMapper.writeValueAsBytes(productDto);
        }catch(Exception message){
            throw new SerializationException("Error when serializing MessageDto to byte[]", message);
        }
    }

    @Override
    public void close() {
    }
}
