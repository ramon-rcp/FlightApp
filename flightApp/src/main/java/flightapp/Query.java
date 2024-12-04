package flightapp;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Runs queries against a back-end database
 */
public class Query extends QueryAbstract {
  //
  // Canned queries
  //
  private static final String FLIGHT_CAPACITY_SQL = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement flightCapacityStmt;

  private static final String DELETE_USERS = "DELETE FROM Users_ramonrcp";
  private PreparedStatement deleteUsers;

  private static final String DELETE_RESERVATIONS = "DELETE FROM Reservations_ramonrcp";
  private PreparedStatement deleteReservations;

  private static final String ADD_USER = "INSERT INTO Users_ramonrcp VALUES (?, ?, ?)";
  private PreparedStatement addUser;

  private static final String USERNAME_COUNT = "SELECT COUNT(*) AS count " +
    "FROM Users_ramonrcp as U " +
    "WHERE U.username = ?";
  private PreparedStatement usernameCount;

  private static final String FIND_PASSWORD = "SELECT U.password as p " +
    "FROM Users_ramonrcp AS U " +
    "WHERE U.username = ?";
  private PreparedStatement findPassword;

  private static final String DIRECT_FLIGHTS = "SELECT TOP (?) fid, day_of_month, carrier_id, flight_num, origin_city, dest_city, actual_time, capacity, price " +
    "FROM Flights " +
    "WHERE origin_city = ? AND " + 
    "dest_city = ? AND " +
    "day_of_month = ? AND " +
    "canceled = 0 " +
    "ORDER BY actual_time ASC";
  private PreparedStatement directFlights;

  private static final String INDIRECT_FLIGHTS = "SELECT TOP (?)" +
      //flight1 information
      "F1.fid AS F1_fid," +
      "F1.day_of_month AS F1_dayOfMonth," +
      "F1.carrier_id AS F1_carrier," +
      "F1.flight_num AS F1_flightNum," +
      "F1.origin_city AS F1_originCity," +
      "F1.dest_city AS F1_destCity," +
      "F1.actual_time AS F1_time," +
      "F1.capacity AS F1_capacity," +
      "F1.price AS F1_price, " + 

      //flight2 information
      "F2.fid AS F2_fid," +
      "F2.day_of_month AS F2_dayOfMonth," +
      "F2.carrier_id AS F2_carrier," +
      "F2.flight_num AS F2_flightNum," +
      "F2.origin_city AS F2_originCity," +
      "F2.dest_city AS F2_destCity," +
      "F2.actual_time AS F2_time," +
      "F2.capacity AS F2_capacity," +
      "F2.price AS F2_price " +

    "FROM FLIGHTS AS F1, FLIGHTS AS F2 " +
    
    "WHERE F1.fid != F2.fid AND " +
      "F1.day_of_month = ? AND " +
      "F1.day_of_month = F2.day_of_month AND " +
      "F1.origin_city = ? AND " +
      "F1.dest_city = F2.origin_city AND " +
      "F2.dest_city = ? AND " +
      "F1.canceled = 0 AND " +
      "F2.canceled = 0 " + 
    "ORDER BY (F1.actual_time + F2.actual_time), " +
      "F1.fid, F2.fid";
  private PreparedStatement indirectFlights;

  private static final String USER_RESERVATIONS = "SELECT * " +
    "FROM Reservations_ramonrcp " +
    "WHERE username = ?";
  private PreparedStatement userReservations;

  private static final String FIND_FLIGHT_FID = "SELECT * " +
    "FROM FLIGHTS " +
    "WHERE fid = ?";
  private PreparedStatement findFlightFid;

  private static final String ADD_RESERVATION = "INSERT INTO Reservations_ramonrcp VALUES (" +
  "?, "+
  "?, " +
  "(SELECT fid FROM FLIGHTS WHERE fid = ?), " +
  "(SELECT fid FROM FLIGHTS WHERE fid = ?), " +
  "(SELECT username FROM Users_ramonrcp WHERE username = ?))";
  private PreparedStatement addReservation;

  private static final String RESERVATION_AMOUNT = "SELECT COUNT(rid) AS cnt " + 
    "FROM Reservations_ramonrcp " +
    "WHERE username = ?";
  private PreparedStatement reservationAmount;

  private static final String GET_RESERVATION_RID = "SELECT * " +
    "FROM Reservations_ramonrcp " +
    "WHERE rid = ? ";
  private PreparedStatement getReservationRid;

  private static final String PAY_RESERVATION = "UPDATE Reservations_ramonrcp " +
    "SET is_paid = 1 " +
    "WHERE rid = ?";
  private PreparedStatement payReservation;

  private static final String UPDATE_BALANCE = "UPDATE Users_ramonrcp " +
    "SET balance = ? " +
    "WHERE username = ?";
  private PreparedStatement updateBalance;

  private static final String FIND_RESERVATION_AMOUNT_OF_FLIGHT = "SELECT COUNT(rid) AS cnt " +
    "FROM Reservations_ramonrcp " +
    "WHERE  flight_1 = ? OR flight_2 = ?";
  private PreparedStatement findFlightAmountReserved;

  private static final String GET_USER = "SELECT * FROM Users_ramonrcp WHERE username = ?";
  private PreparedStatement getUser;

  //
  // Instance variables
  //
  private String userLoggedIn = "";
  private ArrayList<Itinerary> itineraries = new ArrayList<>();

  protected Query() throws SQLException, IOException {
    prepareStatements();
  }

  /**
   * Clear the data in any custom tables created.
   * 
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      deleteReservations.clearParameters();
      deleteReservations.executeUpdate();

      deleteUsers.clearParameters();
      deleteUsers.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method.
   */
  private void prepareStatements() throws SQLException {
    flightCapacityStmt = conn.prepareStatement(FLIGHT_CAPACITY_SQL);

    deleteUsers = conn.prepareStatement(DELETE_USERS);
    deleteReservations = conn.prepareStatement(DELETE_RESERVATIONS);
    addUser = conn.prepareStatement(ADD_USER);
    usernameCount = conn.prepareStatement(USERNAME_COUNT);
    findPassword = conn.prepareStatement(FIND_PASSWORD);
    directFlights = conn.prepareStatement(DIRECT_FLIGHTS);
    indirectFlights = conn.prepareStatement(INDIRECT_FLIGHTS);
    userReservations = conn.prepareStatement(USER_RESERVATIONS);
    findFlightFid = conn.prepareStatement(FIND_FLIGHT_FID);
    addReservation = conn.prepareStatement(ADD_RESERVATION);
    reservationAmount = conn.prepareStatement(RESERVATION_AMOUNT);
    getReservationRid = conn.prepareStatement(GET_RESERVATION_RID);
    payReservation = conn.prepareStatement(PAY_RESERVATION);
    updateBalance = conn.prepareStatement(UPDATE_BALANCE);
    findFlightAmountReserved = conn.prepareStatement(FIND_RESERVATION_AMOUNT_OF_FLIGHT);
    getUser = conn.prepareStatement(GET_USER);
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_login(String username, String password) {
    if(!userLoggedIn.equals("")){
      return "User already logged in\n";
    }

    itineraries = new ArrayList<>();

    try{
      //check if user was created
      usernameCount.clearParameters();
      usernameCount.setString(1, username.toLowerCase());
      ResultSet rs = usernameCount.executeQuery();
      if(rs.next()){
        int usrnmCnt = rs.getInt("count");
        if(usrnmCnt == 0){
          return "Login failed\n";
        }
      }

      //find the user's password 
      findPassword.clearParameters();
      findPassword.setString(1, username);
      rs = findPassword.executeQuery();
      if(rs.next()){
        byte[] hashedPassword = rs.getBytes("p");
        if(!PasswordUtils.plaintextMatchesSaltedHash(password, hashedPassword)){
          return "Login failed\n";
        }
      }

      //log in
      userLoggedIn = username.toLowerCase();
      return "Logged in as " + username.toLowerCase() + "\n";
    } 
    catch (Exception e){
      e.printStackTrace();
    }
    return "Login failed\n";
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    // The balance amount can't be negative
    if(initAmount < 0) {
      return "Failed to create user\n";
    }

    try {
      //get the amount of existing users with the given username
      usernameCount.clearParameters();
      usernameCount.setString(1, username);
      ResultSet rs = usernameCount.executeQuery();

      //don't create the new user if the username already exists
      if(rs.next()){
        int usrnmCnt = rs.getInt("count");
        if(usrnmCnt > 0){
          return "Failed to create user\n";
        }
      }

      //set the parameters for the new user
      addUser.clearParameters();
      addUser.setString(1, username.toLowerCase());
      addUser.setBytes(2, PasswordUtils.saltAndHashPassword(password));
      addUser.setInt(3, initAmount);
      addUser.executeUpdate();

      return "Created user " + username.toLowerCase() + "\n";
    } 
    catch(Exception e){
      e.printStackTrace();
    }
    return "Failed to create user\n";
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_search(String originCity, String destinationCity, 
                                   boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries) {

    if(numberOfItineraries < 0){
      return "Failed to search\n";
    }

    if(dayOfMonth < 0 || dayOfMonth > 31){
      return "Failed to search\n";
    }

    StringBuffer sb = new StringBuffer();
    itineraries = new ArrayList<Itinerary>();

    try {
      //get direct flights
      directFlights.clearParameters();
      directFlights.setInt(1, numberOfItineraries);
      directFlights.setString(2, originCity);
      directFlights.setString(3, destinationCity);
      directFlights.setInt(4, dayOfMonth);
      ResultSet flightsQueryResult = directFlights.executeQuery();

      //place flights into list
      while(flightsQueryResult.next()){
        itineraries.add(
          new Itinerary(
            new Flight(
              flightsQueryResult.getInt("fid"),
              flightsQueryResult.getInt("day_of_month"),
              flightsQueryResult.getString("carrier_id"),
              flightsQueryResult.getString("flight_num"),
              flightsQueryResult.getString("origin_city"),
              flightsQueryResult.getString("dest_city"),
              flightsQueryResult.getInt("actual_time"),
              flightsQueryResult.getInt("capacity"),
              flightsQueryResult.getInt("price")
            )
          )
        );
      }
      flightsQueryResult.close();

      if(!directFlight && itineraries.size()<numberOfItineraries){
        //get indirect flights
        indirectFlights.clearParameters();
        indirectFlights.setInt(1, numberOfItineraries - itineraries.size());
        indirectFlights.setInt(2, dayOfMonth);
        indirectFlights.setString(3, originCity);
        indirectFlights.setString(4, destinationCity);
        flightsQueryResult = indirectFlights.executeQuery();

         //place flights into list
        while(flightsQueryResult.next()){
          itineraries.add(
            new Itinerary(
              new Flight(
                flightsQueryResult.getInt("F1_fid"),
                flightsQueryResult.getInt("F1_dayOfMonth"),
                flightsQueryResult.getString("F1_carrier"),
                flightsQueryResult.getString("F1_flightNum"),
                flightsQueryResult.getString("F1_originCity"),
                flightsQueryResult.getString("F1_destCity"),
                flightsQueryResult.getInt("F1_time"),
                flightsQueryResult.getInt("F1_capacity"),
                flightsQueryResult.getInt("F1_price")
              ),
              new Flight(
                flightsQueryResult.getInt("F2_fid"),
                flightsQueryResult.getInt("F2_dayOfMonth"),
                flightsQueryResult.getString("F2_carrier"),
                flightsQueryResult.getString("F2_flightNum"),
                flightsQueryResult.getString("F2_originCity"),
                flightsQueryResult.getString("F2_destCity"),
                flightsQueryResult.getInt("F2_time"),
                flightsQueryResult.getInt("F2_capacity"),
                flightsQueryResult.getInt("F2_price")
              )
            )
          );
        }
        flightsQueryResult.close();
      }
      
      Collections.sort(itineraries);

      //create the string to return
      for(int i=0; i<itineraries.size(); i++){
        //itinerary info
        sb.append("Itinerary " + i + ": " +
          itineraries.get(i).getFlightAmount() + " flight(s), " + itineraries.get(i).getTime() + " minutes\n");

        //flight 1 info
        sb.append(itineraries.get(i).getFlight(1).toString() + "\n");

        //flight 2 info
        if(itineraries.get(i).getFlight(2) != null){
          sb.append(itineraries.get(i).getFlight(2).toString() + "\n");
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
      return "Failed to search\n";
    }

    if(itineraries.size() == 0){
      return "No flights match your selection\n";
    }

    return sb.toString();
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_book(int itineraryId) {
    if(userLoggedIn.equals("")){
      return "Cannot book reservations, not logged in\n";
    }
    
    if(itineraryId < 0 || itineraryId >= itineraries.size()){
      return "No such itinerary "+ itineraryId +"\n";
    }


    try{
      conn.setAutoCommit(false);

      //get the flights already reserved by the user
      userReservations.clearParameters();
      userReservations.setString(1, userLoggedIn);
      ResultSet reserved = userReservations.executeQuery();

      //compare the day of all reservations with the day of new booking
      while(reserved.next()){
        //get the flight from the reservation
        findFlightFid.clearParameters();
        findFlightFid.setInt(
          1, 
          reserved.getInt("flight_1")
        );
        ResultSet flight = findFlightFid.executeQuery();
        flight.next();

        //compare
        if(flight.getInt("day_of_month") == itineraries.get(itineraryId).getFlight(1).dayOfMonth){
          conn.rollback();
          conn.setAutoCommit(true);
          return "You cannot book two flights in the same day\n";
        }
      }

      //check the flights capacity
      findFlightAmountReserved.clearParameters();
      findFlightAmountReserved.setInt(1, itineraries.get(itineraryId).getFlight(1).fid);
      findFlightAmountReserved.setInt(2, itineraries.get(itineraryId).getFlight(1).fid);
      ResultSet flightAmount = findFlightAmountReserved.executeQuery();
      flightAmount.next();

      if((checkFlightCapacity(itineraries.get(itineraryId).getFlight(1).fid) - flightAmount.getInt("cnt")) == 0){
        conn.rollback();
        conn.setAutoCommit(true);
        return "Booking failed\n";
      }
      if(itineraries.get(itineraryId).getFlightAmount() == 2){
        findFlightAmountReserved.setInt(1, itineraries.get(itineraryId).getFlight(2).fid);
        findFlightAmountReserved.setInt(2, itineraries.get(itineraryId).getFlight(2).fid);
        flightAmount = findFlightAmountReserved.executeQuery();
        flightAmount.next();
        if((checkFlightCapacity(itineraries.get(itineraryId).getFlight(2).fid) - flightAmount.getInt("cnt")) == 0){
          conn.rollback();
          conn.setAutoCommit(true);
          return "Booking failed\n";
        }
      }

      //get reservation amount 
      reservationAmount.clearParameters();
      reservationAmount.setString(1, userLoggedIn);
      ResultSet rsrvAmTable = reservationAmount.executeQuery();
      rsrvAmTable.next();
      int rsrvAm = rsrvAmTable.getInt("cnt") + 1;
   
      /* update reservations table */
      addReservation.clearParameters();
      addReservation.setBoolean(2, false);
      addReservation.setInt(3, itineraries.get(itineraryId).getFlight(1).fid);
      addReservation.setString(5, userLoggedIn);

      //store 0 for flight_2 if the itinerary is direct
      if(itineraries.get(itineraryId).getFlightAmount() == 2){
        addReservation.setInt(4, itineraries.get(itineraryId).getFlight(2).fid);
      }
      else{
        addReservation.setInt(4, 0);
      }

      //check whether there's already a reservation with this rid
      getReservationRid.clearParameters();
      getReservationRid.setInt(1, rsrvAm);
      ResultSet reservation = getReservationRid.executeQuery();
      while(reservation.next()){
        rsrvAm++;
        getReservationRid.setInt(1, rsrvAm);
        reservation = getReservationRid.executeQuery();
      }

      addReservation.setInt(1, rsrvAm);

      addReservation.executeUpdate();
      

      conn.commit();
      conn.setAutoCommit(true);

      return "Booked flight(s), reservation ID: " + (rsrvAm) + "\n";

    } catch(SQLException e) {
      e.printStackTrace(); 
      try{
        conn.setAutoCommit(false);
        conn.rollback();
        conn.setAutoCommit(true);
      } catch (Exception exception){
        exception.printStackTrace();
      }  
      if(isDeadlock(e)){
        return transaction_book(itineraryId);
      }
    }

    
    return "Booking failed\n";
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_pay(int reservationId) {
    if(userLoggedIn.equals("")){
      return "Cannot pay, not logged in\n";
    }

    try{
      conn.setAutoCommit(false);
      
      //get the reservation 
      getReservationRid.clearParameters();
      getReservationRid.setInt(1, reservationId);
      ResultSet rsrv = getReservationRid.executeQuery();

      //reservation not found or already paid for
      if(!rsrv.next() || (rsrv.getBoolean("is_paid") && rsrv.getString("username").equals(userLoggedIn))){
        conn.rollback();
        conn.setAutoCommit(true);
        return "Cannot find unpaid reservation " + reservationId + " under user: " + userLoggedIn + "\n";
      }

      //check for not enough balance
      getUser.clearParameters();
      getUser.setString(1, rsrv.getString("username"));
      ResultSet user = getUser.executeQuery();
      user.next();
      findFlightFid.clearParameters();
      findFlightFid.setInt(1, rsrv.getInt("flight_1"));
      ResultSet flight = findFlightFid.executeQuery();
      flight.next();
      int price = flight.getInt("price");
      if(rsrv.getInt("flight_2") != 0){
        findFlightFid.setInt(1, rsrv.getInt("flight_2"));
        flight = findFlightFid.executeQuery();
        flight.next();
        price += flight.getInt("price");
      }
      if(price > user.getInt("balance")){
        conn.rollback();
        conn.setAutoCommit(true);
        return "User has only " + user.getInt("balance") + " in account but itinerary costs " + price + "\n";
      }

      //pay the reservation
      payReservation.clearParameters();
      payReservation.setInt(1, reservationId);
      payReservation.executeUpdate();
      updateBalance.clearParameters();
      updateBalance.setInt(1, user.getInt("balance") - price);
      updateBalance.setString(2, userLoggedIn);
      updateBalance.executeUpdate();

      user = getUser.executeQuery();
      user.next();

      conn.commit();
      conn.setAutoCommit(true);
      return "Paid reservation: " + reservationId + " remaining balance: " + user.getInt("balance") + "\n";

    } catch(SQLException e) {
      e.printStackTrace();
      try{
        conn.setAutoCommit(false);
        conn.rollback();
        conn.setAutoCommit(true);
      } catch (Exception exception){
        exception.printStackTrace();
      } 
      if(isDeadlock(e)){
        return transaction_pay(reservationId);
      }
    }


    return "Failed to pay for reservation " + reservationId + "\n";
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_reservations() {
    if(userLoggedIn.equals("")){
      return "Cannot view reservations, not logged in\n";
    }

    try{

      //no reservations
      reservationAmount.clearParameters();
      reservationAmount.setString(1, userLoggedIn);
      ResultSet rsrvAm = reservationAmount.executeQuery();
      rsrvAm.next();
      if(rsrvAm.getInt("cnt") == 0){
        return "No reservations found\n";
      }

      //get all the user's reservations
      userReservations.clearParameters();
      userReservations.setString(1, userLoggedIn);
      ResultSet reservations = userReservations.executeQuery();

      //print out the reservations 
      StringBuffer sb = new StringBuffer();
      while(reservations.next()){
        sb.append("Reservation " + reservations.getInt("rid"));
        sb.append(" paid: " + reservations.getBoolean("is_paid") + ":\n");
        
        //flight 1
        findFlightFid.clearParameters();
        findFlightFid.setInt(1, reservations.getInt("flight_1"));
        ResultSet flight = findFlightFid.executeQuery();
        flight.next();
        sb.append(
          new Flight(
              flight.getInt("fid"),
              flight.getInt("day_of_month"),
              flight.getString("carrier_id"),
              flight.getString("flight_num"),
              flight.getString("origin_city"),
              flight.getString("dest_city"),
              flight.getInt("actual_time"),
              flight.getInt("capacity"),
              flight.getInt("price")
          ).toString() + "\n"
        );

        //flight 2
        if(reservations.getInt("flight_2") != 0){
          findFlightFid.setInt(1, reservations.getInt("flight_2"));
          flight = findFlightFid.executeQuery();
          flight.next();
          sb.append(
            new Flight(
                flight.getInt("fid"),
                flight.getInt("day_of_month"),
                flight.getString("carrier_id"),
                flight.getString("flight_num"),
                flight.getString("origin_city"),
                flight.getString("dest_city"),
                flight.getInt("actual_time"),
                flight.getInt("capacity"),
                flight.getInt("price")
            ).toString() + "\n"
          );
        }
      }

      return sb.toString();
    } catch (Exception e){
      e.printStackTrace();
    }
    return "Failed to retrieve reservations\n";
  }

  /**
   * Example utility function that uses prepared statements
   */
  private int checkFlightCapacity(int fid) throws SQLException {
    flightCapacityStmt.clearParameters();
    flightCapacityStmt.setInt(1, fid);

    ResultSet results = flightCapacityStmt.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }

  /**
   * Utility function to determine whether an error was caused by a deadlock
   */
  private static boolean isDeadlock(SQLException e) {
    return e.getErrorCode() == 1205;
  }

  /**
   * A class to store information about a single flight
   *
   * TODO(hctang): move this into QueryAbstract
   */
  class Flight {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    Flight(int id, int day, String carrier, String fnum, String origin, String dest, int tm,
           int cap, int pri) {
      fid = id;
      dayOfMonth = day;
      carrierId = carrier;
      flightNum = fnum;
      originCity = origin;
      destCity = dest;
      time = tm;
      capacity = cap;
      price = pri;
    }
    
    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: "
          + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time
          + " Capacity: " + capacity + " Price: " + price;
    }
  }

  class Itinerary implements Comparable<Object> {
    private Flight f1;
    private Flight f2;
    private int overall_time;

    public Itinerary(Flight f1) {
      this.f1 = f1;
      this.f2 = null;
      this.overall_time = f1.time;
    }

    public Itinerary(Flight f1, Flight f2) {
      this.f1 = f1;
      this.f2 = f2;
      this.overall_time = f1.time + f2.time;
    }

    public int getTime(){
      return overall_time;
    }

    public Flight getFlight(int flight){
      if(flight == 1){
        return f1;
      }
      return f2;
    }

    public int getFlightAmount(){
      if(f2 != null){
        return 2;
      }
      return 1;
    }

    @Override
    public int compareTo(Object o2) {
      Itinerary it2 = (Itinerary) o2;

      //if the time is the same
      if(this.overall_time == it2.getTime()){
        //if the first flights are the same and both itineraries are indirect
        if((this.f1.fid == it2.getFlight(1).fid) && (this.f1 != null && it2.getFlight(2) != null)){
          return (this.f2.fid) - (it2.getFlight(2).fid);
        }
        return (this.f1.fid) - (it2.getFlight(1).fid);
      }
      return this.overall_time - it2.getTime();
    }
  }
}
