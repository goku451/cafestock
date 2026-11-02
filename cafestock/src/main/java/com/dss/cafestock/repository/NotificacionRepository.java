package com.dss.cafestock.repository;

import com.dss.cafestock.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findAllByOrderByFechaDesc();
    List<Notificacion> findTop20ByOrderByFechaDesc();
    List<Notificacion> findByLeidaFalseOrderByFechaDesc();
    long countByLeidaFalse();
}
