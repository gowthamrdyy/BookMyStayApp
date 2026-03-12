import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

class RoomInventory implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 5);
        inventory.put("Double", 10);
        inventory.put("Suite", 2);
    }

    public synchronized void validateRoomType(String roomType) throws InvalidRoomTypeException {
        if (!inventory.containsKey(roomType)) {
            throw new InvalidRoomTypeException("Invalid Room Type: '" + roomType + "'. Valid types are Single, Double, Suite.");
        }
    }

    public synchronized void updateAvailability(String roomType, int newAvailability) throws InvalidRoomTypeException {
        validateRoomType(roomType);
        inventory.put(roomType, newAvailability);
    }

    public synchronized int getAvailability(String roomType) throws InvalidRoomTypeException {
        validateRoomType(roomType);
        return inventory.get(roomType);
    }
}

class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
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

    public synchronized void addRequest(Reservation request) {
        requestQueue.offer(request);
        System.out.println(Thread.currentThread().getName() + " added to queue: " + request);
    }

    public synchronized Reservation getNextRequest() {
        return requestQueue.poll();
    }

    public synchronized boolean hasRequests() {
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

    public synchronized void processReservation(Reservation request) {
        String roomType = request.getRoomType();
        
        try {
            inventory.validateRoomType(roomType);
            int available = inventory.getAvailability(roomType);

            if (available > 0) {
                String roomId = roomType.substring(0, 1).toUpperCase() + nextRoomId++;
                
                if(!allocatedRooms.get(roomType).contains(roomId)){
                    inventory.updateAvailability(roomType, available - 1);
                    allocatedRooms.get(roomType).add(roomId);
                    request.setAllocatedRoomId(roomId);
                    reportService.addBookingToHistory(request);
                    System.out.println(Thread.currentThread().getName() + " -> Reservation Confirmed! ID: " + request.getReservationId() + " | Guest: " + request.getGuestName() + " | Allocated Room: " + roomId);
                } else {
                    System.out.println(Thread.currentThread().getName() + " -> Reservation Failed (Double Booking Prevented) for " + roomType + " Room.");
                }
            } else {
                throw new RoomUnavailableException("Reservation Failed. No availability for " + roomType + " Room.");
            }
        } catch (InvalidRoomTypeException | RoomUnavailableException e) {
            System.out.println(Thread.currentThread().getName() + " -> Booking Error for " + request.getReservationId() + ": " + e.getMessage());
        }
    }
    
    public synchronized void removeAllocatedRoom(String roomType, String roomId) {
        if(allocatedRooms.containsKey(roomType)) {
            allocatedRooms.get(roomType).remove(roomId);
        }
    }
}

class BookingReportService implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Reservation> bookingHistory;

    public BookingReportService() {
        this.bookingHistory = new ArrayList<>();
    }

    public synchronized void addBookingToHistory(Reservation reservation) {
        bookingHistory.add(reservation);
    }

    public synchronized void generateBookingHistoryReport() {
        System.out.println("\n--- Booking History Report ---");
        if (bookingHistory.isEmpty()) {
            System.out.println("No bookings confirmed yet.");
            return;
        }

        System.out.println("Total Bookings Tracked: " + bookingHistory.size());
        for (int i = 0; i < bookingHistory.size(); i++) {
            Reservation r = bookingHistory.get(i);
            String status = r.isCancelled() ? " [CANCELLED]" : "";
            System.out.println((i + 1) + ". Booking: ID=" + r.getReservationId() + ", Guest=" + r.getGuestName() + ", Room=" + r.getRoomType() + ", Allocated: " + r.getAllocatedRoomId() + status);
        }
    }
}

class PersistenceService {
    private static final String DATA_FILE = "system_state.ser";

    public void saveSystemState(RoomInventory inventory, BookingReportService reportService) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(inventory);
            out.writeObject(reportService);
            System.out.println("System state saved successfully to '" + DATA_FILE + "'.");
        } catch (IOException e) {
            System.err.println("Error saving system state: " + e.getMessage());
        }
    }

    public Object[] loadSystemState() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No previous data found. Starting fresh.");
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            RoomInventory inventory = (RoomInventory) in.readObject();
            BookingReportService reportService = (BookingReportService) in.readObject();
            System.out.println("System state recovered successfully from '" + DATA_FILE + "'.");
            return new Object[]{inventory, reportService};
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading system state: " + e.getMessage());
            return null;
        }
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        PersistenceService persistenceService = new PersistenceService();
        RoomInventory inventory;
        BookingReportService reportService;

        Object[] loadedData = persistenceService.loadSystemState();

        if (loadedData != null) {
            inventory = (RoomInventory) loadedData[0];
            reportService = (BookingReportService) loadedData[1];
        } else {
            inventory = new RoomInventory();
            reportService = new BookingReportService();
        }

        BookingSystem bookingSystem = new BookingSystem();
        RoomAllocationService allocationService = new RoomAllocationService(inventory, reportService);

        if (loadedData == null) {
            System.out.println("\n--- Initializing New Bookings ---");
            bookingSystem.addRequest(new Reservation("RES101", "Alice", "Single"));
            bookingSystem.addRequest(new Reservation("RES102", "Bob", "Double"));
            bookingSystem.addRequest(new Reservation("RES103", "Charlie", "Suite"));
            
            System.out.println("\n--- Processing Booking Requests ---");
            Thread.currentThread().setName("MainThread");
            while (bookingSystem.hasRequests()) {
                allocationService.processReservation(bookingSystem.getNextRequest());
            }

            System.out.println("\n--- Shutting Down: Saving State ---");
            persistenceService.saveSystemState(inventory, reportService);
        } else {
            System.out.println("\n--- Displaying Restored State ---");
            reportService.generateBookingHistoryReport();
            
            try {
                System.out.println("\n--- Restored Inventory Status ---");
                System.out.println("Single Room Availability: " + inventory.getAvailability("Single"));
                System.out.println("Double Room Availability: " + inventory.getAvailability("Double"));
                System.out.println("Suite Room Availability: " + inventory.getAvailability("Suite"));
            } catch (InvalidRoomTypeException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nVersion 12.0");
    }
}
