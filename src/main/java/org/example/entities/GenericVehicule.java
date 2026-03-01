package org.example.entities;

/** Generic vehicle class for types without a specific entity class. */
public class GenericVehicule extends BaseVehicule {
    public GenericVehicule() {
        super();
    }

    public GenericVehicule(String type) {
        super();
        this.setType(type);
    }
}
