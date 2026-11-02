package com.dss.cafestock.repository;

import com.dss.cafestock.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByInsumo_IdInsumo(Long idInsumo);
}
