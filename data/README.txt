BUS MANAGEMENT SYSTEM - SHARED DATA

The Passenger, Conductor, Driver and Owner applications read and write the same
text files in the shared-data folder, which sits beside the four project folders.
It is created automatically the first time any application starts.

FILES USED BY THE PASSENGER APPLICATION (fields separated by |)

users.txt:                 userId|name|email|password|Passenger|phone
buses.txt:                 busId|busNumber|capacity|status|speed|busType
routes.txt:                routeId|startLocation|endLocation|distance
trips.txt:                 tripId|busId|routeId|departureTime|status|currentLocation
bus_schedule_details.txt:  schedule_id|bus_number|bus_type|start|end|departure|arrival|ticket_price|operating_days
bookings.txt:              bookingId|passengerId|scheduleId|seatId|bookingDate|status
tickets.txt:               ticketId|passengerId|tripId|fare|ticketTime
ratings.txt:               passengerId|busId|ratingValue|comment
complaints.txt:            complaintId|passengerId|description|submittedDate
lostitems.txt:             reportId|passengerId|description|travelDate|submittedDate|status

Buses, routes, trips and schedule details are managed by the Owner application.
Seats are generated from the bus capacity. The scheduleId in a booking is the tripId.
Do not type the | character inside a field.
