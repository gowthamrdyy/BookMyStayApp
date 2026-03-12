import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// Custom Exceptions
class InvalidRoomTypeException extends Exception {
    public InvalidRoomTypeException(String message) {
        super(message);
    }
}

class RoomUnavailableException extends Exception {
    public RoomUnavailableException(String message) {
        super(message);
    }
}

abstract class Room {
    protected int numberOfBeds;
    protected double size;
    protected double price;

    public Room(int numberOfBeds, double size, double price) {
        this.numberOfBeds = numberOfBeds;
        this.size = size;
        this.price = price;
    }

    public abstract void displayDetails();
}

class SingleRoom extends Room {
    public SingleRoom() {
        super(1, 150.0, 100.0);
    }

    @Override
    public void displayDetails() {
        System.out.println("Single Room Details: Beds: " + numberOfBeds + ", Size : " + size + " sq ft, Price : $" + price);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super(2, 250.0, 150.0);
    }

    @Override
    public void displayDetails() {
        System.out.println("Double Room Details: Beds: " + numberOfBeds + ", Size : " + size + " sq ft, Price : $" + price);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super(3, 400.0, 300.0);
    }

    @Override
    public void displayDetails() {
        System.out.println("Suite Room Details: Beds: " + numberOfBeds + ", Size : " + size + " sq ft, Price : $" + price);
    }
}

class RoomInventory {
    private Map<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 5);
        inventory.put("Double", 10);
        inventory.put("Suite", 2);
    }

    public void validateRoomType(String roomType) throws InvalidRoomTypeException {
        if (!inventory.containsKey(roomType)) {
            throw new InvalidRoomTypeException("Invalid Room Type: '" + roomType + "'. Valid types are Single, Double, Suite.");
        }
    }

    public void updateAvailability(String roomType, int newAvailability) throws InvalidRoomTypeException {
        validateRoomType(roomType);
        inventory.put(roomType, newAvailability);
    }

    public int getAvailability(String roomType) throws InvalidRoomTypeException {
        validateRoomType(roomType);
        return inventory.get(roomType);
    }
}

class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;

    public Reservation(String reservationId, String guestName, String roomType) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    @Override
    public String toString() {
        return "Reservation Request [ID: " + reservationId + ", Guest: " + guestName + ", Room Type: " + roomType + "]";
    }
}

class BookingSystem {
    private Queue<Reservation> requestQueue;

    public BookingSystem() {
        requestQueue = new LinkedList<>();
    }

    public void addRequest(Reservation request) {
        requestQueue.offer(request);
        System.out.println("Added to queue: " + request);
    }

    public Reservation getNextRequest() {
        return requestQueue.poll();
    }

    public boolean hasRequests() {
        return !requestQueue.isEmpty();
    }
}

class RoomAllocationService {
    private RoomInventory inventory;
    private BookingReportService reportService;
    private Map<String, Set<String>> allocatedRooms;
    private int nextRoomId;

    public RoomAllocationService(RoomInventory inventory, BookingReportService reportService) {
        this.inventory = inventory;
        this.reportService = reportService;
        this.allocatedRooms = new HashMap<>();
        this.allocatedRooms.put("Single", new HashSet<>());
        this.allocatedRooms.put("Double", new HashSet<>());
        this.allocatedRooms.put("Suite", new HashSet<>());
        this.nextRoomId = 101; 
    }

    public void processReservation(Reservation request) {
        String roomType = request.getRoomType();
        
        try {
            inventory.validateRoomType(roomType);
            int available = inventory.getAvailability(roomType);

            if (available > 0) {
                String roomId = roomType.substring(0, 1).toUpperCase() + nextRoomId++;
                if(allocatedRooms.get(roomType).add(roomId)){
                    inventory.updateAvailability(roomType, available - 1);
                    reportService.addBookingToHistory(request);
                    System.out.println("Reservation Confirmed! ID: " + request.getReservationId() + " | Guest: " + request.getGuestName() + " | Allocated Room: " + roomId);
                } else {
                    System.out.println("Reservation Failed (Double Booking Prevented) for " + roomType + " Room.");
                }
            } else {
                throw new RoomUnavailableException("Reservation Failed. No availability for " + roomType + " Room.");
            }
        } catch (InvalidRoomTypeException e) {
            System.err.println("Booking Error: " + e.getMessage());
        } catch (RoomUnavailableException e) {
            System.err.println("Booking Error: " + e.getMessage());
        }
    }
}

class BookingReportService {
    private List<Reservation> bookingHistory;

    public BookingReportService() {
        this.bookingHistory = new ArrayList<>();
    }

    public void addBookingToHistory(Reservation reservation) {
        bookingHistory.add(reservation);
    }

    public void generateBookingHistoryReport() {
        System.out.println("\n--- Booking History Report ---");
        if (bookingHistory.isEmpty()) {
            System.out.println("No bookings confirmed yet.");
            return;
        }

        System.out.println("Total Confirmed Bookings: " + bookingHistory.size());
        for (int i = 0; i < bookingHistory.size(); i++) {
            Reservation r = bookingHistory.get(i);
            System.out.println((i + 1) + ". Confirmed Booking: ID=" + r.getReservationId() + ", Guest=" + r.getGuestName() + ", Room=" + r.getRoomType());
        }
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        RoomInventory inventory = new RoomInventory();
        BookingReportService reportService = new BookingReportService();
        BookingSystem bookingSystem = new BookingSystem();
        RoomAllocationService allocationService = new RoomAllocationService(inventory, reportService);

        System.out.println("--- Submitting Booking Requests ---");
        bookingSystem.addRequest(new Reservation("RES101", "Alice", "Single"));
        bookingSystem.addRequest(new Reservation("RES102", "Bob", "Penthouse")); // Invalid room type
        bookingSystem.addRequest(new Reservation("RES103", "Charlie", "Suite"));
        bookingSystem.addRequest(new Reservation("RES104", "Diana", "Suite"));
        bookingSystem.addRequest(new Reservation("RES105", "Eve", "Suite"));


        System.out.println("\n--- Processing Booking Requests ---");
        while (bookingSystem.hasRequests()) {
            Reservation request = bookingSystem.getNextRequest();
            System.out.println("\nProcessing: " + request);
            allocationService.processReservation(request);
        }

        System.out.println("\nVersion 9.0");
    }
}
