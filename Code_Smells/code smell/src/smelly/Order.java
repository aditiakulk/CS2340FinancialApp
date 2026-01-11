package smelly;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Order {
    private List<Item> items;
    private String customerName;
    private String customerEmail;
  
    private static final Logger logger = Logger.getLogger(Order.class.getName());


    public Order(List<Item> items, String customerName, String customerEmail) {
        this.items = items;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }

    public double calculateTotalPrice() {
    	double total = 0.0;
    	for (Item item : items) {
        	double price = item.getPrice();
        	switch (item.getDiscountType()) {
            	case PERCENTAGE:
                	price -= item.getDiscountAmount() * price;
                	break;
            	case AMOUNT:
                	price -= item.getDiscountAmount();
                	break;
            	default:
                	// no discount
                	break;
        	}
        	total += price * item.getQuantity();
       	    if (item instanceof TaxableItem) {
                TaxableItem taxableItem = (TaxableItem) item;
                double tax = taxableItem.getTaxRate() / 100.0 * item.getPrice();
                total += tax;
            }
        }
    	if (hasGiftCard()) {
        	total -= 10.0; // subtract $10 for gift card
    	}
    	if (total > 100.0) {
        	total *= 0.9; // apply 10% discount for orders over $100
    	}
    	return total;
    }

    public void sendConfirmationEmail() {
        StringBuilder message = new StringBuilder();

        message.append("Thank you for your order, ")
                .append(customerName)
                .append("!\n\n")
                .append("Your order details:\n");

        for (Item item : items) {
            message.append(item.getName())
                    .append(" - ")
                    .append(item.getPrice())
                    .append("\n");
        }

        message.append("Total: ").append(calculateTotalPrice());

        EmailSender.sendEmail(customerEmail, "Order Confirmation", message.toString());
    }


    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public boolean hasGiftCard() {
        boolean giftCard = false;
        for (Item item : items) {
            if (item instanceof GiftCardItem) {
                giftCard = true;
                break;
            }
        }
        return giftCard;
    }

   public void printOrder() {
        logger.log(Level.INFO, "Order Details:");
        for (Item item : items) {
            logger.log(Level.INFO, item.getName() + " - " + item.getPrice());
        }
   }

   public void addItemsFromAnotherOrder(Order otherOrder) {
        for (Item item : otherOrder.getItems()) {
            items.add(item);
        }
   }

}

