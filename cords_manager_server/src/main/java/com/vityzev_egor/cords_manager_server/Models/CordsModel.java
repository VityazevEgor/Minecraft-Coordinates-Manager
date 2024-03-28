package com.vityzev_egor.cords_manager_server.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CordsModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    public String title;

    public String cords;

    public String imageName;
}
