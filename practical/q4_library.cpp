#include <iostream>
#include <string>
#include <vector>
using namespace std;

// --------------------- Book Class ---------------------
class Book {
private:
    string title;
    string author;
    string ISBN;
    bool isAvailable;

public:
    // Constructor
    Book(string t = "", string a = "", string i = "") 
        : title(t), author(a), ISBN(i), isAvailable(true) {}

    // Setters
    void setTitle(string t) { title = t; }
    void setAuthor(string a) { author = a; }
    void setISBN(string i) { ISBN = i; }

    // Getters
    string getTitle() const { return title; }
    string getAuthor() const { return author; }
    string getISBN() const { return ISBN; }
    bool getAvailability() const { return isAvailable; }

    // Operations
    bool checkOut() {
        if (isAvailable) {
            isAvailable = false;
            return true;
        }
        return false;
    }

    void returnBook() { isAvailable = true; }

    void displayInfo() const {
        cout << "Title: " << title << "\nAuthor: " << author 
             << "\nISBN: " << ISBN 
             << "\nStatus: " << (isAvailable ? "Available" : "Checked Out") 
             << "\n----------------------\n";
    }
};

// --------------------- Patron Class ---------------------
class Patron {
private:
    string name;
    string cardNumber;
    vector<Book*> borrowedBooks;

public:
    // Constructor
    Patron(string n = "", string c = "") : name(n), cardNumber(c) {}

    // Setters
    void setName(string n) { name = n; }
    void setCardNumber(string c) { cardNumber = c; }

    // Getters
    string getName() const { return name; }
    string getCardNumber() const { return cardNumber; }

    // Borrow a book
    void borrowBook(Book &book) {
        if (book.checkOut()) {
            borrowedBooks.push_back(&book);
            cout << name << " borrowed \"" << book.getTitle() << "\"\n";
        } else {
            cout << "Sorry, \"" << book.getTitle() << "\" is not available.\n";
        }
    }

    // Return a book
    void returnBook(Book &book) {
        for (auto it = borrowedBooks.begin(); it != borrowedBooks.end(); ++it) {
            if (*it == &book) {
                book.returnBook();
                borrowedBooks.erase(it);
                cout << name << " returned \"" << book.getTitle() << "\"\n";
                return;
            }
        }
        cout << name << " did not borrow \"" << book.getTitle() << "\"\n";
    }

    // Display patron information
    void displayInfo() const {
        cout << "Patron Name: " << name 
             << "\nCard Number: " << cardNumber << "\nBorrowed Books:\n";
        if (borrowedBooks.empty()) {
            cout << "  None\n";
        } else {
            for (const auto &book : borrowedBooks) {
                cout << "  - " << book->getTitle() << " by " << book->getAuthor() << "\n";
            }
        }
        cout << "----------------------\n";
    }
};

// --------------------- Main Function ---------------------
int main() {
    // Create books
    Book b1("The Great Gatsby", "F. Scott Fitzgerald", "12345");
    Book b2("1984", "George Orwell", "67890");

    // Create patrons
    Patron p1("Alice", "P001");
    Patron p2("Bob", "P002");

    // Display initial book info
    b1.displayInfo();
    b2.displayInfo();

    // Borrow and return operations
    p1.borrowBook(b1);
    p1.borrowBook(b2);
    p2.borrowBook(b1); // Already borrowed by Alice

    p1.displayInfo();
    p2.displayInfo();

    // Alice returns book
    p1.returnBook(b1);
    p2.borrowBook(b1); // Now available for Bob

    // Final status
    b1.displayInfo();
    b2.displayInfo();
    p1.displayInfo();
    p2.displayInfo();

    return 0;
}