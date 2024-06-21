import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

class HospitalManagementSystem extends JFrame {

    private JComboBox<Staff> staffComboBox;
    private JComboBox<Doctor> doctorComboBox;
    private JTextField patientNameField;
    private JTextField daysStayedField;
    private JTextArea billTextArea;
    private JTextArea patientHistoryTextArea;
    private JTextArea medicationTextArea;
    private JButton addButton;
    private JButton printBillButton;
    private JButton addMedicationButton;

    private EmergencyWard emergencyWard;
    private LoginDialog loginDialog;
    private List<Staff> staffList;
    private List<Doctor> doctorList;
    private List<Patient> patientList;
    private Staff currentUser;
    private String[] availableMedications = {"Medication1", "Medication2", "Medication3"};


    private JTextArea emergencyStaffTextArea;
    private JTextArea doctorRoundsTextArea;
    private JButton emergencyWardButton;
    private JButton emergencyAdmissionButton;
    private JTextArea admissionDetailsTextArea;

    private Patient lastAdmittedPatient = null;

    public HospitalManagementSystem() {
        setTitle("Hospital Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        staffList = new ArrayList<>();
        doctorList = new ArrayList<>();
        patientList = new ArrayList<>();

        // Create components

        staffComboBox = new JComboBox<>();
        doctorComboBox = new JComboBox<>();
        patientNameField = new JTextField(20);
        daysStayedField = new JTextField(5);
        billTextArea = new JTextArea(10, 30);
        medicationTextArea = new JTextArea(10, 15);
        patientHistoryTextArea = new JTextArea(10, 30);
        addButton = new JButton("Add Patient");
        addButton.setLocation(20, 20);
        printBillButton = new JButton("Print Bill");
        addMedicationButton = new JButton("Add medication");
        JButton emergencyWardButton = new JButton("Emergency Ward Status");
        loginDialog = new LoginDialog(this);
        JButton viewAllPatientsButton = new JButton("View All Patients");


        // Layout components...
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4,1));
        panel.add(new JLabel("\nPatient Name:"));
        panel.add(patientNameField);
        panel.add(new JLabel("Days Stayed:"));
        panel.add(daysStayedField);
        panel.add(new JLabel("Staff:"));
        panel.add(staffComboBox);
        panel.add(new JLabel("Doctor:"));
        panel.add(doctorComboBox);
        panel.add(addButton);
        panel.add(printBillButton);
        panel.add(addMedicationButton);
        panel.add(emergencyWardButton);
        emergencyAdmissionButton = new JButton("Admit to Emergency Ward");
        panel.add(emergencyAdmissionButton);
        panel.add(viewAllPatientsButton);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 6));
        JPanel patientDetailsPanel = new JPanel(new BorderLayout());
        patientDetailsPanel.add(new JScrollPane(patientHistoryTextArea), BorderLayout.CENTER);

        JPanel medicationPanel = new JPanel(new BorderLayout());
        medicationPanel.add(new JScrollPane(medicationTextArea), BorderLayout.CENTER);

        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.add(new JScrollPane(billTextArea), BorderLayout.CENTER);

        bottomPanel.add(patientDetailsPanel);
        bottomPanel.add(medicationPanel);
        bottomPanel.add(billPanel);

        emergencyStaffTextArea = new JTextArea(10, 15);
        doctorRoundsTextArea = new JTextArea(10, 15);
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        rightPanel.add(new JScrollPane(emergencyStaffTextArea));
        rightPanel.add(new JScrollPane(doctorRoundsTextArea));
        add(rightPanel, BorderLayout.EAST);



        // Set layout for the frame
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
        bottomPanel.add(patientDetailsPanel, BorderLayout.WEST);
        bottomPanel.add(billPanel, BorderLayout.CENTER);
        bottomPanel.add(medicationPanel, BorderLayout.EAST);
        staffList.add(new Staff("Admin", "admin", "Administrator"));
        staffList.add(new Staff("Nurse", "nurse", "Nurse"));
        staffList.add(new Staff("Receptionist", "receptionist", "Receptionist"));
        doctorList.add(new Doctor("Dr. Smith", 200.0));
        doctorList.add(new Doctor("Dr. Johnson", 250.0));
        // Populate combo boxes...
        for (Staff staff : staffList) {
            staffComboBox.addItem(staff);
        }

        for (Doctor doctor : doctorList) {
            doctorComboBox.addItem(doctor);
        }





        admissionDetailsTextArea = new JTextArea(10, 40); // Initialize the text area
        JPanel admissionDetailsPanel = new JPanel();
        admissionDetailsPanel.add(new JScrollPane(admissionDetailsTextArea));
        add(admissionDetailsPanel, BorderLayout.SOUTH);

        emergencyWard = new EmergencyWard(10);




        // Add action listeners...
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPatient();
            }
        });

        printBillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printBill();
            }
        });
        addMedicationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMedication();
            }
        });


        emergencyWardButton.addActionListener(e -> displayEmergencyWardStatus());

        emergencyAdmissionButton.addActionListener(e -> admitToEmergencyWard());

        viewAllPatientsButton.addActionListener(e -> viewAllPatients());

        // Display login dialog
        displayLoginDialog();
    }

    // Implement View All Patients functionality
    private void viewAllPatients() {
        StringBuilder allPatients = new StringBuilder();
        if (!patientList.isEmpty()) {
            allPatients.append("All Patients:\n");
            for (Patient patient : patientList) {
                allPatients.append(patient.toString()).append("\n\n");
            }
        } else {
            allPatients.append("No patients admitted at the moment.");
        }
        JOptionPane.showMessageDialog(this, allPatients.toString(), "All Patients", JOptionPane.INFORMATION_MESSAGE);
    }

    private void displayLoginDialog() {
        loginDialog.setVisible(true);
    }

    private void admitToEmergencyWard() {
        Patient selectedPatient = getSelectedPatient();
        if (selectedPatient != null) {
            if (emergencyWard.getEmptyBeds() > 0) {
                emergencyWard.occupyBed();
                Staff assignedStaff = getAvailableStaff();
                if (assignedStaff != null) {
                    selectedPatient.staff = assignedStaff;
                    updateBillWithEmergencyInfo(selectedPatient);
                    updateBillTextArea();
                    displayAdmissionDetails(selectedPatient, assignedStaff);

                    int remainingStaff = getRemainingStaffCount();
                    int remainingBeds = emergencyWard.getEmptyBeds();
                    String patientEmergencyMessage = "Patient " + selectedPatient.getName() + " is in the Emergency Ward.";
                    JOptionPane.showMessageDialog(this,
                            patientEmergencyMessage +
                                    "\nRemaining Staff: " + remainingStaff +
                                    "\nRemaining Beds in Emergency Ward: " + remainingBeds,
                            "Emergency Ward Information",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No available staff at the moment!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Emergency ward is full!");
            }
        }
    }

    private Staff getAvailableStaff() {
        for (Staff staff : staffList) {
            if (!isStaffAssigned(staff)) {
                return staff;
            }
        }
        return null;
    }

    private boolean isStaffAssigned(Staff staff) {
        for (Patient patient : patientList) {
            if (patient.staff != null && patient.staff.equals(staff) && patient.isEmergency()) {
                return true;
            }
        }
        return false;
    }

    private void updateBillWithEmergencyInfo(Patient patient) {
        patient.addToBill("\nEmergency Ward Admission Details:");
        patient.addToBill("Emergency Ward Patient: Yes");
        patient.addToBill("Assigned Staff: " + patient.staff.getUsername());
    }

    private void displayAdmissionDetails(Patient patient, Staff assignedStaff) {
        StringBuilder admissionDetails = new StringBuilder();
        admissionDetails.append("Admission Details for Patient: ").append(patient.name).append("\n\n");

        // Display the assigned staff for the patient
        admissionDetails.append("Assigned Staff: ").append(assignedStaff.getUsername()).append("\n");

        // Display the remaining available staff
        admissionDetails.append("Remaining Available Staff:\n");
        List<Staff> availableStaff = getAvailableStaffList();
        for (Staff staff : availableStaff) {
            admissionDetails.append(staff.getUsername()).append("\n");
        }

        // Display details about the doctor who checked the patient
        Doctor checkingDoctor = patient.getDoctor();
        if (checkingDoctor != null) {
            admissionDetails.append("\nDoctor who checked: ").append(checkingDoctor.getName()).append("\n");
            admissionDetails.append("Doctor's Rounds: ").append(getDoctorRounds(checkingDoctor)).append("\n");
        } else {
            admissionDetails.append("\nDoctor who checked: Not checked yet\n");
        }

        admissionDetailsTextArea.setText(admissionDetails.toString());
    }

    private List<Staff> getAvailableStaffList() {
        List<Staff> availableStaff = new ArrayList<>();
        for (Staff staff : staffList) {
            if (!isStaffAssigned(staff)) {
                availableStaff.add(staff);
            }
        }
        return availableStaff;
    }

    private String getDoctorRounds(Doctor doctor) {
        return doctor.getRounds();
    }

    private int getRemainingStaffCount() {
        int assignedStaffCount = 0;
        for (Patient patient : patientList) {
            if (patient.isEmergency() && patient.staff != null) {
                assignedStaffCount++;
            }
        }
        return staffList.size() - assignedStaffCount;
    }


    private void displayRemainingStaff() {
        int remainingStaffCount = getRemainingStaffCount();
        StringBuilder staffInfo = new StringBuilder();
        staffInfo.append("Remaining Available Staff in Emergency Ward: ").append(remainingStaffCount).append("\n");

        // List all available staff
        List<Staff> availableStaff = getAvailableStaffList();
        staffInfo.append("Available Staff:\n");
        for (Staff staff : availableStaff) {
            staffInfo.append(staff.getUsername()).append("\n");
        }

        JOptionPane.showMessageDialog(this, staffInfo.toString(), "Remaining Staff in Emergency Ward", JOptionPane.INFORMATION_MESSAGE);
    }

    private void displayEmergencyWardStatus() {
        JOptionPane.showMessageDialog(this,
                "Filled Beds: " + emergencyWard.getFilledBeds() +
                        "\nEmpty Beds: " + emergencyWard.getEmptyBeds() +
                        "\n\n" + emergencyWard.displayAvailableDoctors() +
                        "\n" + emergencyWard.displayAvailableStaff(),
                "Emergency Ward Status", JOptionPane.INFORMATION_MESSAGE);
    }

    private void EmergencyWardAdmit() {
        Patient selectedPatient = getSelectedPatient();
        if (selectedPatient != null) {
            emergencyWard.occupyBed();
            reduceStaffCount(); // Reduce available staff count
            selectedPatient.setEmergency(true); // Set patient as emergency
            updateBillWithDoctor(selectedPatient);
            updateBillTextArea();
        }
    }

    private void reduceStaffCount() {
        // Reduce the total staff count when a patient is admitted
        if (emergencyWard.getFilledBeds() > 0) {
            emergencyWard.releaseStaff();
        }
    }

    private void updateBillWithDoctor(Patient patient) {
        // Add the doctor checking the patient to the bill
        Doctor checkingDoctor = patient.getDoctor();
        if (checkingDoctor != null) {
            patient.addToBill("Checked by: " + checkingDoctor.getName());
        }
    }


    private void addPatient() {


        String patientName = patientNameField.getText();
        int daysStayed = Integer.parseInt(daysStayedField.getText());
        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();

        Patient patient = new Patient(patientName, selectedDoctor, currentUser);
        patient.setDaysStayed(daysStayed);

        // Add patient to the list
        patientList.add(patient);

        // Clear input fields
        patientNameField.setText("");
        daysStayedField.setText("");

        // Update bill text area
        updateBillTextArea();

        // Update patient history text area
        updatePatientHistoryTextArea();
    }

    private void addMedication() {

        Patient selectedPatient = getSelectedPatient();
        if (selectedPatient != null) {
            String selectedMedication =(String) JOptionPane.showInputDialog(
                    this,
                    "Select Medication:",
                    "Add Medication",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    availableMedications,
                    availableMedications[0]
            );
            if (selectedMedication != null && !selectedMedication.isEmpty()) {
                double medicationPrice = getMedicationPrice(selectedMedication);

                if (medicationPrice > 0) {
                    selectedPatient.addMedication(selectedMedication, medicationPrice);
                    updateMedicationsTextArea();
                    updateBillTextArea();
                } else {
                    JOptionPane.showMessageDialog(this, "Price not available for the selected medication.");
                }
            }
        }
    }

    private void printBill() {
        updateBillTextArea();
    }

    private void updateBillTextArea() {
        StringBuilder billText = new StringBuilder();
        for (Patient patient : patientList) {
            billText.append(patient.toString()).append("\n");
            billText.append("Medications:\n");
            for (String medication : patient.getMedications()) {
                billText.append(medication.toString()).append("\n");
            }
            billText.append("Total Bill: $").append(patient.calculateBill()).append("\n\n");
        }
        billTextArea.setText(billText.toString());
    }

    private void updatePatientHistoryTextArea() {
        if (patientList.isEmpty()) {
            patientHistoryTextArea.setText("No patients to display.");
            return;
        }

        StringBuilder historyText = new StringBuilder();
        for (Patient patient : patientList) {
            historyText.append(patient.getPatientHistory()).append("\n\n");
        }
        patientHistoryTextArea.setText(historyText.toString());
    }

    private void updateMedicationsTextArea() {
        Patient selectedPatient = getSelectedPatient();
        if (selectedPatient != null) {
            List<String> medications = selectedPatient.getMedications();
            StringBuilder medicationsText = new StringBuilder();
            for (String med : medications) {
                medicationsText.append(med).append("\n");
            }
            medicationTextArea.setText(medicationsText.toString());
        }
    }

    private Patient getSelectedPatient() {
        // For simplicity, assume the first patient in the list is selected
        if (!patientList.isEmpty()) {
            return patientList.get(0);
        }
        return null;
    }


    class LoginDialog extends JDialog {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton loginButton;

        private JButton emergencyWardButton;

        public LoginDialog(JFrame parent) {
            super(parent, "Login", true);
            setSize(300, 150);
            setLayout(new GridLayout(3, 2));

            usernameField = new JTextField();
            passwordField = new JPasswordField();
            loginButton = new JButton("Login");

            add(new JLabel("Username:"));
            add(usernameField);
            add(new JLabel("Password:"));
            add(passwordField);
            add(new JLabel()); // Empty label for spacing
            add(loginButton);

            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    performLogin();
                }
            });

        }

        private void performLogin() {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();
            String role = authenticateUser(username, password);

            if (role != null) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                setLoggedInUser(username, role);
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.");
            }
        }

        private String authenticateUser(String username, char[] password) {
            // In a real-world scenario, you would authenticate against a database or other authentication service.
            // For simplicity, a hardcoded check is used here.
            if (username.equals("admin") && new String(password).equals("admin")) {
                return "Administrator";
            } else if (username.equals("nurse") && new String(password).equals("nurse")) {
                return "Nurse";
            } else if (username.equals("receptionist") && new String(password).equals("receptionist")) {
                return "Receptionist";
            } else {
                return null;
            }
        }

        public void setLoggedInUser(String username, String role) {
            // Set the current user based on the logged-in role
            for (Staff staff : staffList) {
                if (staff.getUsername().equals(username) && staff.getRole().equals(role)) {
                    currentUser = staff;
                    break;
                }
            }
        }
    }

    class Staff {
        private String username;
        private String password;
        private String role;

        public Staff(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        @Override
        public String toString() {
            return username + " - " + role;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Staff staff = (Staff) obj;
            return Objects.equals(username, staff.username) && Objects.equals(role, staff.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, role);
        }
    }

    class Doctor {
        private String name;
        private double chargesPerDay;
        private String roundsInformation;

        public Doctor(String name, double chargesPerDay) {
            this.name = name;
            this.chargesPerDay = chargesPerDay;
            this.roundsInformation = roundsInformation;
        }

        public String getRounds() {
            return roundsInformation;
        }

        public double getChargesPerDay() {
            return chargesPerDay;
        }

        @Override
        public String toString() {
            return name + " - Charges: $" + chargesPerDay + "/day";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Doctor doctor = (Doctor) obj;
            return Double.compare(doctor.chargesPerDay, chargesPerDay) == 0 && Objects.equals(name, doctor.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, chargesPerDay);
        }

        public String getName() {
            return name;
        }

    }

    class Patient {
        private String name;
        private int daysStayed;
        private Doctor doctor;
        private Staff staff;
        private List<String> medications;
        private boolean isEmergency;

        public Patient(String name, Doctor doctor, Staff staff) {
            this.name = name;
            this.doctor = doctor;
            this.staff = staff;
            this.medications = new ArrayList<>();
            this.isEmergency = false;
        }

        public void addMedication(String medicationName, double medicationPrice) {
            medications.add(String.valueOf(new Medication(medicationName, medicationPrice)));
        }


        public List<String> getMedications() {
            return medications;
        }

        public void setDaysStayed(int daysStayed) {
            this.daysStayed = daysStayed;
        }

        public double calculateBill() {
            double medicationBill = 0.0;

            for (String medicationString : medications) {
                // Convert each medication string to Medication object
                Medication medication = convertToMedication(medicationString);

                // If the conversion is successful, add the price to the total bill
                if (medication != null) {
                    medicationBill += medication.getPrice();
                }
            }

            double doctorBill = daysStayed * doctor.getChargesPerDay();
            return medicationBill + doctorBill;
        }

        // Helper method to convert medication string to Medication object
        private Medication convertToMedication(String medicationString) {
            String[] parts = medicationString.split(" - Price: \\$");
            if (parts.length == 2) {
                String name = parts[0];
                double price = Double.parseDouble(parts[1]);
                return new Medication(name, price);
            }
            return null; // Return null if the conversion fails
        }


        public boolean isEmergency() {
            return isEmergency;
        }

        public void setEmergency(boolean emergency) {
            isEmergency = emergency;
        }

        public void addToBill(String item) {
            // Add the checked by doctor to the patient's bill displayed in the billTextArea
            billTextArea.append(item + "\n");
        }

        public String getPatientHistory() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "Patient: " + name + "\nDoctor: " + doctor + "\nStaff: " + staff +
                    "\nAdmission Date: " + sdf.format(new Date()) + "\nDays Stayed: " + daysStayed;
        }
        @Override
        public String toString() {
            return "Patient: " + name + "\nDoctor: " + doctor + "\nStaff: " + staff;
        }

        public Doctor getDoctor() {
            return null;
        }

        public String getName() {
            return name;
        }
    }
    double getMedicationPrice(String medication) {
        // Simulating random prices for medications (replace this with your actual data)
        double[] medicationPrices = {15.0, 25.0, 10.0};

        for (int i = 0; i < availableMedications.length; i++) {
            if (availableMedications[i].equalsIgnoreCase(medication)) {
                return medicationPrices[i];
            }
        }

        return -1; // Return -1 if the medication is not found
    }

    class EmergencyWard {
        private int totalBeds;
        private int filledBeds;
        private List<Doctor> availableDoctors;
        private List<Staff> staffList;

        private HashMap<Doctor, String> doctorRounds;

        public EmergencyWard(int totalBeds) {
            this.totalBeds = totalBeds;
            this.filledBeds = 0;
            this.availableDoctors = new ArrayList<>();
            this.staffList = new ArrayList<>();
            availableDoctors = new ArrayList<>();
            this.doctorRounds = new HashMap<>();

            initializeDoctors();
        }

        private void initializeDoctors() {
            Doctor doctor1 = new Doctor("Dr. Smith", 200.0);
            availableDoctors.add(doctor1);
            doctorRounds.put(doctor1, "Morning: 8 AM - 10 AM, Afternoon: 2 PM - 4 PM");

            Doctor doctor2 = new Doctor("Dr. Johnson", 250.0);
            availableDoctors.add(doctor2);
            doctorRounds.put(doctor2, "Morning: 9 AM - 11 AM, Afternoon: 3 PM - 5 PM");
        }


        public void addAvailableDoctor(Doctor doctor) {
            availableDoctors.add(doctor);
        }

        public void addStaff(Staff staff) {
            staffList.add(staff);
        }

        public int getFilledBeds() {
            return filledBeds;
        }

        public int getEmptyBeds() {
            return totalBeds - filledBeds;
        }

        public String displayAvailableDoctors() {
            StringBuilder availableDoctorsInfo = new StringBuilder();
            availableDoctorsInfo.append("Available Doctors in Emergency Ward:\n");
            for (Doctor doctor : availableDoctors) {
                availableDoctorsInfo.append(doctor.toString()).append("\n");
            }
            return availableDoctorsInfo.toString();
        }

        public String displayDoctorRounds() {
            StringBuilder doctorRoundsInfo = new StringBuilder();
            doctorRoundsInfo.append("Doctor Rounds Schedule:\n");
            for (HashMap.Entry<Doctor, String> entry : doctorRounds.entrySet()) {
                doctorRoundsInfo.append(entry.getKey().getName()).append(": ").append(entry.getValue()).append("\n");
            }
            return doctorRoundsInfo.toString();
        }

        public String displayAvailableStaff() {
            StringBuilder availableStaffInfo = new StringBuilder();
            availableStaffInfo.append("Available Staff in Emergency Ward:\n");
            for (Staff staff : staffList) {
                availableStaffInfo.append(staff.toString()).append("\n");
            }
            return availableStaffInfo.toString();
        }

        public void occupyBed() {
            filledBeds++;
        }

        public void releaseStaff() {
            // For simplicity, assuming 1 staff member per patient
            // Reduce staff count when a patient is admitted
            if (!staffList.isEmpty()) {
                staffList.remove(0);
            }
        }

        public void releaseBed() {
            if (filledBeds > 0) {
                filledBeds--;
            }
        }
    }
    private class Medication {
        private String name;
        private double price;

        public Medication(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        @Override
        public String toString() {
            return name + " - Price: $" + price;
        }
    }



    public static void main(String[] args) {


        {
            SwingUtilities.invokeLater(() -> {
                new HospitalManagementSystem().setVisible(true);
            });

        }

    }
}
