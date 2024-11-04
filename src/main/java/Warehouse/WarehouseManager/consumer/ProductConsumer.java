package Warehouse.WarehouseManager.consumer;



import Warehouse.WarehouseManager.product.ProductDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductConsumer {

    private List<ProductDto> productMessages = new ArrayList<>();

    private final String TOPIC_NAME = "new-topic";

    private int lastReadIndex = 0;

    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;


    @Autowired
    public ProductConsumer(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    @KafkaListener(id = "id-1",topics = TOPIC_NAME, groupId = "group-1",autoStartup = "false")
    public void consume(ProductDto productDto) {
        productMessages.add(productDto);
        System.out.println("Received Product: " + productDto);
    }

    public boolean startListener(String listenerId){
        MessageListenerContainer listener = kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        listener.start();
        return true;
    }

    public boolean stopListener(String listenerId){
        MessageListenerContainer listener = kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        listener.stop();
        return true;
    }

    public List<ProductDto> retrieveAllProductAddedAtDay() {
        List<ProductDto> productsAddedAtDay = new ArrayList<>(productMessages.subList(lastReadIndex,productMessages.size()));
        lastReadIndex = productsAddedAtDay.size();
        return productsAddedAtDay;
    }

}
