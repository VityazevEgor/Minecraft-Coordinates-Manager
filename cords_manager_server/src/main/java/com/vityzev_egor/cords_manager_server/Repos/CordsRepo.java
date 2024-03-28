package com.vityzev_egor.cords_manager_server.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vityzev_egor.cords_manager_server.Models.CordsModel;

public interface CordsRepo extends JpaRepository<CordsModel, Integer> {
    
}
