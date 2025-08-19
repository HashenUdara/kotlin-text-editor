#include <iostream>
#include <string>
using namespace std;

// Base Class
class Person {
protected:
    string name;
    int age;

public:
    virtual void getdata() = 0;   // Pure virtual function
    virtual void putdata() = 0;   // Pure virtual function
    virtual ~Person() {}          // Virtual destructor
};

// Doctor Class
class Doctor : public Person {
private:
    int doctor_specialist;
    int doctor_id;
    static int doctor_count; // Static counter

public:
    Doctor() {
        doctor_id = ++doctor_count; // Assign sequential ID
    }

    void getdata() override {
        cout << "Enter Doctor Name: ";
        cin >> name;
        cout << "Enter Age: ";
        cin >> age;
        cout << "Enter Specialist ID: ";
        cin >> doctor_specialist;
    }

    void putdata() override {
        cout << "Doctor -> Name: " << name
             << ", Age: " << age
             << ", Specialist ID: " << doctor_specialist
             << ", Doctor ID: " << doctor_id << endl;
    }
};

// Initialize static variable
int Doctor::doctor_count = 0;

// Patient Class
class Patient : public Person {
private:
    string admission_date;
    int patient_id;
    static int patient_count; // Static counter

public:
    Patient() {
        patient_id = ++patient_count; // Assign sequential ID
    }

    void getdata() override {
        cout << "Enter Patient Name: ";
        cin >> name;
        cout << "Enter Age: ";
        cin >> age;
        cout << "Enter Admission Date: ";
        cin >> admission_date;
    }

    void putdata() override {
        cout << "Patient -> Name: " << name
             << ", Age: " << age
             << ", Admission Date: " << admission_date
             << ", Patient ID: " << patient_id << endl;
    }
};

// Initialize static variable
int Patient::patient_count = 0;

// Main Function
int main() {
    int n;
    cout << "Enter number of records: ";
    cin >> n;

    Person* people[n]; // Array of base class pointers

    for (int i = 0; i < n; i++) {
        int choice;
        cout << "\nEnter 1 for Doctor, 2 for Patient: ";
        cin >> choice;

        if (choice == 1) {
            people[i] = new Doctor();
        } else {
            people[i] = new Patient();
        }
        people[i]->getdata();
    }

    cout << "\n--- Records ---\n";
    for (int i = 0; i < n; i++) {
        people[i]->putdata();
    }

    // Clean up
    for (int i = 0; i < n; i++) {
        delete people[i];
    }

    return 0;
}