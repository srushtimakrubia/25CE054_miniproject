import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/*
 * MiniBank.java
 * Single-file MiniBank practical project, compatible with Java 8.
 * All classes are kept in this one file for easy compiling and studying.
 */

public class MiniBank {
    private static final Scanner sc = new Scanner(System.in);
    private static final AccountStore store = new AccountStore();

    public static void main(String[] args) {
        store.save(new SavingsAccount(new Customer("Panthi", "panthi@example.com"), 1000.0));
        int choice = -1;

        do {
            System.out.println("\n========== MINI BANK ==========");
            System.out.println("1. Show all accounts");
            System.out.println("2. Create account");
            System.out.println("3. Deposit");
            System.out.println("4. Withdraw");
            System.out.println("5. Transfer money");
            System.out.println("6. Calculate interest");
            System.out.println("7. Show transaction history");
                        System.out.println("8. Search account");
            System.out.println("9. Sort accounts by balance");
            System.out.println("10. Save accounts to file");
            System.out.println("11. Load accounts from file");
            System.out.println("12. Generate text report");
            System.out.println("13. Run practical demonstrations");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
                switch (choice) {
                    case 1: store.showAll(); break;
                    case 2: createAccount(); break;
                    case 3: deposit(); break;
                    case 4: withdraw(); break;
                    case 5: transfer(); break;
                    case 6: calculateInterest(); break;
                    case 7: showHistory(); break;
                    case 8: searchAccount(); break;
                    case 9: store.showSortedByBalance(); break;
                    case 10: saveAccounts(); break;
                    case 11: loadAccounts(); break;
                    case 12: generateReport(); break;
                    case 13: runPracticalDemos(); break;
                    case 0: System.out.println("Thank you for using MiniBank!"); break;
                    default: System.out.println("Please enter a valid choice.");
                }
            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        } while (choice != 0);
    }

    private static void createAccount() {
        System.out.print("Customer name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty() || name.length() > 40) {
            System.out.println("Name must contain 1 to 40 characters.");
            return;
        }

        System.out.print("Customer email: ");
        String email = sc.nextLine().trim();
        if (!Validator.isValidEmail(email)) {
            System.out.println("Email format is not valid.");
            return;
        }

        System.out.println("1. Savings  2. Current  3. Fixed Deposit");
        int type = readInt("Choose account type: ");
        double opening = readDouble("Opening balance: ");
        if (opening < 0) {
            System.out.println("Opening balance cannot be negative.");
            return;
        }

        Customer customer = new Customer(name, email);
        Account account;
        if (type == 1) account = new SavingsAccount(customer, opening);
        
        else if (type == 2) account = new CurrentAccount(customer, opening);
        else if (type == 3) account = new FixedDepositAccount(customer, opening);
        else {
            System.out.println("Invalid account type.");
            return;
        }
        store.save(account);
        System.out.println("Account created. Account ID: " + account.getId());
    }

    private static void deposit() throws InvalidAmountException {
        Account account = askForAccount();
        if (account == null) return;
        double amount = readDouble("Amount to deposit: ");
        account.deposit(amount);
        System.out.println("Deposit successful. New balance: " + account.getBalance());
    }

    private static void withdraw() throws InvalidAmountException, InsufficientFundsException {
        Account account = askForAccount();
        if (account == null) return;
        double amount = readDouble("Amount to withdraw: ");
        account.withdraw(amount);
        System.out.println("Withdrawal successful. New balance: " + account.getBalance());
    }

    private static void transfer() throws InvalidAmountException, InsufficientFundsException {
        System.out.println("From account:");
        Account from = askForAccount();
        if (from == null) return;
        System.out.println("To account:");
        Account to = askForAccount();
        if (to == null) return;
        if (from.getId() == to.getId()) {
            System.out.println("Choose two different accounts.");
            return;
        }
        double amount = readDouble("Transfer amount: ");
        TransactionService.transfer(from, to, amount);
        System.out.println("Transfer complete.");
    }

    private static void calculateInterest() {
        Account account = askForAccount();
        if (account != null) {
            System.out.println("Account type: " + account.getAccountType());
            System.out.printf("Estimated interest: %.2f%n", account.calculateInterest());
        }
    }

    private static void showHistory() {
        Account account = askForAccount();
        if (account == null) return;
        System.out.println("History for account " + account.getId() + ":");
        for (String entry : account.getHistory()) System.out.println(" - " + entry);
    }

    private static void searchAccount() {
        int id = readInt("Enter account ID: ");
        Account account = store.find(id);
        System.out.println(account == null ? "Account not found." : account);
    }

    private static void saveAccounts() throws IOException {
        StateFile.save(store, "minibank.dat");
        System.out.println("Saved to minibank.dat");
    }

    private static void loadAccounts() throws IOException, ClassNotFoundException {
        AccountStore loaded = StateFile.load("minibank.dat");
        if (loaded == null) {
            System.out.println("No saved file found.");
        } else {
            store.replaceAll(loaded.getAll());
            System.out.println("Accounts loaded.");
        }
    }

    private static void generateReport() throws IOException {
        ReportWriter.write(store.getAll(), "minibank-report.txt");
        System.out.println("Report written to minibank-report.txt");
    }

    private static void runPracticalDemos() throws Exception {
        System.out.println("\n--- Practical demonstrations ---");
        System.out.println("1. Customer equals/hashCode: " +
                new Customer("Demo", "demo@example.com").equals(
                new Customer("Other name", "demo@example.com")));
        System.out.println("2. Email regex validation: " +
                Validator.isValidEmail("student@example.com"));
        System.out.println("3. Annotation/reflection validation: " +
                AnnotationValidator.validate(new DemoRecord("Demo", 5)));
        System.out.println("4. Generic repository saved count: " + genericDemo());
        System.out.println("5. Producer-consumer demonstration:");
        ProducerConsumerDemo.run();
        System.out.println("6. Thread pool demonstration:");
        ThreadDemo.run(store.getAll());
        System.out.println("7. Try-with-resources demonstration:");
        try (TextResource resource = new TextResource("MiniBank resource closed safely.")) {
            resource.use();
        }
        System.out.println("Demonstrations finished.");
    }

    private static int genericDemo() {
        Repository<Account> repository = new Repository<Account>();
        for (Account account : store.getAll()) repository.save(account);
        return repository.size();
    }

    private static Account askForAccount() {
        int id = readInt("Enter account ID: ");
        Account account = store.find(id);
        if (account == null) System.out.println("Account not found.");
        return account;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid number."); }
        }
    }
}

enum AccountType { SAVINGS, CURRENT, FIXED_DEPOSIT }

/* Java 8 alternative to a record: a small immutable data class. */
class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    @MaxLength(40) private final String name;
    @Id private final String email;

    Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }
    String getName() { return name; }
    String getEmail() { return email; }

    @Override public String toString() { return name + " (" + email + ")"; }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Customer)) return false;
        Customer other = (Customer) obj;
        return Objects.equals(email, other.email);
    }
    @Override public int hashCode() { return Objects.hash(email); }
}

class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String city;
    private final String street;
    Address(String city, String street) { this.city = city; this.street = street; }
    @Override public String toString() { return street + ", " + city; }
}

interface Transactable {
    void deposit(double amount) throws InvalidAmountException;
    void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException;
}

interface InterestBearing {
    double calculateInterest();
    default String interestMessage() { return "Interest is an estimate, not a bank guarantee."; }
}

interface WithdrawRule {
    boolean canWithdraw(double balance, double amount);
}

interface Premium { }

abstract class Account implements Serializable, Comparable<Account>, Transactable, InterestBearing {
    private static final long serialVersionUID = 1L;
    private static int nextId = 1001;

    @Id private final int id;
    private final Customer customer;
    private final Address address;
    @Positive private double balance;
    private final List<String> history = new ArrayList<String>();

    Account(Customer customer, double openingBalance) {
        this(customer, openingBalance, new Address("Unknown", "Not provided"));
    }

    Account(Customer customer, double openingBalance, Address address) {
        this.id = nextId++;
        this.customer = customer;
        this.balance = openingBalance;
        this.address = address;
        history.add("Opened account with balance " + openingBalance);
    }

    public int getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Address getAddress() { return address; }
    public synchronized double getBalance() { return balance; }
    public synchronized List<String> getHistory() { return new ArrayList<String>(history); }
    public abstract AccountType getAccountType();

    @Override public synchronized void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than zero.");
        balance += amount;
        history.add("Deposited " + amount);
    }

    @Override public synchronized void withdraw(double amount)
            throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than zero.");
        WithdrawRule rule = new WithdrawRule() {
            public boolean canWithdraw(double currentBalance, double requested) {
                return requested <= currentBalance;
            }
        };
        if (!rule.canWithdraw(balance, amount))
            throw new InsufficientFundsException("Insufficient balance.");
        balance -= amount;
        history.add("Withdrew " + amount);
    }

    synchronized void recordTransfer(String message) { history.add(message); }

    @Override public int compareTo(Account other) {
        return Double.compare(this.getBalance(), other.getBalance());
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(id)
          .append(" | Customer: ").append(customer)
          .append(" | Type: ").append(getAccountType())
          .append(" | Balance: ").append(getBalance());
        return sb.toString();
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Account)) return false;
        return id == ((Account) obj).id;
    }
    @Override public int hashCode() { return Integer.valueOf(id).hashCode(); }
}

class SavingsAccount extends Account implements Premium {
    private static final long serialVersionUID = 1L;
    SavingsAccount(Customer customer, double openingBalance) { super(customer, openingBalance); }
    SavingsAccount(Customer customer, double openingBalance, Address address) {
        super(customer, openingBalance, address);
    }
    @Override public AccountType getAccountType() { return AccountType.SAVINGS; }
    @Override public double calculateInterest() { return getBalance() * 0.04; }
}

class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    CurrentAccount(Customer customer, double openingBalance) { super(customer, openingBalance); }
    @Override public AccountType getAccountType() { return AccountType.CURRENT; }
    @Override public double calculateInterest() { return 0.0; }
}

class FixedDepositAccount extends Account {
    private static final long serialVersionUID = 1L;
    FixedDepositAccount(Customer customer, double openingBalance) { super(customer, openingBalance); }
    @Override public AccountType getAccountType() { return AccountType.FIXED_DEPOSIT; }
    @Override public double calculateInterest() { return getBalance() * 0.07; }
}

class BankException extends Exception {
    BankException(String message) { super(message); }
}
class InvalidAmountException extends BankException {
    InvalidAmountException(String message) { super(message); }
}
class InsufficientFundsException extends BankException {
    InsufficientFundsException(String message) { super(message); }
}
class AccountNotFoundException extends BankException {
    AccountNotFoundException(String message) { super(message); }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Id { }

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Positive { }

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface MaxLength { int value() default 40; }

class Validator {
    static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    static void validatePositive(double amount) throws InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than zero.");
         }
}

class AnnotationValidator {
    static boolean validate(Object object) {
        try {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(object);
                if (field.isAnnotationPresent(Id.class) &&
                        (value == null || value.toString().trim().isEmpty())) return false;
                if (field.isAnnotationPresent(Positive.class) && value instanceof Number &&
                        ((Number) value).doubleValue() <= 0) return false;
                if (field.isAnnotationPresent(MaxLength.class) && value instanceof String) {
                    int max = field.getAnnotation(MaxLength.class).value();
                    if (((String) value).length() > max) return false;
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }
}

class DemoRecord {
    @MaxLength(40) private final String name;
    @Positive private final int score;
    DemoRecord(String name, int score) { this.name = name; this.score = score; }
}

class AccountStore implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<Integer, Account> accounts = new HashMap<Integer, Account>();

    synchronized void save(Account account) { accounts.put(account.getId(), account); }
    synchronized Account find(int id) { return accounts.get(id); }
    synchronized List<Account> getAll() { return new ArrayList<Account>(accounts.values()); }

    synchronized void replaceAll(List<Account> loaded) {
        accounts.clear();
        for (Account account : loaded) accounts.put(account.getId(), account);
    }

    synchronized void showAll() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        for (Account account : accounts.values()) System.out.println(account);
    }

    synchronized void showSortedByBalance() {
        List<Account> list = getAll();
        Collections.sort(list, new Comparator<Account>() {
            @Override public int compare(Account a, Account b) {
                return Double.compare(a.getBalance(), b.getBalance());
            }
        });
        for (Account account : list) System.out.println(account);
    }
}

class TransactionService {
    static void transfer(Account from, Account to, double amount)
            throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) throw new InvalidAmountException("Transfer amount must be greater than zero.");
        Account first = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to : from;
        synchronized (first) {
            synchronized (second) {
                if (from.getBalance() < amount)
                    throw new InsufficientFundsException("Insufficient balance for transfer.");
                from.withdraw(amount);
                to.deposit(amount);
                from.recordTransfer("Transferred " + amount + " to account " + to.getId());
                to.recordTransfer("Received " + amount + " from account " + from.getId());
            }
        }
    }
}

class Repository<T> {
    private final List<T> items = new ArrayList<T>();
    public void save(T item) { items.add(item); }
    @SafeVarargs public final void saveAll(T... values) {
        for (T value : values) items.add(value);
    }
    public int size() { return items.size(); }
    public List<T> findAll() { return new ArrayList<T>(items); }

    public static double averageBalance(List<? extends Account> accounts) {
        if (accounts.isEmpty()) return 0;
        double sum = 0;
        for (Account account : accounts) sum += account.getBalance();
        return sum / accounts.size();
    }
}

class StateFile {
     static void save(AccountStore store, String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(store);
        }
    }
    static AccountStore load(String filename) throws IOException, ClassNotFoundException {
        File file = new File(filename);
        if (!file.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (AccountStore) in.readObject();
        }
    }
}

class ReportWriter {
    static void write(List<Account> accounts, String filename) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("MINIBANK ACCOUNT REPORT");
        lines.add("=======================");
        for (Account account : accounts) lines.add(account.toString());
        Files.write(Paths.get(filename), lines, java.nio.charset.StandardCharsets.UTF_8);
    }
}

class TextResource implements AutoCloseable {
    private final String message;
    TextResource(String message) { this.message = message; }
    void use() { System.out.println(message); }
    @Override public void close() { System.out.println("Resource closed."); }
}

class ProducerConsumerDemo {
    static void run() throws InterruptedException {
        final Queue<Integer> queue = new LinkedList<Integer>();
        final Object lock = new Object();

        Thread producer = new Thread(new Runnable() {
            public void run() {
                synchronized (lock) {
                    for (int i = 1; i <= 3; i++) {
                        queue.add(i);
                        System.out.println("Produced: " + i);
                        lock.notifyAll();
                    }
                }
            }
        });

        Thread consumer = new Thread(new Runnable() {
            public void run() {
                synchronized (lock) {
                    for (int i = 1; i <= 3; i++) {
                        while (queue.isEmpty()) {
                            try { lock.wait(); }
                            catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        System.out.println("Consumed: " + queue.remove());
                    }
                }
            }
        });

        consumer.start();
        producer.start();
        producer.join();
        consumer.join();
    }
}

class ThreadDemo {
    static void run(List<Account> accounts) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (final Account account : accounts) {
             pool.submit(new Runnable() {
                public void run() {
                    System.out.println(Thread.currentThread().getName()
                            + " checked account " + account.getId()
                            + " balance " + account.getBalance());
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }
}

