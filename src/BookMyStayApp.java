import java.util.HashMap;
import java.util.Map;

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

public class BookMyStayApp {
    public static void main(String[] args) {
        // Initialize Inventory
        RoomInventory inventory = new RoomInventory();

        // Initialize Search Service
        SearchService searchService = new SearchService(inventory);

        System.out.println("--- Initial Room Search ---");
        searchService.searchAvailableRooms();

        // Simulating booking (reducing availability)
        System.out.println("\n--- Updating Inventory (Simulating Bookings) ---");
        inventory.updateAvailability("Single", 0); // Sold out
        inventory.updateAvailability("Suite", 1);  // 1 left

        System.out.println("\n--- Room Search After Updates ---");
        searchService.searchAvailableRooms();

        System.out.println("\nVersion 4.0");
    }
}
