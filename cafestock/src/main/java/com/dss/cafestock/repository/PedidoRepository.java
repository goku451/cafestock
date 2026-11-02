package com.dss.cafestock.repository;

import com.dss.cafestock.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findAllByOrderByFechaDesc();
    List<Pedido> findTop50ByOrderByFechaDesc();
    List<Pedido> findByEstadoOrderByFechaDesc(String estado);
}
