package com.fivesense.api.materials.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="material")
public class Material {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,unique=true,length=55) private String name;
    @Column(name="stock_quantity",nullable=false) private int stockQuantity;
    @Column(name="minimum_stock",nullable=false) private int minimumStock;
    @Column(nullable=false) private boolean active=true;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected Material() {}
    public Material(String name,int stockQuantity,int minimumStock,Instant now){this.name=name;this.stockQuantity=stockQuantity;this.minimumStock=minimumStock;this.createdAt=now;this.updatedAt=now;}
    public void update(String name,int stockQuantity,int minimumStock,boolean active,Instant now){this.name=name;this.stockQuantity=stockQuantity;this.minimumStock=minimumStock;this.active=active;this.updatedAt=now;}
    public UUID getId(){return id;} public String getName(){return name;} public int getStockQuantity(){return stockQuantity;} public int getMinimumStock(){return minimumStock;} public boolean isActive(){return active;}
}
