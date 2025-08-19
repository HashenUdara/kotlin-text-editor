#include <iostream>
#include <string>
#include <vector>
using namespace std;

// Base Class
class Transaction {
protected:
    int transaction_ID;
    string date;

public:
    virtual void recordTransaction() = 0; // Pure virtual function
    virtual void displayInfo() = 0;       // Pure virtual function
    virtual ~Transaction() {}             // Virtual destructor
};

// Derived Class - IncomeTransaction
class IncomeTransaction : public Transaction {
private:
    string source;
    double amount;

public:
    void recordTransaction() override {
        cout << "\n--- Recording Income Transaction ---\n";
        cout << "Enter Transaction ID: ";
        cin >> transaction_ID;
        cout << "Enter Date (YYYY-MM-DD): ";
        cin >> date;
        cout << "Enter Source of Income: ";
        cin.ignore();
        getline(cin, source);
        cout << "Enter Amount Received: ";
        cin >> amount;
    }

    void displayInfo() override {
        cout << "\n[Income Transaction]\n";
        cout << "Transaction ID: " << transaction_ID << endl;
        cout << "Date: " << date << endl;
        cout << "Source: " << source << endl;
        cout << "Amount Received: " << amount << endl;
    }
};

// Derived Class - ExpenseTransaction
class ExpenseTransaction : public Transaction {
private:
    string category;
    double amount;

public:
    void recordTransaction() override {
        cout << "\n--- Recording Expense Transaction ---\n";
        cout << "Enter Transaction ID: ";
        cin >> transaction_ID;
        cout << "Enter Date (YYYY-MM-DD): ";
        cin >> date;
        cout << "Enter Expense Category: ";
        cin.ignore();
        getline(cin, category);
        cout << "Enter Amount Spent: ";
        cin >> amount;
    }

    void displayInfo() override {
        cout << "\n[Expense Transaction]\n";
        cout << "Transaction ID: " << transaction_ID << endl;
        cout << "Date: " << date << endl;
        cout << "Category: " << category << endl;
        cout << "Amount Spent: " << amount << endl;
    }
};

// Main function
int main() {
    vector<Transaction*> transactions;
    int choice;

    do {
        cout << "\n===== Transaction Menu =====\n";
        cout << "1. Record Income Transaction\n";
        cout << "2. Record Expense Transaction\n";
        cout << "3. Display All Transactions\n";
        cout << "4. Exit\n";
        cout << "Enter your choice: ";
        cin >> choice;

        Transaction* t = nullptr;

        switch (choice) {
            case 1:
                t = new IncomeTransaction();
                t->recordTransaction();
                transactions.push_back(t);
                break;
            case 2:
                t = new ExpenseTransaction();
                t->recordTransaction();
                transactions.push_back(t);
                break;
            case 3:
                cout << "\n===== Transaction Records =====\n";
                for (auto& tr : transactions) {
                    tr->displayInfo();
                }
                break;
            case 4:
                cout << "Exiting...\n";
                break;
            default:
                cout << "Invalid choice! Try again.\n";
        }
    } while (choice != 4);

    // Free memory
    for (auto& tr : transactions) {
        delete tr;
    }

    return 0;
}