package Warehouse.WarehouseManager.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public enum Role {
    ADMIN(new EnumMap<>(WarehouseSystemOperation.class) {{
        put(WarehouseSystemOperation.RECEIVE, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION, Resource.WAREHOUSE));
        put(WarehouseSystemOperation.STORE, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION, Resource.WAREHOUSE));
        put(WarehouseSystemOperation.MODIFY, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION, Resource.WAREHOUSE));
        put(WarehouseSystemOperation.REMOVAL, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION, Resource.WAREHOUSE));
        put(WarehouseSystemOperation.ADD, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION, Resource.WAREHOUSE));
    }}),
    BUSINESS_OWNER(new EnumMap<>(WarehouseSystemOperation.class) {{
        put(WarehouseSystemOperation.RECEIVE, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION));
        put(WarehouseSystemOperation.STORE, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION));
        put(WarehouseSystemOperation.MODIFY, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION));
        put(WarehouseSystemOperation.REMOVAL, Arrays.asList(Resource.PRODUCT, Resource.WAREHOUSE_OPERATION));
    }}),
    WAREHOUSE_OPERATOR(new EnumMap<>(WarehouseSystemOperation.class) {{
        put(WarehouseSystemOperation.RECEIVE, Arrays.asList(Resource.PRODUCT));
        put(WarehouseSystemOperation.STORE, Arrays.asList(Resource.PRODUCT));
        put(WarehouseSystemOperation.MODIFY, Arrays.asList(Resource.PRODUCT));
        put(WarehouseSystemOperation.REMOVAL, Arrays.asList(Resource.PRODUCT));
    }});

    private EnumMap<WarehouseSystemOperation, List<Resource>> userAccess;

    Role(EnumMap<WarehouseSystemOperation, List<Resource>> userAccess) {
        this.userAccess = userAccess;
    }

    public boolean hasAccessTo(WarehouseSystemOperation operation, Resource resource) {
        return userAccess.getOrDefault(operation, Collections.emptyList()).stream()
                .anyMatch(resource1 -> resource1 == resource);
    }

}
