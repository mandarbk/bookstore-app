-- V2__insert_sample_data.sql - Sample data for testing

INSERT INTO books (title, author, isbn, price, inventory, description) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 12.99, 50, 'A classic American novel set in the Jazz Age'),
('To Kill a Mockingbird', 'Harper Lee', '978-0061120084', 14.99, 45, 'A gripping tale of racial injustice and childhood innocence'),
('1984', 'George Orwell', '978-0451524935', 13.99, 60, 'A dystopian novel about totalitarianism'),
('Pride and Prejudice', 'Jane Austen', '978-0141439518', 11.99, 40, 'A romantic novel of manners and marriages'),
('The Catcher in the Rye', 'J.D. Salinger', '978-0316769174', 13.99, 35, 'A story of teenage alienation and angst');

INSERT INTO customers (name, email, phone, address) VALUES
('John Doe', 'john.doe@example.com', '+1-555-0101', '123 Main St, New York, NY 10001'),
('Jane Smith', 'jane.smith@example.com', '+1-555-0102', '456 Oak Ave, Los Angeles, CA 90001'),
('Bob Johnson', 'bob.johnson@example.com', '+1-555-0103', '789 Pine Rd, Chicago, IL 60601');


