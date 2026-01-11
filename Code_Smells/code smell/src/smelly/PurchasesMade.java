package smelly;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PurchasesMade {
    private static final Logger logger = Logger.getLogger(PurchasesMade.class.getName());
	public static void main(String[] args) {
        Item item1 = new Item("Book", 20, 1, DiscountType.AMOUNT, 5);
        Item item2 = new TaxableItem("Laptop", 1000, 1, DiscountType.PERCENTAGE, 0.1);
        Item item3 = new GiftCardItem("Gift Card", 10, 1, DiscountType.AMOUNT, 0);

        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        Order order = new Order(items, "John Doe", "johndoe@example.com");

        logger.log(Level.INFO, "Total Price: {0}", order.calculateTotalPrice());

        order.sendConfirmationEmail();

        order.printOrder();
    }
}

