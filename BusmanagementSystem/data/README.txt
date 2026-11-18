BUS MANAGEMENT SYSTEM - FILE HANDLING VERSION

The Java classes now use text files instead of SQL Server/JDBC.
Keep the data folder beside the src folder (at the project root), or run the
program with the project root as the working directory.

DATA FILE FORMATS (one record per line; fields separated by |)

passengers.txt: passengerId|name|email|password|phone
buses.txt:      busId|busNumber|busType
routes.txt:     routeId|startLocation|endLocation
schedules.txt:  scheduleId|busId|routeId|departureTime|arrivalTime|ticketPrice|operatingDays
seats.txt:      seatId|busId|seatNumber
bookings.txt:   bookingId|passengerId|scheduleId|seatId|bookingDate|status
tickets.txt:    bookingId|ticketNumber|issueDate
ratings.txt:    passengerId|busId|ratingValue|comment
complaints.txt: complaintId|passengerId|description|submittedDate
lostitems.txt:  reportId|passengerId|description|travelDate|submittedDate

The data files are intentionally empty as requested. Add bus, route, schedule and
seat records later using the formats above. IDs in related files must match.
Do not type the | character inside a field.
