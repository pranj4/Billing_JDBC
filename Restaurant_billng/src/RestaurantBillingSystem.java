import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

public class RestaurantBillingSystem {
    private JFrame frame;
    private JPanel panel;
    private JTextField tableNumberField;
    private JTextField itemNameField;
    private JTextField quantityField;
    private JTextField priceField;
    private JTable orderTable;
    private JScrollPane scrollPane;
    private ArrayList<OrderItem> orderItems;
    private Connection connection;

    public RestaurantBillingSystem() {
        initializeComponents();
        initializeDatabaseConnection();
    }

    private void initializeComponents() {
        frame = new JFrame("Restaurant Billing System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(null);

        JLabel tableNumberLabel = new JLabel("Table Number:");
        tableNumberLabel.setBounds(20, 20, 100, 20);
        panel.add(tableNumberLabel);

        tableNumberField = new JTextField(10);
        tableNumberField.setBounds(120, 20, 100, 20);
        panel.add(tableNumberField);

        JLabel itemNameLabel = new JLabel("Item Name:");
        itemNameLabel.setBounds(240, 20, 100, 20);
        panel.add(itemNameLabel);

        itemNameField = new JTextField(10);
        itemNameField.setBounds(320, 20, 100, 20);
        panel.add(itemNameField);

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(440, 20, 100, 20);
        panel.add(quantityLabel);

        quantityField = new JTextField(5);
        quantityField.setBounds(520, 20, 50, 20);
        panel.add(quantityField);

        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setBounds(580, 20, 100, 20);
        panel.add(priceLabel);

        priceField = new JTextField(5);
        priceField.setBounds(640, 20, 50, 20);
        panel.add(priceField);

        JButton addItemButton = new JButton("Add Item");
        addItemButton.setBounds(700, 20, 100, 20);
        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addItemToOrder();
            }
        });
        panel.add(addItemButton);

        JButton exportButton = new JButton("Export to DB");
        exportButton.setBounds(20, 500, 150, 20);
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToDatabase();
            }
        });
        panel.add(exportButton);

        String[] columnNames = {"Table Number", "Item Name", "Quantity", "Price", "Total"};
        orderTable = new JTable(new DefaultTableModel(new Object[][]{}, columnNames));
        scrollPane = new JScrollPane(orderTable);
        scrollPane.setBounds(20, 60, 760, 400);
        panel.add(scrollPane);

        frame.add(panel);
        frame.setVisible(true);

        orderItems = new ArrayList<>();
    }

    private void initializeDatabaseConnection() {
        String url = "jdbc:postgresql://localhost:5432/restaurant";
        String user = "postgres";
        String password = "iamgame25...";

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database connection failed: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addItemToOrder() {
        String tableNumber = tableNumberField.getText();
        String itemName = itemNameField.getText();
        String quantityText = quantityField.getText();
        String priceText = priceField.getText();

        if (tableNumber.isEmpty() || itemName.isEmpty() || quantityText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            double price = Double.parseDouble(priceText);

            OrderItem orderItem = new OrderItem(tableNumber, itemName, quantity, price);
            orderItems.add(orderItem);

            DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
            model.addRow(new Object[]{tableNumber, itemName, quantity, price, quantity * price});
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input for quantity or price.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToDatabase() {
        if (connection == null) {
            JOptionPane.showMessageDialog(frame, "No database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String insertSQL = "INSERT INTO orders (table_number, item_name, quantity, price, total) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (OrderItem item : orderItems) {
                preparedStatement.setString(1, item.getTableNumber());
                preparedStatement.setString(2, item.getItemName());
                preparedStatement.setInt(3, item.getQuantity());
                preparedStatement.setDouble(4, item.getPrice());
                preparedStatement.setDouble(5, item.getQuantity() * item.getPrice());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            JOptionPane.showMessageDialog(frame, "Data exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error exporting data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RestaurantBillingSystem::new);
    }

    // Nested OrderItem class
    class OrderItem {
        private String tableNumber;
        private String itemName;
        private int quantity;
        private double price;

        public OrderItem(String tableNumber, String itemName, int quantity, double price) {
            this.tableNumber = tableNumber;
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
        }

        public String getTableNumber() {
            return tableNumber;
        }

        public String getItemName() {
            return itemName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }
}
