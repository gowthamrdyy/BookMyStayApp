import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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

    public void updateAvailability(String roomType, int newAvailability) {
        if (inventory.containsKey(roomType)) {
            inventory.put(roomType, newAvailability);
        } else {
            System.out.println("Invalid Room Type.");
        }
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }
}

class SearchService {
    private RoomInventory inventory;
    private Map<String, Room> roomCatalog;

    public SearchService(RoomInventory inventory) {
        this.inventory = inventory;
        roomCatalog = new HashMap<>();
        roomCatalog.put("Single", new SingleRoom());
        roomCatalog.put("Double", new DoubleRoom());
        roomCatalog.put("Suite", new SuiteRoom());
    }

    public void searchAvailableRooms() {
        System.out.println("Searching for available rooms...");
        boolean found = false;

        for (String roomType : roomCatalog.keySet()) {
            int availability = inventory.getAvailability(roomType);
            if (availability > 0) {
                System.out.println("\nAvailable: " + roomType + " Room (" + availability + " left)");
                roomCatalog.get(roomType).displayDetails();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No rooms are currently available.");
        }
    }
}

class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    @Override
    public String toString() {
        return "Reservation Request [Guest: " + guestName + ", Room Type: " + roomType + "]";
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

    public void processNextRequest() {
        if (!requestQueue.isEmpty()) {
            Reservation request = requestQueue.poll();
            System.out.println("Processing: " + request);
            // Real allocation will happen in a later use case
            System.out.println("Request prepared for allocation system.");
        } else {
            System.out.println("No pending requests.");
        }
    }

    public void displayQueueStatus() {
        System.out.println("Current queue size: " + requestQueue.size() + " request(s) waiting.");
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        // Initialize Inventory and Booking System
        RoomInventory inventory = new RoomInventory();
        BookingSystem bookingSystem = new BookingSystem();

        System.out.println("--- Submitting Booking Requests (FIFO) ---");
        bookingSystem.addRequest(new Reservation("Alice", "Single"));
        bookingSystem.addRequest(new Reservation("Bob", "Double"));
        bookingSystem.addRequest(new Reservation("Charlie", "Suite"));
        bookingSystem.addRequest(new Reservation("Diana", "Single"));

        System.out.println("\n--- Booking Queue Status ---");
        bookingSystem.displayQueueStatus();

        System.out.println("\n--- Processing Next Requests ---");
        bookingSystem.processNextRequest();
        bookingSystem.processNextRequest();

        System.out.println("\n--- Booking Queue Status After Processing ---");
        bookingSystem.displayQueueStatus();

        System.out.println("\nVersion 5.0");
    }
}
