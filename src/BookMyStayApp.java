import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

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
    private String allocatedRoomId;
    private boolean cancelled;

    public Reservation(String reservationId, String guestName, String roomType) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.cancelled = false;
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
    
    public void setAllocatedRoomId(String allocatedRoomId) {
        this.allocatedRoomId = allocatedRoomId;
    }
    
    public String getAllocatedRoomId() {
        return allocatedRoomId;
    }
    
    public void cancel() {
        this.cancelled = true;
    }
    
    public boolean isCancelled() {
        return cancelled;
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
    private CancellationService cancellationService;
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
    
    public void setCancellationService(CancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    public void processReservation(Reservation request) {
        String roomType = request.getRoomType();
        
        try {
            inventory.validateRoomType(roomType);
            int available = inventory.getAvailability(roomType);

            if (available > 0) {
                // If cancellationService returns a room we can reuse it, else generate new
                String roomId = cancellationService != null ? cancellationService.reuseReleasedRoomIfAvailable(roomType) : null;
                
                if (roomId == null) {
                    roomId = roomType.substring(0, 1).toUpperCase() + nextRoomId++;
                }
                
                if(!allocatedRooms.get(roomType).contains(roomId)){
                    inventory.updateAvailability(roomType, available - 1);
                    allocatedRooms.get(roomType).add(roomId);
                    request.setAllocatedRoomId(roomId);
                    reportService.addBookingToHistory(request);
                    if (cancellationService != null) {
                        cancellationService.addActiveReservation(request);
                    }
                    System.out.println("Reservation Confirmed! ID: " + request.getReservationId() + " | Guest: " + request.getGuestName() + " | Allocated Room: " + roomId);
                } else {
                    System.out.println("Reservation Failed (Double Booking Prevented) for " + roomType + " Room.");
                }
            } else {
                throw new RoomUnavailableException("Reservation Failed. No availability for " + roomType + " Room.");
            }
        } catch (InvalidRoomTypeException | RoomUnavailableException e) {
            System.err.println("Booking Error: " + e.getMessage());
        }
    }
    
    public void removeAllocatedRoom(String roomType, String roomId) {
        if(allocatedRooms.containsKey(roomType)) {
            allocatedRooms.get(roomType).remove(roomId);
        }
    }
}

class CancellationService {
    private RoomInventory inventory;
    private RoomAllocationService allocationService;
    private Map<String, Reservation> activeReservations;
    private Stack<String> releasedRoomIDs;

    public CancellationService(RoomInventory inventory, RoomAllocationService allocationService) {
        this.inventory = inventory;
        this.allocationService = allocationService;
        this.activeReservations = new HashMap<>();
        this.releasedRoomIDs = new Stack<>();
    }

    public void addActiveReservation(Reservation reservation) {
        activeReservations.put(reservation.getReservationId(), reservation);
    }

    public void cancelBooking(String reservationId) {
        try {
            if (!activeReservations.containsKey(reservationId)) {
                System.out.println("Cancellation Failed. Reservation ID " + reservationId + " not found.");
                return;
            }

            Reservation reservation = activeReservations.get(reservationId);
            if (reservation.isCancelled()) {
                System.out.println("Cancellation Failed. Reservation ID " + reservationId + " is already cancelled.");
                return;
            }

            String roomType = reservation.getRoomType();
            String roomId = reservation.getAllocatedRoomId();

            // LIFO Rollback recording structure
            releasedRoomIDs.push(roomId);
            
            // Increment inventory accurately immediately 
            int currentAvailability = inventory.getAvailability(roomType);
            inventory.updateAvailability(roomType, currentAvailability + 1);

            // Removing allocation from actively tracked assignments
            allocationService.removeAllocatedRoom(roomType, roomId);

            reservation.cancel();

            System.out.println("Cancellation Successful! Reservation ID: " + reservationId + " has been cancelled.");
            System.out.println("Rollback performed: Room ID " + roomId + " released back to pool.");

        } catch (Exception e) {
             System.err.println("Cancellation Error: " + e.getMessage());
        }
    }
    
    // Using LIFO conceptually back returning ID
    public String reuseReleasedRoomIfAvailable(String requestedRoomType) {
        if(releasedRoomIDs.isEmpty()) return null;
        
        // Peek to see if matched type
        Stack<String> tempStack = new Stack<>();
        String foundRoomId = null;
        
        while(!releasedRoomIDs.isEmpty()){
            String peekId = releasedRoomIDs.pop();
            // Checking if letter matches our Room Type substring identifier (e.g. S == S)
            if(peekId.startsWith(requestedRoomType.substring(0, 1).toUpperCase())){
                foundRoomId = peekId;
                break;
            } else {
                tempStack.push(peekId);
            }
        }
        
        while(!tempStack.isEmpty()){
             releasedRoomIDs.push(tempStack.pop());
        }
        
        return foundRoomId;
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

        System.out.println("Total Bookings Tracked: " + bookingHistory.size());
        for (int i = 0; i < bookingHistory.size(); i++) {
            Reservation r = bookingHistory.get(i);
            String status = r.isCancelled() ? " [CANCELLED]" : "";
            System.out.println((i + 1) + ". Booking: ID=" + r.getReservationId() + ", Guest=" + r.getGuestName() + ", Room=" + r.getRoomType() + status);
        }
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        RoomInventory inventory = new RoomInventory();
        BookingReportService reportService = new BookingReportService();
        BookingSystem bookingSystem = new BookingSystem();
        RoomAllocationService allocationService = new RoomAllocationService(inventory, reportService);
        CancellationService cancellationService = new CancellationService(inventory, allocationService);
        allocationService.setCancellationService(cancellationService);

        System.out.println("--- Submitting Booking Requests ---");
        bookingSystem.addRequest(new Reservation("RES101", "Alice", "Single"));
        bookingSystem.addRequest(new Reservation("RES102", "Bob", "Double"));
        bookingSystem.addRequest(new Reservation("RES103", "Charlie", "Suite"));


        System.out.println("\n--- Processing Booking Requests ---");
        while (bookingSystem.hasRequests()) {
            Reservation request = bookingSystem.getNextRequest();
            System.out.println("\nProcessing: " + request);
            allocationService.processReservation(request);
        }

        System.out.println("\n--- Initiating Cancellations ---");
        System.out.println("\nAttempting to cancel RES102...");
        cancellationService.cancelBooking("RES102");
        
        System.out.println("\nAttempting to cancel invalid RES999...");
        cancellationService.cancelBooking("RES999");
        
        System.out.println("\nAttempting to cancel already cancelled RES102...");
        cancellationService.cancelBooking("RES102");

        System.out.println("\n--- Updated Booking History After Cancellations ---");
        reportService.generateBookingHistoryReport();
        
        System.out.println("\n--- Final Inventory Status ---");
        try {
            System.out.println("Single Room Availability: " + inventory.getAvailability("Single"));
            System.out.println("Double Room Availability: " + inventory.getAvailability("Double"));
            System.out.println("Suite Room Availability: " + inventory.getAvailability("Suite"));
        } catch (InvalidRoomTypeException e) {
            e.printStackTrace();
        }

        System.out.println("\nVersion 10.0");
    }
}
