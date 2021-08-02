package com.walt;

import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class WaltServiceImpl implements WaltService {

    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private DriverRepository driverRepository;

    /**
     *The function create order and assign driver
     * The city must to be in restaurant and find available driver for dlivery
     * @param customer
     * @param restaurant
     * @param deliveryTime
     * @return
     */
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
        if (customer == null || restaurant == null || deliveryTime == null) {
            throw new RuntimeException("Error: One or more of the details is null");
        }
        if (!customer.getCity().getId().equals(restaurant.getCity().getId())) {
            throw new RuntimeException("Error:The restaurant should be in your city of located");
        }
        List <Driver>driversAvailable=findDriversAvailable(customer,deliveryTime);
        Driver driver =getDriverMostBusy(driversAvailable);
        Delivery delivery = new Delivery(driver, restaurant, customer, deliveryTime);
        deliveryRepository.save(delivery);
        return delivery;
    }

    /**
     *The function search driver that live in same city of customer and restaurant
     * @param customer
     * @param deliveryTime
     * @return
     */
    public List<Driver>findDriversAvailable(Customer customer, Date deliveryTime)
    {
        List<Driver>driversAvailable = new ArrayList<Driver>();
        for (Driver driver :driverRepository.findAllDriversByCity(customer.getCity())){
             if(isDriverAvailable(driver,deliveryTime)){
                 driversAvailable.add(driver);
             }
        }
        if (driversAvailable.isEmpty()) {
            throw new RuntimeException("Error: There is no driver available in the city");
        }
        return  driversAvailable;
    }

    /**
     *The function check if driver available to delivery according time
     * @param driver
     * @param deliveryTime
     * @return
     */
    public boolean isDriverAvailable(Driver driver,Date deliveryTime){
            List<Delivery> deliveries = deliveryRepository.findDeliveriesByDriver(driver);
            for (Delivery delivery : deliveries) {
                long MillionSeconds = Math.abs(delivery.getDeliveryTime().getTime() - deliveryTime.getTime());

                //1000ms * 60s * 2m
                if (MillionSeconds <= (1000 * 60 * 60)) {
                    return false;
                }
            }
            return true;
    }

    /**
     *
     * @param availableDrivers
     * @return
     */
    private Driver getDriverMostBusy(List<Driver> availableDrivers) {
        Collections.sort(availableDrivers, Comparator.comparingDouble(this::getDriverKM));
        return availableDrivers.get(0);
    }

    /**
     *
     * @param driver
     * @return
     */
    private double getDriverKM(Driver driver) {
        return deliveryRepository.findDeliveriesByDriver(driver).stream()
                .mapToDouble(Delivery::getDistance)
                .sum();
    }

    /**
     *
     * @return
     */
    @Override
    public List<DriverDistance> getDriverRankReport() {
        return deliveryRepository.findDistancesByDriver();
    }

    /**
     *
     * @param city
     * @return
     */
    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
         return deliveryRepository.findCityDistancesByDriver(city);
    }


}
