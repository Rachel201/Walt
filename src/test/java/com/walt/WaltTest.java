package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);


        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa,bash);

        createRestaurant(jerusalem, tlv ,bash);
    }

    private void createRestaurant(City jerusalem, City tlv, City bash) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");
        Restaurant cook = new Restaurant("cook", bash, "cookies chockalate");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican,cook));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa,City bash) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");
        Customer gilli = new Customer("Gilli", bash, "gilli shalom");
        Customer shared = new Customer("Shared", bash, "golomb ferg");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach,gilli,shared));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);
        Driver json = new Driver("json", tlv);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata,json));
    }

    @Test
    public void testBasics(){

        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }
    @Test
    public void createOrder() {
        Customer customer = customerRepository.findByName("Bach");
        Restaurant restaurant = restaurantRepository.findByName("cafe");
        Date date = new Date();
        Driver testDriver = driverRepository.findByName("Mary");
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, date);
        assertEquals(testDriver.getId(), delivery.getDriver().getId());
    }

    @Test
    public void createTwoOrderNotAtTheSameTime(){
        Customer customer1 = customerRepository.findByName("Beethoven");
        Customer customer2 = customerRepository.findByName("Bach");
        Restaurant restaurant = restaurantRepository.findByName("chinese");
        Date date = new Date();
        Driver driver = driverRepository.findByName("Mary");

        Delivery delivery = waltService.createOrderAndAssignDriver(customer1, restaurant,date);
        Date date2 = new Date();
        date2.setTime(date2.getTime() + TimeUnit.HOURS.toMillis(3));

        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant, date2);

        assertEquals(driver.getId(), delivery.getDriver().getId());
    }
    @Test
    public void createTwoOrders(){
        Customer customer1 = customerRepository.findByName("Beethoven");
        Customer customer2 = customerRepository.findByName("Bach");
        Restaurant restaurant = restaurantRepository.findByName("cafe");
        Date date = new Date();
        Driver driver = driverRepository.findByName("Robert");

        Delivery delivery = waltService.createOrderAndAssignDriver(customer1, restaurant,date);

        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant, date);

        assertNotEquals(driver.getId(), delivery.getDriver().getId());
    }

    @Test
    public void createTwoOrderOtherTime(){
        Customer customer1 = customerRepository.findByName("Rachmaninoff");
        Customer customer2 = customerRepository.findByName("Bach");
        Restaurant restaurant = restaurantRepository.findByName("restaurant");

        Date date1 = new Date();
        Date date2 = new Date();
        date2.setTime(date2.getTime() + TimeUnit.HOURS.toMillis(3));

        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer1, restaurant, date1);
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant, date2 );

        assertNotEquals(delivery1.getDeliveryTime(),delivery2.getDeliveryTime());
    }

    @Test
    public void createOrderRestaurantNotInTown() {
        Customer customer = customerRepository.findByName("Beethoven");
        Date date = new Date();
        Restaurant restaurant = restaurantRepository.findByName("meat");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(customer, restaurant, date);
        });

        String expectedMessage = "Error:The restaurant should be in your city of located";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public  void avaibleDriver() {
        Customer customer = customerRepository.findByName("Gilli");

        Restaurant restaurant = restaurantRepository.findByName("cook");
        Date date = new Date();

        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer, restaurant, date);

        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer, restaurant, date);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(customer, restaurant, date);
        });

        String expectedMessage = "Error: There is no driver available in the city";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    public void differentCityOfCustomerAndRestaurant() {
        Customer customer1 = customerRepository.findByName("Gilli"); //in bash

        Restaurant restaurant = restaurantRepository.findByName("chinese"); //in tlv
        Date date = new Date();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(customer1, restaurant,
                    date);
        });

        String expectedMessage = "ERROR : The customer and restaurant are in the different cities";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testDriverRankReportByCity() {

        Customer customer1 = customerRepository.findByName("Gilli");
        Customer customer2 = customerRepository.findByName("Shared");

        Restaurant restaurant = restaurantRepository.findByName("cook");
        Date date = new Date();
        date.setTime(date.getTime() + TimeUnit.HOURS.toMillis(10000));


        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer1, restaurant,  date);

        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant,  date);


        City city = cityRepository.findByName("Beer-Sheva");

        List<DriverDistance> driverDistancesByCity = waltService.getDriverRankReportByCity(city);


        System.out.println("\nTotal distance of delivery order by total distance in descending order by city" +
                city.getName() +
                ":");
        System.out.println(
                "----------------------------------------------------------------------\n");

        for (DriverDistance d : driverDistancesByCity) {
            System.out.println("Name: " + d.getDriver().getName() +
                    ", Distance: " + d.getTotalDistance());
        }
    }
    @Test
    public void driverRankReport() {
        Customer customer1 = customerRepository.findByName("Gilli");
        Customer customer2 = customerRepository.findByName("Mozart");

        Restaurant restaurant1 = restaurantRepository.findByName("cook");
        Restaurant restaurant2 = restaurantRepository.findByName("meat");

        Date date = new Date();
        date.setTime(date.getTime() + TimeUnit.HOURS.toMillis(10000));


        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer1, restaurant1, date);

        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant2,date);


        List<DriverDistance> driverDistances = waltService.getDriverRankReport();

        System.out.println("Drivers rank rebort By distance:");
        System.out.println("--------------------------------------------------");

        for (DriverDistance d : driverDistances) {
            System.out.println(" Name: " + d.getDriver().getName() +
                    ", Distance: " + d.getTotalDistance());
        }
    }

    @Test
    public void invalidInput() {
        Customer customer1 = customerRepository.findByName("Mozart");
        Restaurant restaurant = restaurantRepository.findByName("meat");
        Date date = new Date();

        // Test to null customer
        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(null, restaurant,date);
        });

        String expectedMessage = "Error: One or more of the details is null";
        String actualMessage1 = exception1.getMessage();
        assertTrue(actualMessage1.contains(expectedMessage));

        // Test for null restaurant
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(customer1, null,date);
        });
        String actualMessage2 = exception2.getMessage();
        assertTrue(actualMessage2.contains(expectedMessage));

        // Test for null delivery
        Exception exception3 = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(customer1, restaurant,null);
        });
        String actualMessage3 = exception3.getMessage();
        assertTrue(actualMessage3.contains(expectedMessage));
    }



}


