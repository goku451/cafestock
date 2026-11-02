package com.dss.cafestock.repository;

import com.dss.cafestock.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findAllByOrderByFechaDesc();
}
