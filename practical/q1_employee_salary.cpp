#include <iostream>
#include <string>
using namespace std;

// Base class
class Employee {
protected:
    string name;
public:
    Employee(string n) : name(n) {}
    virtual double calculateSalary() = 0;  // Pure virtual function
    virtual void displaySalary() {
        cout << name << "'s Salary: Rs. " << calculateSalary() << endl;
    }
    virtual ~Employee() {}
};

// Derived class: Full-time Employee
class FullTimeEmployee : public Employee {
private:
    double monthlySalary;
public:
    FullTimeEmployee(string n, double salary) : Employee(n), monthlySalary(salary) {}
    double calculateSalary() override {
        return monthlySalary;
    }
};

// Derived class: Part-time Employee
class PartTimeEmployee : public Employee {
private:
    double hourlyRate;
    int hoursPerWeek;
public:
    PartTimeEmployee(string n, double rate, int hours) 
        : Employee(n), hourlyRate(rate), hoursPerWeek(hours) {}
    double calculateSalary() override {
        return hourlyRate * hoursPerWeek * 4;  // Assuming 4 weeks in a month
    }
};

// Derived class: Contract Employee
class ContractEmployee : public Employee {
private:
    double fixedPayment;
public:
    ContractEmployee(string n, double payment) : Employee(n), fixedPayment(payment) {}
    double calculateSalary() override {
        return fixedPayment;
    }
};

int main() {
    // Creating employees
    FullTimeEmployee kamal("Kamal", 60000);
    PartTimeEmployee piyal("Piyal", 2000, 20);
    ContractEmployee damith("Damith", 30000);

    // Displaying salaries
    kamal.displaySalary();
    piyal.displaySalary();
    damith.displaySalary();

    return 0;
}