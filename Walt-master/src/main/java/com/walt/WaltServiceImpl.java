package com.walt;

import com.walt.model.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WaltServiceImpl implements WaltService {

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
        return null;
    }

    @Override
    public List<DriverDistance> getDriverRankReport() {
        return null;
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return null;
    }
}
