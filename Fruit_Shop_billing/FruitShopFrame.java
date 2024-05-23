import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;


public class FruitShopFrame extends JFrame {

    private JCheckBox[] checkBoxes;
    private JTextField[] rateFields;
    private JTextField[] quantityFields;
    private JTable table;
    private JLabel totalLabel;

    public FruitShopFrame() {
        // Frame title
        setTitle("Fruits and Vegetable Shop");

        // Create JPanels for grouping components
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        // Create JLabels
        JLabel titleLabel = new JLabel("Fruits and Vegetables Shop", JLabel.CENTER);
        totalLabel = new JLabel("TOTAL: $0.0", JLabel.RIGHT);
        JLabel itemLabel = new JLabel("Item");
        JLabel rateLabel = new JLabel("Rate");
        JLabel quantityLabel = new JLabel("Quantity");

        // Create JCheckBoxes
        String[] fruits = {"Banana", "Mango", "Onion", "Tomato", "Potato", "Cabbage", "Spinach", "Jackfruit", "Apples"};
        checkBoxes = new JCheckBox[fruits.length];
        for (int i = 0; i < fruits.length; i++) {
            checkBoxes[i] = new JCheckBox(fruits[i]);
        }

        // Create JTextFields
        rateFields = new JTextField[fruits.length];
        quantityFields = new JTextField[fruits.length];
        for (int i = 0; i < fruits.length; i++) {
            rateFields[i] = new JTextField(4); // Adjusted width
            rateFields[i].setPreferredSize(new Dimension(10,15)); // Adjusted height
            quantityFields[i] = new JTextField(4); // Adjusted width
            quantityFields[i].setPreferredSize(new Dimension(10,15)); // Adjusted height
        }

        // Create JTable with model and column names
        table = new JTable();
        table.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Item", "Rate", "Quantity", "Total"}
        ));
        table.setPreferredSize(new Dimension(400, 200));

        // Create JButton
        JButton calculateButton = new JButton("Calculate Total");
        calculateButton.addActionListener(this::calculateTotalButtonClicked);

        // Add components to left panel
        leftPanel.setLayout(new GridLayout(fruits.length + 1, 3));
        leftPanel.add(itemLabel);
        leftPanel.add(rateLabel);
        leftPanel.add(quantityLabel);
        for (int i = 0; i < fruits.length; i++) {
            leftPanel.add(checkBoxes[i]);
            leftPanel.add(rateFields[i]);
            leftPanel.add(quantityFields[i]);
        }

        // Add components to right panel
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Add components to bottom panel
        bottomPanel.add(totalLabel);
        bottomPanel.add(calculateButton);

        // Add components to the frame layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titleLabel, BorderLayout.NORTH);
        getContentPane().add(leftPanel, BorderLayout.WEST);
        getContentPane().add(rightPanel, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // Set frame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // Center the frame
    }

    private void exportToDatabase(double totalCost) {
        
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            // Establish a connection to the database
            String url = "jdbc:postgresql://localhost:5432/bills";
            String username = "postgres";
            String password = "iamgame25...";
            Connection connection = DriverManager.getConnection(url, username, password);

            // Create SQL insert statement
            String sql = "INSERT INTO billingtable (item, rate, quantity,total) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            // Iterate through table rows and insert each row into the database
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                preparedStatement.setString(1, (String) model.getValueAt(i, 0));
                preparedStatement.setDouble(2, (Double) model.getValueAt(i, 1));
                preparedStatement.setDouble(3, (Double) model.getValueAt(i, 2));
                preparedStatement.setDouble(4, (Double) model.getValueAt(i, 3));
                preparedStatement.setDouble(5, totalCost);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();

            // Close the connection
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateTotalButtonClicked(ActionEvent e) {
        // Clear existing table rows
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        double totalCost = 0.0;

        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                String item = checkBoxes[i].getText();
                double rate = Double.parseDouble(rateFields[i].getText());
                double quantity = Double.parseDouble(quantityFields[i].getText());
                double itemTotal = rate * quantity;
                totalCost += itemTotal;

                model.addRow(new Object[]{item, rate, quantity, itemTotal});
            }
        }

        // Update total label
        totalLabel.setText("TOTAL: Rs " + totalCost);
        exportToDatabase(totalCost);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FruitShopFrame().setVisible(true);
        });
    }
}
