package com.labISD.demo.domain;

import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.labISD.demo.enums.*;


@Entity
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter
    private UUID purchaseId;

    @Setter @Getter @NotNull
    private UUID userId;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private List<PurchaseItem> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Getter @Setter
    private ORDERSTATUS status = ORDERSTATUS.CART;

    @CreationTimestamp
    @Column(updatable = false)
    @Getter
    private LocalDateTime createdAt;

    @Getter @Setter
    private LocalDateTime orderedAt;

    @Getter @Setter @NotNull(message = "total amount cannot be null") @Min(value = 0, message = "total amount cannot be less than 0")
    private float totalAmount;

    protected Purchase() {}

    public Purchase(UUID userId) {
        this.userId = userId;
        items = new ArrayList <>();
        totalAmount = 0;
        orderedAt = null;
    }

    public void addItemToCart(Product product, int quantity){
        Optional<PurchaseItem> existingItem = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().addQuantity(quantity);
            existingItem.get().updateTotalPrice();
        } else {
            PurchaseItem newItem = new PurchaseItem(product, this, quantity);
            items.add(newItem);
        }
        totalAmount+= quantity*product.getPrice();
    }

    public boolean removeItem(UUID productId){
        PurchaseItem itemToRemove = items.stream()
        .filter(item -> item.getProduct().getId().equals(productId))
        .findFirst()
        .orElse(null);
        if(itemToRemove!= null){
            items.remove(itemToRemove);
            totalAmount-= itemToRemove.getTotalPrice();
            return true;
        }
        return false;
    }

    public void clear(){
        items.clear();
        totalAmount = 0;
    }

    public boolean isEmpty(){
        if(items.size() == 0){
            return true;
        }
        return false;
    }

    @Override
    public String toString(){
        if(this.isEmpty()){
            return "Cart is empty";
        }
        String printPurchase = "";
        if(status == ORDERSTATUS.ORDER){
            printPurchase+= "Ordered at: "+orderedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }else{
            printPurchase+= "Cart:";
        }
        printPurchase+= "\nCreated at: "+createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        printPurchase+= "\nProducts:";
        for(int i = 0; i < items.size(); i++){
            printPurchase+= String.format("\n%d) %s", (i+1), items.get(i).toString());
        }
        printPurchase+= String.format("\nTotal amount: %.2f EUR",totalAmount);
        
        return printPurchase;
    }
}
