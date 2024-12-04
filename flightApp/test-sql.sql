SELECT TOP (1) 
      F1.fid AS F1_fid, 
      F1.day_of_month AS F1_dayOfMonth, 
      F1.carrier_id AS F1_carrier, 
      F1.flight_num AS F1_flightNum, 
      F1.origin_city AS F1_originCity, 
      F1.dest_city AS F1_destCity, 
      F1.actual_time AS F1_time, 
      F1.capacity AS F1_capacity, 
      F1.price AS F1_price,   

      F2.fid AS F2_fid, 
      F2.day_of_month AS F2_dayOfMonth, 
      F2.carrier_id AS F2_carrier, 
      F2.flight_num AS F2_flightNum, 
      F2.origin_city AS F2_originCity, 
      F2.dest_city AS F2_destCity, 
      F2.actual_time AS F2_time, 
      F2.capacity AS F2_capacity, 
      F2.price AS F2_price  

    FROM FLIGHTS AS F1, FLIGHTS AS F2  
    
    WHERE F1.fid != F2.fid AND  
      F1.day_of_month = 1 AND  
      F1.day_of_month = F2.day_of_month AND  
      F1.origin_city = 1 AND  
      F1.dest_city = F2.origin_city AND  
      F2.dest_city = 1 AND  
      F1.canceled = 0 AND  
      F2.canceled = 0   
    ORDER BY (F1.actual_time + F2.actual_time),  
      F1.fid, F2.fid